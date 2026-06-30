package es.sebas1705.categories.viewmodel

import es.sebas1705.common.mvi.MVIBaseIntent
import es.sebas1705.models.Categories

sealed interface CategoriesIntent : MVIBaseIntent {
    data class Initialize(val savedCategories: Map<Categories, Boolean>) : CategoriesIntent
    data class ToggleCategory(val category: Categories) : CategoriesIntent
    data object SelectAll : CategoriesIntent
    data object ClearSelection : CategoriesIntent
    data object Save : CategoriesIntent
}

