package es.sebas1705.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import es.sebas1705.common.theme.ThemeContrast
import es.sebas1705.common.utlis.UiModePreviews
import es.sebas1705.core.resources.R
import es.sebas1705.ui.theme.AppDimens
import es.sebas1705.ui.theme.AppTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ContrastChips(
    selected: ThemeContrast,
    onSelect: (ThemeContrast) -> Unit
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppDimens.spacing.sm)
    ) {
        ThemeContrast.entries.forEach { contrast ->
            FilterChip(
                selected = contrast == selected,
                onClick = { onSelect(contrast) },
                label = {
                    val labelRes = when (contrast) {
                        ThemeContrast.Low -> R.string.core_resources_contrast_low
                        ThemeContrast.Medium -> R.string.core_resources_contrast_medium
                        ThemeContrast.High -> R.string.core_resources_contrast_high
                    }
                    Text(stringResource(labelRes))
                }
            )
        }
    }
}

@UiModePreviews
@Composable
private fun ContrastChipsPreview() {
    AppTheme {
        ContrastChips(selected = ThemeContrast.Medium, onSelect = {})
    }
}
