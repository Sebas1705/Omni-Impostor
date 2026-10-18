package es.sebas1705.settings.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import es.sebas1705.core.resources.R
import es.sebas1705.settings.models.AppSettingsDialogActions
import es.sebas1705.settings.viewmodel.AppSettingsUiState
import kotlin.math.roundToInt

@Composable
internal fun AudioSettingsCard(
    uiState: AppSettingsUiState,
    actions: AppSettingsDialogActions,
    modifier: Modifier = Modifier
) {
    val settings = uiState.settings ?: return

    SettingCard(
        modifier = modifier,
        title = stringResource(R.string.core_resources_settings_audio_title),
        icon = Icons.Outlined.VolumeUp
    ) {
        Text(
            text = stringResource(
                R.string.core_resources_settings_music_percent,
                (settings.musicVolume * 100f).roundToInt()
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Slider(
            value = settings.musicVolume,
            onValueChange = actions.onUpdateMusicVolume,
            valueRange = 0f..1f
        )
        Text(
            text = stringResource(
                R.string.core_resources_settings_effects_percent,
                (settings.soundVolume * 100f).roundToInt()
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Slider(
            value = settings.soundVolume,
            onValueChange = actions.onUpdateSoundVolume,
            valueRange = 0f..1f
        )
    }
}
