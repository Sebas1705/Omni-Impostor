package es.sebas1705.settings.viewmodel

import es.sebas1705.common.mvi.MVIBaseIntent
import es.sebas1705.common.theme.ThemeContrast
import es.sebas1705.models.AppLanguage
import es.sebas1705.models.ClueHelp
import es.sebas1705.models.DarkThemePreference

sealed interface AppSettingsIntent : MVIBaseIntent {
    data object ObserveSettings : AppSettingsIntent
    data class UpdateMusicVolume(val value: Float) : AppSettingsIntent
    data class UpdateSoundVolume(val value: Float) : AppSettingsIntent
    data class UpdateContrast(val contrast: ThemeContrast) : AppSettingsIntent
    data class UpdateCompactTables(val enabled: Boolean) : AppSettingsIntent
    data class SetShowTutorialOnNextStart(val show: Boolean) : AppSettingsIntent
    data class UpdateLanguage(val language: AppLanguage) : AppSettingsIntent
    data class UpdateDarkTheme(val pref: DarkThemePreference) : AppSettingsIntent
    data class UpdateClueHelp(val help: ClueHelp) : AppSettingsIntent
    data object ResetDefaults : AppSettingsIntent
}

