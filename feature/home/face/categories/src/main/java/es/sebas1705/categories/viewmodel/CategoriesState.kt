package es.sebas1705.categories.viewmodel

import es.sebas1705.common.mvi.MVIBaseState
import es.sebas1705.models.Categories

data class CategoriesState(
    val draftCategories: Map<Categories, Boolean> = emptyMap()
) : MVIBaseState

