package es.sebas1705.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import es.sebas1705.common.utlis.UiModePreviews
import es.sebas1705.ui.theme.AppDimens
import es.sebas1705.ui.theme.AppTheme
import es.sebas1705.ui.theme.makeTitle

/** Section title (Finger Paint, primary color) with an optional subtitle. */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(AppDimens.spacing.xs),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.makeTitle(),
            color = MaterialTheme.colorScheme.primary,
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@UiModePreviews
@Composable
private fun SectionHeaderPreview() {
    AppTheme {
        SectionHeader(
            title = "Ranking",
            subtitle = "Who wins the most",
            modifier = Modifier.padding(16.dp),
        )
    }
}
