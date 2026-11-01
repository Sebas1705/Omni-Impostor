package es.sebas1705.main.viewmodel

import es.sebas1705.authentication.IsEmailVerifiedUseCase
import es.sebas1705.authentication.IsGuestUserUseCase
import es.sebas1705.authentication.IsUserLoggedUseCase
import es.sebas1705.authentication.RefreshEmailVerifiedUseCase
import es.sebas1705.authentication.SignOutUseCase
import es.sebas1705.common.managers.ClassLogData
import es.sebas1705.common.responses.ErrorResponseType
import es.sebas1705.common.responses.ResponseState
import es.sebas1705.common.utlis.alias.FlowResponse
import es.sebas1705.settings.ReadSettingsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.stub
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/** The splash must not sign out a user whose link was clicked while the app was closed. */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class MainViewModelEmailCheckTest {

    private val isLogged: IsUserLoggedUseCase = mock()
    private val isGuest: IsGuestUserUseCase = mock()
    private val isEmailVerified: IsEmailVerifiedUseCase = mock()
    private val refreshEmailVerified: RefreshEmailVerifiedUseCase = mock()
    private val signOut: SignOutUseCase = mock()
    private val readSettings: ReadSettingsUseCase = mock()
    private val collectors = mutableListOf<Job>()
    private lateinit var vm: MainViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        isLogged.stub { onBlocking { invoke() } doReturn true }
        whenever(isGuest.invoke()).thenReturn(false)
        whenever(readSettings.invoke()).thenReturn(emptyFlow())
        vm = MainViewModel(
            isUserLoggedUseCase = isLogged,
            isGuestUserUseCase = isGuest,
            isEmailVerifiedUseCase = isEmailVerified,
            refreshEmailVerifiedUseCase = refreshEmailVerified,
            signOutUseCase = signOut,
            readSettingsUseCase = readSettings,
            importDefaultWordsUseCase = mock(),
            mediaPlayerManager = mock(),
            soundPoolManager = mock(),
            context = RuntimeEnvironment.getApplication(),
        )
        collectors += CoroutineScope(Dispatchers.Unconfined).launch { vm.uiState.collect() }
    }

    @After
    fun tearDown() {
        collectors.forEach { it.cancel() }
        Dispatchers.resetMain()
    }

    @Test
    fun `a verified cached user does not wait for the server`() {
        whenever(isEmailVerified.invoke()).thenReturn(true)

        charge()

        assertTrue(vm.uiState.value.isUserLogged)
        verifyNoInteractions(refreshEmailVerified)
    }

    @Test
    fun `a stale cache is corrected by the server and the session is kept`() {
        whenever(isEmailVerified.invoke()).thenReturn(false)
        stubRefresh(flowOf(ResponseState.Loading, ResponseState.Success(true)))

        charge()

        assertTrue(vm.uiState.value.isUserLogged)
        verifyBlocking(signOut, never()) { invoke() }
    }

    @Test
    fun `an unverified user is signed out`() {
        whenever(isEmailVerified.invoke()).thenReturn(false)
        stubRefresh(flowOf(ResponseState.Loading, ResponseState.Success(false)))

        charge()

        assertFalse(vm.uiState.value.isUserLogged)
        verifyBlocking(signOut) { invoke() }
    }

    @Test
    fun `a failed check falls back to login`() {
        whenever(isEmailVerified.invoke()).thenReturn(false)
        stubRefresh(
            flowOf(
                ResponseState.Loading,
                ResponseState.Error(mock<ClassLogData>(), ErrorResponseType.BAD_REQUEST, "offline") { _, _ -> },
            )
        )

        charge()

        assertFalse(vm.uiState.value.isUserLogged)
    }

    @Test
    fun `a hanging check cannot block the splash`() {
        whenever(isEmailVerified.invoke()).thenReturn(false)
        stubRefresh(flow { awaitCancellation() })

        charge()

        assertFalse(vm.uiState.value.isUserLogged)
    }

    @Test
    fun `guests never reach the email check`() {
        whenever(isGuest.invoke()).thenReturn(true)

        charge()

        assertTrue(vm.uiState.value.isUserLogged)
        verifyNoInteractions(isEmailVerified, refreshEmailVerified)
        verify(isGuest).invoke()
    }

    private fun stubRefresh(response: FlowResponse<Boolean>) {
        whenever(refreshEmailVerified.invoke()).thenReturn(response)
    }

    private fun charge() {
        vm.eventHandler(MainIntent.ChargeData)
        val deadline = System.currentTimeMillis() + TIMEOUT_MS
        while (!vm.uiState.value.splashFinished) {
            check(System.currentTimeMillis() < deadline) { "Splash did not finish" }
            Thread.sleep(POLL_MS)
        }
    }

    private companion object {
        const val TIMEOUT_MS = 8_000L
        const val POLL_MS = 5L
    }
}
