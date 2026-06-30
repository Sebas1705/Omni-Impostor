package es.sebas1705.categories

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import es.sebas1705.categories.design.CategoriesDesign
import es.sebas1705.categories.viewmodel.CategoriesIntent
import es.sebas1705.categories.viewmodel.CategoriesViewModel
import es.sebas1705.models.Categories
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableMap

@Composable
fun CategoriesScreen(
    categoriesStates: ImmutableMap<Categories, Boolean>,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    categoriesViewModel: CategoriesViewModel = hiltViewModel()
) {
    val uiState by categoriesViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(categoriesStates) {
        categoriesViewModel.eventHandler(CategoriesIntent.Initialize(categoriesStates))
    }

    val draftStates = remember(uiState.draftCategories, categoriesStates) {
        uiState.draftCategories.ifEmpty { categoriesStates }.toImmutableMap()
    }

    CategoriesDesign(
        modifier = modifier,
        categoriesStates = draftStates,
        onCategoryClick = { category ->
            categoriesViewModel.eventHandler(CategoriesIntent.ToggleCategory(category))
        },
        onSelectAll = {
            categoriesViewModel.eventHandler(CategoriesIntent.SelectAll)
        },
        onClearSelection = {
            categoriesViewModel.eventHandler(CategoriesIntent.ClearSelection)
        },
        onBack = onBack,
        onDone = {
            categoriesViewModel.eventHandler(CategoriesIntent.Save)
            onBack()
        }
    )
}

@Suppress("ModifierTopMost")
@Composable
fun CategoriesFullScreenDialog(
    onDismiss: () -> Unit,
    categoriesStates: ImmutableMap<Categories, Boolean>,
    modifier: Modifier = Modifier,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        CategoriesScreen(
            categoriesStates = categoriesStates,
            modifier = modifier,
            onBack = onDismiss,
        )
    }
}


