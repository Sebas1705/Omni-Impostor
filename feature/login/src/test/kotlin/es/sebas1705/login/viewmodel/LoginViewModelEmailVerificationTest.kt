package es.sebas1705.login.viewmodel

import es.sebas1705.authentication.IsEmailVerifiedUseCase
import es.sebas1705.authentication.IsUserLoggedUseCase
import es.sebas1705.authentication.RefreshEmailVerifiedUseCase
import es.sebas1705.authentication.SendVerificationEmailUseCase
import es.sebas1705.authentication.SetSessionExpectedUseCase
import es.sebas1705.authentication.SignOutUseCase
import es.sebas1705.authentication.SignUpWithEmailUseCase
import es.sebas1705.common.managers.ClassLogData
import es.sebas1705.common.responses.ErrorResponseType
import es.sebas1705.common.responses.ResponseState
import es.sebas1705.core.resources.R
import es.sebas1705.repositories.interfaces.IAuthenticationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
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
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/**
 * The view model runs on the real Dispatchers.IO, so [awaitState] waits for the expected state.
 * Use cases are real over a mocked repository: "Cancel never deletes" is asserted at that boundary.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class LoginViewModelEmailVerificationTest {

    private val repository: IAuthenticationRepository = mock()
    private val isUserLogged: IsUserLoggedUseCase = mock()
    private val collectors = mutableListOf<Job>()
    private lateinit var vm: LoginViewModel

    private val context get() = RuntimeEnvironment.getApplication()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        whenever(repository.signOut()).thenReturn(true)
        vm = LoginViewModel(
            isUserLoggedUseCase = isUserLogged,
            signInWithGoogleUseCase = mock(),
            signInWithEmailUseCase = mock(),
            signUpWithEmailUseCase = SignUpWithEmailUseCase(repository),
            sendPasswordResetUseCase = mock(),
            signInAsGuestUseCase = mock(),
            sendVerificationEmailUseCase = SendVerificationEmailUseCase(repository),
            isEmailVerifiedUseCase = IsEmailVerifiedUseCase(repository),
            refreshEmailVerifiedUseCase = RefreshEmailVerifiedUseCase(repository),
            setSessionExpectedUseCase = SetSessionExpectedUseCase(repository),
            signOutUseCase = SignOutUseCase(repository, SetSessionExpectedUseCase(repository)),
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
    fun `verified after reload navigates home even though the cached user still says unverified`() {
        whenever(repository.isEmailVerified()).thenReturn(false)
        whenever(repository.refreshEmailVerified())
            .thenReturn(flowOf(ResponseState.Loading, ResponseState.Success(true)))

        vm.eventHandler(LoginIntent.CheckEmailVerified)

        awaitState { it.navigateToHome }
        assertFalse(vm.uiState.value.isWaitingEmailVerification)
        assertNull(vm.uiState.value.errorMessage)
        assertFalse(vm.loading.value)
        verifyBlocking(repository) { setSessionExpected(true) }
    }

    @Test
    fun `still unverified after reload stays on the panel with the not verified message`() {
        whenever(repository.refreshEmailVerified())
            .thenReturn(flowOf(ResponseState.Loading, ResponseState.Success(false)))

        vm.eventHandler(LoginIntent.CheckEmailVerified)

        val expected = context.getString(R.string.core_resources_login_email_not_verified)
        awaitState { it.errorMessage == expected }
        assertFalse(vm.uiState.value.navigateToHome)
    }

    @Test
    fun `a failed reload reports the check failed, not that the email is unverified`() {
        whenever(repository.refreshEmailVerified()).thenReturn(flowOf(ResponseState.Loading, error()))

        vm.eventHandler(LoginIntent.CheckEmailVerified)

        val expected = context.getString(R.string.core_resources_login_verification_check_failed)
        awaitState { it.errorMessage == expected }
        assertFalse(vm.uiState.value.navigateToHome)
        assertFalse(vm.loading.value)
    }

    @Test
    fun `cancel signs out and returns to the main panel without deleting the account`() {
        enterVerificationPanel()

        vm.eventHandler(LoginIntent.CancelEmailVerification)

        awaitState { it.loginMode == LoginMode.Main && !it.isWaitingEmailVerification }
        assertEquals(0, vm.uiState.value.resendCooldownSeconds)
        assertFalse(vm.loading.value)
        verify(repository).signOut()
        verify(repository, never()).deleteCurrentUser()
        verifyBlocking(repository) { setSessionExpected(false) }
    }

    @Test
    fun `recreating the screen on the panel does not let an unverified user into home`() {
        enterVerificationPanel()
        isUserLogged.stub { onBlocking { invoke() } doReturn true }

        vm.eventHandler(LoginIntent.CheckSession).let { job -> awaitJob(job) }

        awaitState { !it.isCheckingSession }
        assertFalse(vm.uiState.value.navigateToHome)
    }

    @Test
    fun `an existing session outside the panel still goes home`() {
        isUserLogged.stub { onBlocking { invoke() } doReturn true }

        vm.eventHandler(LoginIntent.CheckSession)

        awaitState { it.navigateToHome }
        verify(repository, times(0)).signOut()
    }

    private fun enterVerificationPanel() {
        whenever(repository.signUpWithEmail(any(), any()))
            .thenReturn(flowOf(ResponseState.Loading, ResponseState.EmptySuccess))
        whenever(repository.sendVerificationEmail())
            .thenReturn(flowOf(ResponseState.Loading, ResponseState.EmptySuccess))
        vm.eventHandler(LoginIntent.SignUpWithEmail("user@example.com", "secret1"))
        awaitState { it.isWaitingEmailVerification }
    }

    private fun error() = ResponseState.Error(
        mock<ClassLogData>(),
        ErrorResponseType.BAD_REQUEST,
        "network",
    ) { _, _ -> }

    private fun awaitJob(job: Job) {
        val deadline = System.currentTimeMillis() + TIMEOUT_MS
        while (!job.isCompleted) {
            check(System.currentTimeMillis() < deadline) { "Job did not finish" }
            Thread.sleep(POLL_MS)
        }
    }

    private fun awaitState(condition: (LoginState) -> Boolean) {
        val deadline = System.currentTimeMillis() + TIMEOUT_MS
        while (!condition(vm.uiState.value)) {
            check(System.currentTimeMillis() < deadline) {
                "State did not reach the expected condition, last=${vm.uiState.value}"
            }
            Thread.sleep(POLL_MS)
        }
    }

    private companion object {
        const val TIMEOUT_MS = 5_000L
        const val POLL_MS = 5L
    }
}
