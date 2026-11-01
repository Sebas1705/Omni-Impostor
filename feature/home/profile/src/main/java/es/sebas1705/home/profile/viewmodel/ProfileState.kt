package es.sebas1705.home.profile.viewmodel

import es.sebas1705.common.mvi.MVIBaseState
import es.sebas1705.home.profile.models.OfflineProfileRecordRowUi
import es.sebas1705.models.Categories
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class ProfileState(
    val navigateToLogin: Boolean = false,
    val errorMessage: String? = null,
    val selectedTab: ProfileTab = ProfileTab.OfflineRecord,
    val offlineRecordRows: ImmutableList<OfflineProfileRecordRowUi> = persistentListOf(),
    val offlineRecordSort: ProfileOfflineRecordSort = ProfileOfflineRecordSort(),
    val rolePreference: ProfileRolePreference = ProfileRolePreference.ImpostorHunter,
    val favoriteCategory: Categories? = null,
    val matchesPlayed: Int = 0,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val canDeleteAccount: Boolean = false,
    val deleteAccountStep: DeleteAccountStep = DeleteAccountStep.Hidden,
    val deleteAccountError: String? = null
) : MVIBaseState

/** Dialog shown by the delete-account flow. */
enum class DeleteAccountStep {
    Hidden,
    Confirm,
    ReauthPassword,
    ReauthGoogle,
    InProgress
}

data class ProfileOfflineRecordSort(
    val column: ProfileOfflineRecordSortColumn = ProfileOfflineRecordSortColumn.TotalWins,
    val direction: ProfileSortDirection = ProfileSortDirection.Descending
)

enum class ProfileOfflineRecordSortColumn {
    Position,
    Player,
    CivilianWins,
    ImpostorWins,
    TotalWins,
    CurrentStreak
}

enum class ProfileSortDirection {
    Ascending,
    Descending
}

