package es.sebas1705.settings.design

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import es.sebas1705.common.utlis.UiModePreviews
import es.sebas1705.core.resources.R
import es.sebas1705.core.resources.Sounds
import es.sebas1705.settings.components.AppearanceSettingsCard
import es.sebas1705.settings.components.AudioSettingsCard
import es.sebas1705.settings.components.GameplaySettingsCard
import es.sebas1705.settings.components.SettingsLoading
import es.sebas1705.settings.models.AppSettingsDialogActions
import es.sebas1705.settings.viewmodel.AppSettingsUiState
import es.sebas1705.ui.components.StatusChip
import es.sebas1705.ui.components.StatusChipVariant
import es.sebas1705.ui.sound.LocalSoundPlayer
import es.sebas1705.ui.theme.AppDimens
import es.sebas1705.ui.theme.AppTheme

/** Cards stop growing on tablets so the form stays readable. */
private val ContentMaxWidth = 560.dp

@Composable
private fun SettingsContent(
    paddingValues: PaddingValues,
    uiState: AppSettingsUiState,
    actions: AppSettingsDialogActions
) {
    val sound = LocalSoundPlayer.current
    val spacing = AppDimens.spacing
    val cardModifier = Modifier.widthIn(max = ContentMaxWidth)
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentPadding = PaddingValues(spacing.lg),
        verticalArrangement = Arrangement.spacedBy(spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item(contentType = "audio") {
            AudioSettingsCard(uiState = uiState, actions = actions, modifier = cardModifier)
        }
        item(contentType = "appearance") {
            AppearanceSettingsCard(uiState = uiState, actions = actions, modifier = cardModifier)
        }
        item(contentType = "gameplay") {
            GameplaySettingsCard(uiState = uiState, actions = actions, modifier = cardModifier)
        }
        if (!uiState.errorMessage.isNullOrBlank()) {
            item(contentType = "error") {
                StatusChip(
                    text = uiState.errorMessage,
                    variant = StatusChipVariant.Error,
                    icon = Icons.Outlined.ErrorOutline
                )
            }
        }
        item(contentType = "reset") {
            FilledTonalButton(
                onClick = { sound(Sounds.CLK_INSTANT); actions.onResetDefaults() },
                modifier = cardModifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
            ) {
                Text(stringResource(R.string.core_resources_settings_reset_defaults))
            }
        }
    }
}

@Suppress("ModifierTopMost")
@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun AppSettingsDialogDesign(
    loading: Boolean,
    uiState: AppSettingsUiState,
    actions: AppSettingsDialogActions,
    modifier: Modifier = Modifier,
) {
    val sound = LocalSoundPlayer.current
    Dialog(
        onDismissRequest = actions.onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.core_resources_settings_title)) },
                    navigationIcon = {
                        IconButton(onClick = { sound(Sounds.CLK_TAP); actions.onDismiss() }) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = stringResource(R.string.core_resources_close)
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            if (loading || uiState.settings == null)
                SettingsLoading(paddingValues)
            else SettingsContent(
                paddingValues = paddingValues,
                uiState = uiState,
                actions = actions
            )
        }
    }
}

@UiModePreviews
@Composable
private fun SettingsLoadingPreview() {
    AppTheme {
        SettingsLoading(PaddingValues())
    }
}
