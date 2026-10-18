package es.sebas1705.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import es.sebas1705.common.utlis.UiModePreviews
import es.sebas1705.ui.theme.AppDimens
import es.sebas1705.ui.theme.AppTheme
import es.sebas1705.ui.theme.cardShape

/**
 * Standard card: consistent shape, elevation and padding, optional header (icon + title),
 * optional selected state and optional click.
 */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    icon: ImageVector? = null,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val spacing = AppDimens.spacing
    val colors = CardDefaults.cardColors(
        containerColor = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainer
        },
        contentColor = if (selected) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurface
        },
    )
    val elevation = CardDefaults.cardElevation(defaultElevation = AppDimens.elevation.low)
    val border = if (selected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    val body: @Composable ColumnScope.() -> Unit = {
        Column(
            modifier = Modifier.padding(spacing.lg),
            verticalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            if (title != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                ) {
                    if (icon != null) {
                        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(24.dp))
                    }
                    Text(text = title, style = MaterialTheme.typography.titleMedium)
                }
            }
            content()
        }
    }
    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier,
            shape = MaterialTheme.cardShape,
            colors = colors,
            elevation = elevation,
            border = border,
            content = body,
        )
    } else {
        Card(
            modifier = modifier,
            shape = MaterialTheme.cardShape,
            colors = colors,
            elevation = elevation,
            border = border,
            content = body,
        )
    }
}

@UiModePreviews
@Composable
private fun AppCardPreview() {
    AppTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AppCard(title = "Categories", icon = Icons.Outlined.Category) { Text("12 selected") }
            AppCard(title = "Selected", icon = Icons.Outlined.Category, selected = true, onClick = {}) {
                Text("Clickable and selected")
            }
        }
    }
}
