package es.sebas1705.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import es.sebas1705.core.resources.R
import es.sebas1705.models.AppLanguage
import es.sebas1705.settings.models.AppSettingsDialogActions
import es.sebas1705.settings.viewmodel.AppSettingsUiState
import es.sebas1705.ui.theme.AppDimens

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun GameplaySettingsCard(
    uiState: AppSettingsUiState,
    actions: AppSettingsDialogActions,
    modifier: Modifier = Modifier
) {
    val settings = uiState.settings ?: return

    SettingCard(
        modifier = modifier,
        title = stringResource(R.string.core_resources_settings_gameplay_title),
        icon = Icons.Outlined.Language
    ) {
        Text(
            text = stringResource(R.string.core_resources_settings_language),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppDimens.spacing.sm)
        ) {
            AppLanguage.entries.forEach { language ->
                FilterChip(
                    selected = settings.appLanguage == language,
                    onClick = { actions.onUpdateLanguage(language) },
                    label = {
                        Text(
                            text = when (language) {
                                AppLanguage.English -> stringResource(R.string.core_resources_language_english)
                                AppLanguage.Spanish -> stringResource(R.string.core_resources_language_spanish)
                            }
                        )
                    }
                )
            }
        }
    }
}
