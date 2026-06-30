package es.sebas1705.categories.viewmodel

import android.content.Context
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import es.sebas1705.common.mvi.MVIBaseViewModel
import es.sebas1705.common.utlis.extensions.types.logW
import es.sebas1705.game.settings.UpdateGameSelectedCategoriesUseCase
import es.sebas1705.models.Categories
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val updateGameSelectedCategoriesUseCase: UpdateGameSelectedCategoriesUseCase
) : MVIBaseViewModel<CategoriesState, CategoriesIntent>(context) {

    override fun initState(): CategoriesState = CategoriesState()

    override fun intentHandler(intent: CategoriesIntent) =
        when (intent) {
            is CategoriesIntent.Initialize -> initialize(intent.savedCategories)
            is CategoriesIntent.ToggleCategory -> toggleCategory(intent.category)
            CategoriesIntent.SelectAll -> selectAll()
            CategoriesIntent.ClearSelection -> clearAll()
            CategoriesIntent.Save -> save()
        }

    private fun initialize(saved: Map<Categories, Boolean>) = updateUi {
        it.copy(draftCategories = saved)
    }

    private fun toggleCategory(category: Categories) = updateUi { state ->
        val newDraft = state.draftCategories.toMutableMap()
        newDraft[category] = !(newDraft[category] ?: false)
        state.copy(draftCategories = newDraft)
    }

    private fun selectAll() = updateUi { state ->
        state.copy(draftCategories = Categories.entries.associateWith { true })
    }

    private fun clearAll() = updateUi { state ->
        state.copy(draftCategories = Categories.entries.associateWith { false })
    }

    private fun save() = execute(Dispatchers.IO) {
        val draft = _uiState.value.draftCategories
        runCatching {
            updateGameSelectedCategoriesUseCase(
                selectedCategories = draft.filterValues { it }.keys.toSet()
            )
        }.onFailure { logW("save categories failed: ${it.message}") }
    }
}
