package es.sebas1705.common.mvi

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock

/**
 * [MVIBaseViewModel.onInit] must only run once the subclass is fully constructed: it is the hook the
 * feature ViewModels use to start their first load, and that load reads injected dependencies.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MVIBaseViewModelTest {

    private data class State(val value: String) : MVIBaseState
    private object Intent : MVIBaseIntent

    /** Mirrors the feature ViewModels: a constructor-injected dependency read from [onInit]. */
    private class DependentViewModel(
        context: Context,
        private val dependency: String
    ) : MVIBaseViewModel<State, Intent>(context) {
        var seenOnInit: String? = null
            private set
        var initCalls = 0
            private set

        override fun initState() = State("initial")
        override fun intentHandler(intent: Intent) = Job()

        // Deliberately not calling super.onInit(): the base hook only adds debug logging.
        override fun onInit() {
            initCalls++
            seenOnInit = dependency
        }
    }

    private val context: Context = mock()

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `onInit runs after construction and sees the injected dependency`() = runTest {
        val viewModel = DependentViewModel(context, "injected")

        assertNull(viewModel.seenOnInit)
        advanceUntilIdle()

        assertEquals(1, viewModel.initCalls)
        assertEquals("injected", viewModel.seenOnInit)
    }

    @Test
    fun `onInit runs exactly once`() = runTest {
        val viewModel = DependentViewModel(context, "injected")

        advanceUntilIdle()
        advanceUntilIdle()

        assertEquals(1, viewModel.initCalls)
        assertEquals(State("initial"), viewModel.uiState.value)
    }
}
