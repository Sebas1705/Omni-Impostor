package es.sebas1705.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import es.sebas1705.core.resources.R
import es.sebas1705.models.DarkThemePreference
import es.sebas1705.settings.models.AppSettingsDialogActions
import es.sebas1705.settings.viewmodel.AppSettingsUiState
import es.sebas1705.ui.theme.AppDimens

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun AppearanceSettingsCard(
    uiState: AppSettingsUiState,
    actions: AppSettingsDialogActions,
    modifier: Modifier = Modifier
) {
    val settings = uiState.settings ?: return

    SettingCard(
        modifier = modifier,
        title = stringResource(R.string.core_resources_settings_appearance_title),
        icon = Icons.Outlined.Palette
    ) {
        Text(
            text = stringResource(R.string.core_resources_settings_ui_contrast),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        ContrastChips(
            selected = settings.appContrast,
            onSelect = actions.onUpdateContrast
        )

        Text(
            text = stringResource(R.string.core_resources_settings_dark_theme),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppDimens.spacing.sm)
        ) {
            DarkThemePreference.entries.forEach { pref ->
                FilterChip(
                    selected = pref == settings.darkThemePreference,
                    onClick = { actions.onUpdateDarkTheme(pref) },
                    label = {
                        val labelRes = when (pref) {
                            DarkThemePreference.System -> R.string.core_resources_settings_theme_system
                            DarkThemePreference.Light -> R.string.core_resources_settings_theme_light
                            DarkThemePreference.Dark -> R.string.core_resources_settings_theme_dark
                        }
                        Text(stringResource(labelRes))
                    }
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .toggleable(
                    value = settings.forceCompactTables,
                    role = Role.Switch,
                    onValueChange = actions.onUpdateCompactTables
                ),
            horizontalArrangement = Arrangement.spacedBy(AppDimens.spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.core_resources_settings_compact_tables),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = settings.forceCompactTables,
                onCheckedChange = null
            )
        }
    }
}
