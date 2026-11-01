package es.sebas1705.home.profile.viewmodel

import android.app.Activity
import android.content.Context
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import es.sebas1705.authentication.DeleteCurrentUserUseCase
import es.sebas1705.authentication.GetAccountProviderUseCase
import es.sebas1705.authentication.IsGuestUserUseCase
import es.sebas1705.authentication.ReauthenticateWithGoogleUseCase
import es.sebas1705.authentication.ReauthenticateWithPasswordUseCase
import es.sebas1705.authentication.SignOutUseCase
import es.sebas1705.authentication.config.AccountProvider
import es.sebas1705.common.mvi.MVIBaseViewModel
import es.sebas1705.common.responses.ErrorResponseType
import es.sebas1705.common.responses.ResponseState
import es.sebas1705.common.utlis.alias.FlowResponseNothing
import es.sebas1705.common.utlis.extensions.composables.printTextInToast
import es.sebas1705.common.utlis.extensions.types.logI
import es.sebas1705.core.resources.R
import es.sebas1705.game.settings.ReadGameUseCase
import es.sebas1705.home.profile.models.OfflineProfileRecordRowUi
import es.sebas1705.models.Categories
import es.sebas1705.models.GameModel
import es.sebas1705.models.OfflineRankingModel
import es.sebas1705.ranking.ReadOfflineRankingUseCase
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
@Suppress("TooManyFunctions", "LongParameterList")
class ProfileViewModel @Inject constructor(
    private val signOutUseCase: SignOutUseCase,
    private val isGuestUserUseCase: IsGuestUserUseCase,
    private val getAccountProviderUseCase: GetAccountProviderUseCase,
    private val deleteCurrentUserUseCase: DeleteCurrentUserUseCase,
    private val reauthenticateWithPasswordUseCase: ReauthenticateWithPasswordUseCase,
    private val reauthenticateWithGoogleUseCase: ReauthenticateWithGoogleUseCase,
    private val readOfflineRankingUseCase: ReadOfflineRankingUseCase,
    private val readGameUseCase: ReadGameUseCase,
    private val savedStateHandle: SavedStateHandle,
    @ApplicationContext context: Context
) : MVIBaseViewModel<ProfileState, ProfileIntent>(context) {

    override fun initState(): ProfileState = ProfileState(
        offlineRecordSort = readSavedProfileSort(savedStateHandle)
    )

    // Not in onInit(): the base class posts that to Main, so it can run after the first Load has
    // already finished (cached DataStore) and would switch the spinner back on for good.
    init {
        startLoading()
    }

    override fun intentHandler(intent: ProfileIntent) =
        when (intent) {
            ProfileIntent.Load -> loadProfileData()
            ProfileIntent.SignOut -> signOut()
            ProfileIntent.ConsumeSignOutNavigation -> consumeSignOutNavigation()
            ProfileIntent.RequestDeleteAccount -> showDeleteAccountStep(DeleteAccountStep.Confirm)
            ProfileIntent.DismissDeleteAccount -> showDeleteAccountStep(DeleteAccountStep.Hidden)
            ProfileIntent.ConfirmDeleteAccount -> deleteAccount(DeleteAccountStep.Confirm)
            is ProfileIntent.ReauthenticateWithPassword -> reauthenticateAndDelete(
                DeleteAccountStep.ReauthPassword
            ) { reauthenticateWithPasswordUseCase(intent.password) }
            is ProfileIntent.ReauthenticateWithGoogle -> reauthenticateWithGoogle(intent.activity)
            is ProfileIntent.SelectTab -> updateUi { it.copy(selectedTab = intent.tab) }
            is ProfileIntent.ToggleOfflineRecordSort -> toggleOfflineSort(intent.column)
        }

    private fun loadProfileData() = execute(Dispatchers.IO) {
        startLoading()
        updateUi { it.copy(errorMessage = null, canDeleteAccount = !isGuestUserUseCase()) }

        runCatching {
            val game = readGameUseCase().first()
            val rankingEntries = readOfflineRankingUseCase()
            buildProfilePayload(game, rankingEntries)
        }
            .onSuccess { payload ->
                val currentSort = uiState.value.offlineRecordSort
                stopLoading()
                updateUi {
                    it.copy(
                        offlineRecordRows = sortProfileOfflineRows(payload.offlineRows, currentSort),
                        rolePreference = payload.rolePreference,
                        favoriteCategory = payload.favoriteCategory,
                        matchesPlayed = payload.matchesPlayed,
                        currentStreak = payload.currentStreak,
                        bestStreak = payload.bestStreak
                    )
                }
            }
            .onFailure { throwable ->
                stopLoading()
                updateUi { it.copy(errorMessage = throwable.message) }
            }
    }

    private fun buildProfilePayload(
        game: GameModel,
        rankingEntries: List<OfflineRankingModel>
    ): ProfileStatePayload {
        val primaryPlayer = game.players.firstOrNull().orEmpty().trim()
        val primaryPlayerEntry = rankingEntries.firstOrNull {
            it.playerName.equals(primaryPlayer, ignoreCase = true)
        }

        val roleSource = primaryPlayerEntry ?: aggregateTotalsEntry(rankingEntries)
        val rolePreference = if (roleSource.impostorWins > roleSource.civilianWins)
            ProfileRolePreference.ImpostorSpecialist
        else ProfileRolePreference.ImpostorHunter

        return ProfileStatePayload(
            offlineRows = rankingEntries.mapIndexed { index, entry ->
                OfflineProfileRecordRowUi(
                    position = index,
                    playerName = entry.playerName,
                    civilianWins = entry.civilianWins,
                    impostorWins = entry.impostorWins,
                    totalWins = entry.totalWins,
                    currentStreak = entry.currentStreak
                )
            }.toImmutableList(),
            rolePreference = rolePreference,
            favoriteCategory = game.selectedCategories.firstOrNull(),
            matchesPlayed = primaryPlayerEntry?.totalWins ?: rankingEntries.sumOf { it.totalWins },
            currentStreak = primaryPlayerEntry?.currentStreak
                ?: rankingEntries.maxOfOrNull { it.currentStreak }
                ?: 0,
            bestStreak = primaryPlayerEntry?.bestStreak
                ?: rankingEntries.maxOfOrNull { it.bestStreak }
                ?: 0
        )
    }

    private fun aggregateTotalsEntry(entries: List<OfflineRankingModel>): OfflineRankingModel =
        OfflineRankingModel(
            playerName = "all",
            civilianWins = entries.sumOf { it.civilianWins },
            impostorWins = entries.sumOf { it.impostorWins }
        )

    private data class ProfileStatePayload(
        val offlineRows: kotlinx.collections.immutable.ImmutableList<OfflineProfileRecordRowUi>,
        val rolePreference: ProfileRolePreference,
        val favoriteCategory: Categories?,
        val matchesPlayed: Int,
        val currentStreak: Int,
        val bestStreak: Int
    )

    private fun toggleOfflineSort(column: ProfileOfflineRecordSortColumn) = execute {
        updateUi { state ->
            val nextSort = resolveNextProfileSort(state.offlineRecordSort, column)
            persistProfileSort(savedStateHandle, nextSort)
            state.copy(
                offlineRecordSort = nextSort,
                offlineRecordRows = sortProfileOfflineRows(state.offlineRecordRows, nextSort)
            )
        }
    }

    private fun signOut() = execute(Dispatchers.IO) {
        val isSuccess = signOutUseCase()
        updateUi {
            if (isSuccess) {
                it.copy(navigateToLogin = true, errorMessage = null)
            } else {
                it.copy(errorMessage = context.getString(R.string.core_resources_settings_sign_out_error))
            }
        }
    }

    private fun consumeSignOutNavigation() = execute {
        updateUi { it.copy(navigateToLogin = false) }
    }

    private fun showDeleteAccountStep(step: DeleteAccountStep) = execute {
        _uiState.update {
            // A running deletion can't be dismissed: it finishes on the server regardless.
            if (it.deleteAccountStep == DeleteAccountStep.InProgress) it
            else it.copy(deleteAccountStep = step, deleteAccountError = null)
        }
    }

    private fun deleteAccount(stepOnFailure: DeleteAccountStep) = execute(Dispatchers.IO) {
        showProgress()
        runDeletion(stepOnFailure)
    }

    private fun reauthenticateWithGoogle(activity: Activity) =
        reauthenticateAndDelete(DeleteAccountStep.ReauthGoogle) {
            reauthenticateWithGoogleUseCase(activity)
        }

    private fun reauthenticateAndDelete(
        stepOnFailure: DeleteAccountStep,
        reauthenticate: suspend () -> FlowResponseNothing
    ) = execute(Dispatchers.IO) {
        showProgress()
        reauthenticate().collect { response ->
            when (response) {
                is ResponseState.EmptySuccess -> runDeletion(stepOnFailure)
                is ResponseState.Error -> showDeleteFailure(
                    step = stepOnFailure,
                    messageRes = if (response.type == ErrorResponseType.FORBIDDEN) {
                        R.string.core_resources_profile_delete_account_wrong_credentials
                    } else {
                        R.string.core_resources_profile_delete_account_error
                    }
                )
                else -> Unit
            }
        }
    }

    private suspend fun runDeletion(stepOnFailure: DeleteAccountStep) {
        deleteCurrentUserUseCase().collect { response ->
            when (response) {
                is ResponseState.EmptySuccess -> onAccountDeleted()
                is ResponseState.Error ->
                    if (response.type == ErrorResponseType.UNAUTHORIZED) {
                        onRecentLoginRequired(stepOnFailure)
                    } else {
                        showDeleteFailure(stepOnFailure, R.string.core_resources_profile_delete_account_error)
                    }
                else -> Unit
            }
        }
    }

    private fun onRecentLoginRequired(stepOnFailure: DeleteAccountStep) {
        when {
            // Already confirmed once and Firebase still refuses: asking again would loop.
            stepOnFailure != DeleteAccountStep.Confirm -> showDeleteFailure(
                stepOnFailure,
                R.string.core_resources_profile_delete_account_relogin_needed
            )
            else -> when (getAccountProviderUseCase()) {
                AccountProvider.Password -> showReauthStep(DeleteAccountStep.ReauthPassword)
                AccountProvider.Google -> showReauthStep(DeleteAccountStep.ReauthGoogle)
                AccountProvider.Other -> showDeleteFailure(
                    stepOnFailure,
                    R.string.core_resources_profile_delete_account_relogin_needed
                )
            }
        }
    }

    private suspend fun onAccountDeleted() {
        // Clears the session flag too; Firebase has already dropped the deleted user.
        signOutUseCase()
        logI("Account deleted. Navigating to login.")
        _uiState.update {
            it.copy(
                deleteAccountStep = DeleteAccountStep.Hidden,
                deleteAccountError = null,
                navigateToLogin = true
            )
        }
        execute { context.printTextInToast(context.getString(R.string.core_resources_profile_delete_account_done)) }
    }

    private fun showProgress() {
        _uiState.update {
            it.copy(deleteAccountStep = DeleteAccountStep.InProgress, deleteAccountError = null)
        }
    }

    private fun showReauthStep(step: DeleteAccountStep) {
        _uiState.update { it.copy(deleteAccountStep = step, deleteAccountError = null) }
    }

    private fun showDeleteFailure(step: DeleteAccountStep, messageRes: Int) {
        val message = context.getString(messageRes)
        _uiState.update { it.copy(deleteAccountStep = step, deleteAccountError = message) }
    }
}

