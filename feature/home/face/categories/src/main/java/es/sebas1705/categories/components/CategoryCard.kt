package es.sebas1705.categories.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import es.sebas1705.categories.models.toCategoryIcon
import es.sebas1705.common.utlis.UiModePreviews
import es.sebas1705.models.Categories
import es.sebas1705.models.descriptionRes
import es.sebas1705.models.nameRes
import es.sebas1705.ui.components.AppCard
import es.sebas1705.ui.theme.AppDimens
import es.sebas1705.ui.theme.AppTheme

@Composable
internal fun CategoryCard(
    category: Categories,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AppCard(
        modifier = modifier.semantics { selected = isSelected },
        selected = isSelected,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = category.toCategoryIcon(),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Icon(
                imageVector = if (isSelected) {
                    Icons.Outlined.CheckCircle
                } else {
                    Icons.Outlined.RadioButtonUnchecked
                },
                contentDescription = null,
                tint = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(AppDimens.spacing.xs)) {
            Text(
                text = stringResource(category.nameRes),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = stringResource(category.descriptionRes),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@UiModePreviews
@Composable
private fun CategoryCardPreview() {
    AppTheme {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(AppDimens.spacing.sm)
        ) {
            CategoryCard(category = Categories.entries.first(), isSelected = true, onClick = {})
            CategoryCard(category = Categories.entries.last(), isSelected = false, onClick = {})
        }
    }
}
