package es.sebas1705.home.nav.viewmodel

import android.content.Context
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import es.sebas1705.common.FaceState
import es.sebas1705.common.mvi.MVIBaseViewModel
import es.sebas1705.common.utlis.extensions.types.logW
import es.sebas1705.game.settings.ReadGameUseCase
import es.sebas1705.models.Categories
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@HiltViewModel
class FaceViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val readGameUseCase: ReadGameUseCase,
) : MVIBaseViewModel<FaceState, FaceIntent>(context) {

    override fun initState(): FaceState = FaceState()

    // Not in onInit(): the base class posts that to Main, so it can run after the first Load has
    // already finished (cached DataStore) and would switch the spinner back on for good.
    init {
        startLoading()
    }

    override fun intentHandler(intent: FaceIntent) =
        when (intent) {
            FaceIntent.Load -> load()
        }

    private fun load() = execute(Dispatchers.IO) {
        runCatching {
            readGameUseCase().collect { game ->
                stopLoading()
                updateUi {
                    it.copy(
                        categoriesStates = Categories.entries.associateWith { category ->
                            game.selectedCategories.contains(category)
                        }.toImmutableMap(),
                        users = game.players.toImmutableList(),
                        mode = game.mode,
                        impostors = game.impostors,
                        showImpostorsInResult = game.showImpostorsInResult,
                        discussionTimerSeconds = game.discussionTimerSeconds,
                        impostorsKnowEachOther = game.impostorsKnowEachOther,
                        showNumOfImpostors = game.showNumOfImpostors,
                        errorMessage = null,
                    )
                }
            }
        }.onFailure { throwable ->
            logW("load failed: ${throwable.message}")
            stopLoading()
            updateUi { it.copy(errorMessage = throwable.message) }
        }
    }
}

