package es.sebas1705.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import es.sebas1705.common.utlis.UiModePreviews
import es.sebas1705.ui.theme.AppDimens
import es.sebas1705.ui.theme.AppTheme

/**
 * Small read-only status pill. Colors come from the container/onContainer pairs
 * that `ContrastTest` guarantees to meet WCAG AA.
 */
@Composable
fun StatusChip(
    text: String,
    modifier: Modifier = Modifier,
    variant: StatusChipVariant = StatusChipVariant.Ready,
    icon: ImageVector? = null,
) {
    val scheme = MaterialTheme.colorScheme
    val (container, content) = when (variant) {
        StatusChipVariant.Ready -> scheme.secondaryContainer to scheme.onSecondaryContainer
        StatusChipVariant.Warning -> scheme.tertiaryContainer to scheme.onTertiaryContainer
        StatusChipVariant.Error -> scheme.errorContainer to scheme.onErrorContainer
    }
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = container,
        contentColor = content,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = AppDimens.spacing.md, vertical = AppDimens.spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppDimens.spacing.xs),
        ) {
            if (icon != null) {
                Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp))
            }
            Text(text = text, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@UiModePreviews
@Composable
private fun StatusChipPreview() {
    AppTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            StatusChip("4 players", icon = Icons.Outlined.CheckCircle)
            StatusChip("Select a category", variant = StatusChipVariant.Warning, icon = Icons.Outlined.Warning)
            StatusChip("Not enough players", variant = StatusChipVariant.Error)
        }
    }
}