private const val PROFILE_SORT_COLUMN_KEY = "profile.sort.column"
private const val PROFILE_SORT_DIRECTION_KEY = "profile.sort.direction"

private fun readSavedProfileSort(savedStateHandle: SavedStateHandle): ProfileOfflineRecordSort {
    val savedColumn = savedStateHandle.get<String>(PROFILE_SORT_COLUMN_KEY)
    val savedDirection = savedStateHandle.get<String>(PROFILE_SORT_DIRECTION_KEY)

    val column = savedColumn?.let {
        runCatching { ProfileOfflineRecordSortColumn.valueOf(it) }.getOrNull()
    } ?: ProfileOfflineRecordSortColumn.TotalWins

    val direction = savedDirection?.let {
        runCatching { ProfileSortDirection.valueOf(it) }.getOrNull()
    } ?: ProfileSortDirection.Descending

    return ProfileOfflineRecordSort(column = column, direction = direction)
}

private fun persistProfileSort(
    savedStateHandle: SavedStateHandle,
    sort: ProfileOfflineRecordSort
) {
    savedStateHandle[PROFILE_SORT_COLUMN_KEY] = sort.column.name
    savedStateHandle[PROFILE_SORT_DIRECTION_KEY] = sort.direction.name
}

private fun resolveNextProfileSort(
    current: ProfileOfflineRecordSort,
    column: ProfileOfflineRecordSortColumn
): ProfileOfflineRecordSort {
    if (current.column != column) {
        return ProfileOfflineRecordSort(
            column = column,
            direction = if (column == ProfileOfflineRecordSortColumn.Player) {
                ProfileSortDirection.Ascending
            } else {
                ProfileSortDirection.Descending
            }
        )
    }
    val nextDirection = if (current.direction == ProfileSortDirection.Ascending) {
        ProfileSortDirection.Descending
    } else {
        ProfileSortDirection.Ascending
    }
    return current.copy(direction = nextDirection)
}

private fun sortProfileOfflineRows(
    rows: List<OfflineProfileRecordRowUi>,
    sort: ProfileOfflineRecordSort
) = rows
    .sortedWith(compareByDescending<OfflineProfileRecordRowUi> { sortableProfileValue(it, sort.column) }
        .thenBy { it.playerName.lowercase() })
    .let { sortedRows ->
        if (sort.direction == ProfileSortDirection.Descending) {
            sortedRows
        } else {
            sortedRows.reversed()
        }
    }
    .mapIndexed { index, row -> row.copy(position = index + 1) }
    .toImmutableList()

private fun sortableProfileValue(
    row: OfflineProfileRecordRowUi,
    column: ProfileOfflineRecordSortColumn
): Comparable<*> = when (column) {
    ProfileOfflineRecordSortColumn.Position -> row.position
    ProfileOfflineRecordSortColumn.Player -> row.playerName.lowercase()
    ProfileOfflineRecordSortColumn.CivilianWins -> row.civilianWins
    ProfileOfflineRecordSortColumn.ImpostorWins -> row.impostorWins
    ProfileOfflineRecordSortColumn.TotalWins -> row.totalWins
    ProfileOfflineRecordSortColumn.CurrentStreak -> row.currentStreak
}

