package es.sebas1705.home.nav.viewmodel

import es.sebas1705.game.settings.ReadGameUseCase
import es.sebas1705.models.GameModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.setMain
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/**
 * Regression for 1.4.0: after upgrading, the "Jugar" tab showed an endless spinner. The saved game
 * setup was already cached, so the first Load finished before the base ViewModel's posted
 * `onInit()` ran, and `onInit()` then switched the spinner back on for good.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class FaceViewModelLoadingTest {

    private val main = StandardTestDispatcher()
    private val readGame: ReadGameUseCase = mock()
    private val loaded = CountDownLatch(1)

    @Before
    fun setUp() {
        Dispatchers.setMain(main)
        whenever(readGame.invoke()).thenReturn(flow {
            emit(GameModel())
            loaded.countDown()
        })
    }

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun loading_isOff_whenSavedGameLoadsBeforeMainRunsPostedWork() {
        val vm = FaceViewModel(RuntimeEnvironment.getApplication(), readGame)

        // Load runs on IO and finishes while Main has not yet run anything queued by the constructor.
        vm.eventHandler(FaceIntent.Load)
        runBlockingUntilLoaded(vm)

        // Main now drains its queue (the posted onInit and the state update).
        main.scheduler.runCurrent()
        assertFalse("spinner must be off once the game setup is loaded", vm.loading.value)
    }

    private fun runBlockingUntilLoaded(vm: FaceViewModel) {
        check(loaded.await(5, TimeUnit.SECONDS)) { "Load did not finish" }
    }
}
