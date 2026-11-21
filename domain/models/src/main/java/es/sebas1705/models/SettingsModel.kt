package es.sebas1705.models

import es.sebas1705.common.theme.ThemeContrast

/**
 * Represents the settings of the application.
 *
 * @property firstTime Indicates if this is the first time the app is being run.
 * @property musicVolume The volume level for music.
 * @property soundVolume The volume level for sound effects.
 * @property appContrast The contrast setting for the app's theme.
 *
 * @since 0.1.0
 * @author Sebas1705 01/03/2025
 */
data class SettingsModel (
    val firstTime: Boolean,
    val musicVolume: Float,
    val soundVolume: Float,
    val appContrast: ThemeContrast,
    val appLanguage: AppLanguage,
    val forceCompactTables: Boolean,
    val darkThemePreference: DarkThemePreference = DarkThemePreference.System,
    val clueHelp: ClueHelp = ClueHelp.Normal,
)