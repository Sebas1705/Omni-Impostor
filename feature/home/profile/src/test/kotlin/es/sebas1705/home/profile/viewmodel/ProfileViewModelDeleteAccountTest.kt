package es.sebas1705.home.profile.viewmodel

import android.app.Activity
import androidx.lifecycle.SavedStateHandle
import es.sebas1705.authentication.DeleteCurrentUserUseCase
import es.sebas1705.authentication.GetAccountProviderUseCase
import es.sebas1705.authentication.IsGuestUserUseCase
import es.sebas1705.authentication.ReauthenticateWithGoogleUseCase
import es.sebas1705.authentication.ReauthenticateWithPasswordUseCase
import es.sebas1705.authentication.SetSessionExpectedUseCase
import es.sebas1705.authentication.SignOutUseCase
import es.sebas1705.authentication.config.AccountProvider
import es.sebas1705.common.managers.ClassLogData
import es.sebas1705.common.responses.ErrorResponseType
import es.sebas1705.common.responses.ResponseState
import es.sebas1705.core.resources.R
import es.sebas1705.game.settings.ReadGameUseCase
import es.sebas1705.models.GameModel
import es.sebas1705.ranking.ReadOfflineRankingUseCase
import es.sebas1705.repositories.interfaces.IAuthenticationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.stub
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.whenever
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/** Runs on the real Dispatchers.IO, so [awaitState] waits for the expected state. */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ProfileViewModelDeleteAccountTest {

    private val repository: IAuthenticationRepository = mock()
    private val readGame: ReadGameUseCase = mock()
    private val readRanking: ReadOfflineRankingUseCase = mock()
    private val collectors = mutableListOf<Job>()
    private lateinit var vm: ProfileViewModel

    private val context get() = RuntimeEnvironment.getApplication()
    private val state get() = vm.uiState.value

    private val success = flowOf(ResponseState.Loading, ResponseState.EmptySuccess)
    private val recentLoginRequired = flowOf(ResponseState.Loading, error(ErrorResponseType.UNAUTHORIZED))

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        whenever(repository.signOut()).thenReturn(true)
        whenever(readGame.invoke()).thenReturn(flowOf(GameModel()))
        readRanking.stub { onBlocking { invoke() } doReturn emptyList() }
        vm = ProfileViewModel(
            signOutUseCase = SignOutUseCase(repository, SetSessionExpectedUseCase(repository)),
            isGuestUserUseCase = IsGuestUserUseCase(repository),
            getAccountProviderUseCase = GetAccountProviderUseCase(repository),
            deleteCurrentUserUseCase = DeleteCurrentUserUseCase(repository),
            reauthenticateWithPasswordUseCase = ReauthenticateWithPasswordUseCase(repository),
            reauthenticateWithGoogleUseCase = ReauthenticateWithGoogleUseCase(repository),
            readOfflineRankingUseCase = readRanking,
            readGameUseCase = readGame,
            savedStateHandle = SavedStateHandle(),
            context = context,
        )
        collectors += CoroutineScope(Dispatchers.Unconfined).launch { vm.uiState.collect() }
    }

    @After
    fun tearDown() {
        collectors.forEach { it.cancel() }
        Dispatchers.resetMain()
    }

    @Test
    fun `registered users are offered account deletion`() {
        whenever(repository.isAnonymous()).thenReturn(false)

        vm.eventHandler(ProfileIntent.Load)

        awaitState { it.canDeleteAccount }
    }

    @Test
    fun `guests are not offered account deletion`() {
        whenever(repository.isAnonymous()).thenReturn(true)

        vm.eventHandler(ProfileIntent.Load)

        awaitLoaded()
        assertFalse(state.canDeleteAccount)
    }

    @Test
    fun `requesting deletion only asks for confirmation`() {
        vm.eventHandler(ProfileIntent.RequestDeleteAccount)

        awaitState { it.deleteAccountStep == DeleteAccountStep.Confirm }
        verify(repository, never()).deleteCurrentUser()
    }

    @Test
    fun `dismissing the confirmation keeps the account`() {
        vm.eventHandler(ProfileIntent.RequestDeleteAccount)
        awaitState { it.deleteAccountStep == DeleteAccountStep.Confirm }

        vm.eventHandler(ProfileIntent.DismissDeleteAccount)

        awaitState { it.deleteAccountStep == DeleteAccountStep.Hidden }
        verify(repository, never()).deleteCurrentUser()
        verify(repository, never()).signOut()
    }

    @Test
    fun `confirming deletes the account, ends the session and goes to login`() {
        whenever(repository.deleteCurrentUser()).thenReturn(success)

        vm.eventHandler(ProfileIntent.ConfirmDeleteAccount)

        awaitState { it.navigateToLogin }
        assertEquals(DeleteAccountStep.Hidden, state.deleteAccountStep)
        verify(repository).deleteCurrentUser()
        verify(repository).signOut()
        verifyBlocking(repository) { setSessionExpected(false) }
    }

    @Test
    fun `recent login required asks a password user for the password`() {
        whenever(repository.deleteCurrentUser()).thenReturn(recentLoginRequired)
        whenever(repository.getAccountProvider()).thenReturn(AccountProvider.Password)

        vm.eventHandler(ProfileIntent.ConfirmDeleteAccount)

        awaitState { it.deleteAccountStep == DeleteAccountStep.ReauthPassword }
        assertNull(state.deleteAccountError)
        verify(repository, never()).signOut()
    }

    @Test
    fun `recent login required asks a Google user for the Google account`() {
        whenever(repository.deleteCurrentUser()).thenReturn(recentLoginRequired)
        whenever(repository.getAccountProvider()).thenReturn(AccountProvider.Google)

        vm.eventHandler(ProfileIntent.ConfirmDeleteAccount)

        awaitState { it.deleteAccountStep == DeleteAccountStep.ReauthGoogle }
    }

    @Test
    fun `recent login required with no way to reauthenticate explains how to retry`() {
        whenever(repository.deleteCurrentUser()).thenReturn(recentLoginRequired)
        whenever(repository.getAccountProvider()).thenReturn(AccountProvider.Other)

        vm.eventHandler(ProfileIntent.ConfirmDeleteAccount)

        val expected = context.getString(R.string.core_resources_profile_delete_account_relogin_needed)
        awaitState { it.deleteAccountError == expected }
        assertEquals(DeleteAccountStep.Confirm, state.deleteAccountStep)
    }

    @Test
    fun `correct password retries the deletion`() {
        whenever(repository.deleteCurrentUser()).thenReturn(recentLoginRequired, success)
        whenever(repository.getAccountProvider()).thenReturn(AccountProvider.Password)
        whenever(repository.reauthenticateWithPassword("secret1")).thenReturn(success)
        vm.eventHandler(ProfileIntent.ConfirmDeleteAccount)
        awaitState { it.deleteAccountStep == DeleteAccountStep.ReauthPassword }

        vm.eventHandler(ProfileIntent.ReauthenticateWithPassword("secret1"))

        awaitState { it.navigateToLogin }
        verify(repository, times(2)).deleteCurrentUser()
    }

    @Test
    fun `wrong password stays on the step and does not retry the deletion`() {
        whenever(repository.reauthenticateWithPassword("nope"))
            .thenReturn(flowOf(ResponseState.Loading, error(ErrorResponseType.FORBIDDEN)))

        vm.eventHandler(ProfileIntent.ReauthenticateWithPassword("nope"))

        val expected = context.getString(R.string.core_resources_profile_delete_account_wrong_credentials)
        awaitState { it.deleteAccountError == expected }
        assertEquals(DeleteAccountStep.ReauthPassword, state.deleteAccountStep)
        assertFalse(state.navigateToLogin)
        verify(repository, never()).deleteCurrentUser()
    }

    @Test
    fun `any other reauthentication failure shows the generic error`() {
        whenever(repository.reauthenticateWithPassword("secret1"))
            .thenReturn(flowOf(ResponseState.Loading, error(ErrorResponseType.BAD_REQUEST)))

        vm.eventHandler(ProfileIntent.ReauthenticateWithPassword("secret1"))

        val expected = context.getString(R.string.core_resources_profile_delete_account_error)
        awaitState { it.deleteAccountError == expected }
    }

    @Test
    fun `Google reauthentication retries the deletion`() {
        val activity = Robolectric.buildActivity(Activity::class.java).get()
        whenever(repository.deleteCurrentUser()).thenReturn(success)
        repository.stub { onBlocking { reauthenticateWithGoogle(any()) } doReturn success }

        vm.eventHandler(ProfileIntent.ReauthenticateWithGoogle(activity))

        awaitState { it.navigateToLogin }
        verifyBlocking(repository) { reauthenticateWithGoogle(activity) }
        verify(repository).deleteCurrentUser()
    }

    @Test
    fun `a rejection right after reauthenticating does not loop back to the prompt`() {
        whenever(repository.deleteCurrentUser()).thenReturn(recentLoginRequired)
        whenever(repository.reauthenticateWithPassword("secret1")).thenReturn(success)

        vm.eventHandler(ProfileIntent.ReauthenticateWithPassword("secret1"))

        val expected = context.getString(R.string.core_resources_profile_delete_account_relogin_needed)
        awaitState { it.deleteAccountError == expected }
        verify(repository).deleteCurrentUser()
        verify(repository, never()).getAccountProvider()
    }

    @Test
    fun `a failed deletion keeps the session and lets the user retry`() {
        whenever(repository.deleteCurrentUser())
            .thenReturn(flowOf(ResponseState.Loading, error(ErrorResponseType.BAD_REQUEST)))

        vm.eventHandler(ProfileIntent.ConfirmDeleteAccount)

        val expected = context.getString(R.string.core_resources_profile_delete_account_error)
        awaitState { it.deleteAccountError == expected }
        assertEquals(DeleteAccountStep.Confirm, state.deleteAccountStep)
        assertFalse(state.navigateToLogin)
        verify(repository, never()).signOut()
    }

    @Test
    fun `a deletion in progress cannot be dismissed`() {
        val release = CompletableDeferred<Unit>()
        whenever(repository.deleteCurrentUser()).thenReturn(
            flow {
                emit(ResponseState.Loading)
                release.await()
                emit(ResponseState.EmptySuccess)
            }
        )
        vm.eventHandler(ProfileIntent.ConfirmDeleteAccount)
        awaitState { it.deleteAccountStep == DeleteAccountStep.InProgress }

        vm.eventHandler(ProfileIntent.DismissDeleteAccount)

        assertEquals(DeleteAccountStep.InProgress, state.deleteAccountStep)
        release.complete(Unit)
        awaitState { it.navigateToLogin }
        assertTrue(state.deleteAccountStep == DeleteAccountStep.Hidden)
    }

    private fun error(type: ErrorResponseType) =
        ResponseState.Error(mock<ClassLogData>(), type, "raw firebase text") { _, _ -> }

    private fun awaitLoaded() {
        val deadline = System.currentTimeMillis() + TIMEOUT_MS
        while (vm.loading.value || !isLoadDone()) {
            check(System.currentTimeMillis() < deadline) { "Profile did not load" }
            Thread.sleep(POLL_MS)
        }
    }

    private fun isLoadDone(): Boolean = runCatching {
        verifyBlocking(readRanking) { invoke() }
    }.isSuccess

    private fun awaitState(condition: (ProfileState) -> Boolean) {
        val deadline = System.currentTimeMillis() + TIMEOUT_MS
        while (!condition(state)) {
            check(System.currentTimeMillis() < deadline) {
                "State did not reach the expected condition, last=$state"
            }
            Thread.sleep(POLL_MS)
        }
    }

    private companion object {
        const val TIMEOUT_MS = 5_000L
        const val POLL_MS = 5L
    }
}
