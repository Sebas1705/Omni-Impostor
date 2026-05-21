package es.sebas1705.categories.design

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import es.sebas1705.categories.components.CategoryCard
import es.sebas1705.common.utlis.UiModePreviews
import es.sebas1705.models.Categories
import es.sebas1705.core.resources.Sounds
import es.sebas1705.ui.sound.LocalSoundPlayer
import es.sebas1705.ui.theme.AppTheme
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import es.sebas1705.core.resources.R as ResourceR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Suppress("LongMethod")
fun CategoriesDesign(
    modifier: Modifier = Modifier,
    categoriesStates: ImmutableMap<Categories, Boolean> = persistentMapOf(),
    onCategoryClick: (Categories) -> Unit = {},
    onSelectAll: () -> Unit = {},
    onClearSelection: () -> Unit = {},
    onBack: () -> Unit = {},
) {
    val sound = LocalSoundPlayer.current
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(ResourceR.string.core_resources_categories_title)) },
                navigationIcon = {
                    IconButton(onClick = { sound(Sounds.CLK_TAP); onBack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(ResourceR.string.core_resources_back)
                        )
                    }
                }
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledTonalButton(
                        onClick = { sound(Sounds.CLK_ARCADE); onSelectAll() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(ResourceR.string.core_resources_categories_select_all))
                    }
                    OutlinedButton(
                        onClick = { sound(Sounds.CLK_INSTANT); onClearSelection() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(ResourceR.string.core_resources_categories_clear))
                    }
                }
                Text(
                    text = stringResource(
                        ResourceR.string.core_resources_categories_selected_summary,
                        categoriesStates.count { it.value },
                        Categories.entries.size
                    ),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    ) { paddingValues ->
        LazyVerticalGrid(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 10.dp),
            columns = GridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = categoriesStates.toList(),
                key = { it.first }
            ) { category ->
                CategoryCard(
                    modifier = Modifier.height(200.dp),
                    category = category.first,
                    isSelected = category.second,
                    onClick = { sound(Sounds.CLK_CASUAL); onCategoryClick(category.first) }
                )
            }
        }
    }
}


@UiModePreviews
@Composable
private fun Preview() {
    AppTheme {
        CategoriesDesign(categoriesStates = persistentMapOf())
    }
}
