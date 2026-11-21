package es.sebas1705.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import es.sebas1705.settings.design.AppSettingsDialogDesign
import es.sebas1705.settings.models.AppSettingsDialogActions
import es.sebas1705.settings.viewmodel.AppSettingsIntent
import es.sebas1705.settings.viewmodel.AppSettingsViewModel

@Composable
fun AppSettingsDialog(
    onDismiss: () -> Unit,
    onOpenCategories: () -> Unit,
    onOpenDebugTools: () -> Unit,
    modifier: Modifier = Modifier,
    appSettingsViewModel: AppSettingsViewModel = hiltViewModel()
) {
    val uiState by appSettingsViewModel.uiState.collectAsStateWithLifecycle()
    val loading by appSettingsViewModel.loading.collectAsStateWithLifecycle()

    LaunchedEffect(null) {
        appSettingsViewModel.eventHandler(AppSettingsIntent.ObserveSettings)
    }

    AppSettingsDialogDesign(
        loading = loading,
        modifier = modifier,
        uiState = uiState,
        actions = AppSettingsDialogActions(
            onDismiss = onDismiss,
            onOpenCategories = onOpenCategories,
            onOpenDebugTools = onOpenDebugTools,
            onUpdateMusicVolume = {
                appSettingsViewModel.eventHandler(AppSettingsIntent.UpdateMusicVolume(it))
            },
            onUpdateSoundVolume = {
                appSettingsViewModel.eventHandler(AppSettingsIntent.UpdateSoundVolume(it))
            },
            onUpdateContrast = {
                appSettingsViewModel.eventHandler(AppSettingsIntent.UpdateContrast(it))
            },
            onUpdateCompactTables = {
                appSettingsViewModel.eventHandler(AppSettingsIntent.UpdateCompactTables(it))
            },
            onUpdateLanguage = {
                appSettingsViewModel.eventHandler(AppSettingsIntent.UpdateLanguage(it))
            },
            onUpdateDarkTheme = {
                appSettingsViewModel.eventHandler(AppSettingsIntent.UpdateDarkTheme(it))
            },
            onUpdateClueHelp = {
                appSettingsViewModel.eventHandler(AppSettingsIntent.UpdateClueHelp(it))
            },
            onResetDefaults = {
                appSettingsViewModel.eventHandler(AppSettingsIntent.ResetDefaults)
            }
        )
    )
}
