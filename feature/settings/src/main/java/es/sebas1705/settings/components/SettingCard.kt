package es.sebas1705.settings.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import es.sebas1705.ui.components.AppCard

/** Settings section: a full-width [AppCard] with a titled header. */
@Composable
internal fun SettingCard(
    title: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Outlined.Settings,
    content: @Composable ColumnScope.() -> Unit
) {
    AppCard(
        modifier = modifier.fillMaxWidth(),
        title = title,
        icon = icon,
        content = content
    )
}
