package es.sebas1705.common.mvi

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.sebas1705.common.BuildConfig
import es.sebas1705.common.utlis.extensions.composables.printTextInToast
import es.sebas1705.common.utlis.extensions.composables.withAppLocale
import es.sebas1705.common.utlis.extensions.types.logD
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

/**
 * Base class for the MVIBaseViewModel
 *
 * @param S [MVIBaseState]: State of the viewModel
 * @param I [MVIBaseIntent]: Intent of the viewModel
 *
 * @property context [Context]: Application context localized to the language chosen inside the app
 *   (the application context itself keeps the system language), so strings built here match the UI
 * @property initialState [S]: Initial state of the viewModel
 * @property uiState [MutableStateFlow]<[S]>: UiState for the composable
 *
 * @since 0.1.0
 * @author Sebas1705 01/03/2025
 */
@SuppressLint("StaticFieldLeak")
abstract class MVIBaseViewModel<S : MVIBaseState, I : MVIBaseIntent>(
    private val appContext: Context
) : ViewModel() {

    protected val context: Context
        get() = appContext.withAppLocale()

    companion object {
        private val instances = mutableMapOf<Class<*>, Int>()
    }

    private val initialState: S by lazy { initState() }
    protected val _uiState: MutableStateFlow<S> by lazy { MutableStateFlow(initialState) }
    val uiState by lazy {
        _uiState.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            initialState
        )
    }

    private val _loading = MutableStateFlow(false)
    val loading
        get() = _loading.asStateFlow()


    /**
     * Initialization function for the viewModel, called in the init block. It is used to log the number of instances of the viewModel in debug mode and to collect the uiState for logging purposes.
     *
     * @since 0.1.0
     * @author Sebas1705 12/05/2026
     */
    protected open fun onInit() {
        if (BuildConfig.DEBUG) {
            val clazz = this::class.java
            instances[clazz] = (instances[clazz] ?: 0) + 1
            MVIBaseViewModel.logD("ViewModel initialized ${clazz.simpleName} (${instances[clazz]})")
            execute {
                try {
                    uiState.collect {
                        logD("[STATE:${clazz.simpleName}] updated | $it")
                    }
                } catch (e: CancellationException) {
                    throw e // Must re-throw to preserve structured concurrency
                } catch (_: Exception) {
                    // Prevent debug logging from crashing tests or production
                }
            }
        }
    }

    init {
        onInit()
    }

    override fun onCleared() {
        super.onCleared()
        if (BuildConfig.DEBUG) {
            try {
                val clazz = this::class.java
                val count = ((instances[clazz] ?: 1) - 1).coerceAtLeast(0)
                if (count == 0) instances.remove(clazz) else instances[clazz] = count
                MVIBaseViewModel.logD("ViewModel cleared ${clazz.simpleName} ($count)")
            } catch (_: Exception) {
                // Prevent debug logging from crashing tests
            }
        }
    }

    /**
     * Initial state of the viewModel
     *
     * @return [S]
     *
     * @since 0.1.0
     * @author Sebas1705 01/03/2025
     */
    protected abstract fun initState(): S

    /**
     * Handle the intents from the composable
     *
     * @param intent [I]: Intent from the composable
     *
     * @since 0.1.0
     * @author Sebas1705 01/03/2025
     */
    protected abstract fun intentHandler(intent: I): Job

    /**
     * Receive the intents from the composable
     *
     * @param intent [I]: Intent from the composable
     *
     * @since 0.1.0
     * @author Sebas1705 01/03/2025
     */
    fun eventHandler(intent: I): Job {
        if (BuildConfig.DEBUG) {
            try {
                val viewModelName = this::class.java.simpleName
                val intentName = intent::class.simpleName ?: intent::class.java.simpleName
                logD("[INTENT:$viewModelName] $intentName")
            } catch (_: Exception) {
            }
        }
        return intentHandler(intent)
    }

    /**
     * Update the uiState
     *
     * @param handler [suspend] (intent: S) -> S: handler to update the uiState
     *
     * @since 0.1.0
     * @author Sebas1705 01/03/2025
     */
    protected fun updateUi(
        handler: suspend (intent: S) -> S,
    ) = execute {
        _uiState.update { handler(it) }
    }

    /**
     * Execute the actions in the viewModelScope
     *
     * @param dispatcher [CoroutineDispatcher]: dispatcher to execute the action
     * @param action [suspend] (): Unit: action to be executed
     *
     * @since 0.1.0
     * @author Sebas1705 01/03/2025
     */
    protected fun execute(
        dispatcher: CoroutineDispatcher = Dispatchers.Main,
        action: suspend () -> Unit
    ) = viewModelScope.launch(dispatcher) { action() }

    /**
     * Start the loading state
     *
     * @since 0.1.0
     * @author Sebas1705 01/03/2025
     */
    protected fun startLoading() {
        _loading.update { true }
    }

    /**
     * Stop the loading state
     *
     * @since 0.1.0
     * @author Sebas1705 01/03/2025
     */
    protected fun stopLoading() {
        _loading.update { false }
    }

    /**
     * Stop the loading state and show an error
     *
     * @param error [String]: error to show
     * @param onError [(String) -> Unit]: action to be executed when the error is shown
     *
     * @since 0.1.0
     * @author Sebas1705 01/03/2025
     */
    protected fun stopAndError(
        error: String,
        onError: (String) -> Unit = context::printTextInToast
    ) {
        stopLoading()
        execute {
            onError(error)
        }
    }

}

