package es.sebas1705.settings.models

import es.sebas1705.common.theme.ThemeContrast
import es.sebas1705.models.AppLanguage
import es.sebas1705.models.ClueHelp
import es.sebas1705.models.DarkThemePreference

internal data class AppSettingsDialogActions(
    val onDismiss: () -> Unit,
    val onOpenCategories: () -> Unit,
    val onOpenDebugTools: () -> Unit,
    val onUpdateMusicVolume: (Float) -> Unit,
    val onUpdateSoundVolume: (Float) -> Unit,
    val onUpdateContrast: (ThemeContrast) -> Unit,
    val onUpdateCompactTables: (Boolean) -> Unit,
    val onUpdateLanguage: (AppLanguage) -> Unit,
    val onUpdateDarkTheme: (DarkThemePreference) -> Unit,
    val onUpdateClueHelp: (ClueHelp) -> Unit,
    val onResetDefaults: () -> Unit,
)

