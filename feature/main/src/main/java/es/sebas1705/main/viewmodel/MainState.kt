package es.sebas1705.main.viewmodel

import es.sebas1705.common.mvi.MVIBaseState
import es.sebas1705.common.theme.ThemeContrast
import es.sebas1705.models.AppLanguage
import es.sebas1705.models.DarkThemePreference

data class MainState(
    val splashFinished: Boolean = false,
    val isUserLogged: Boolean = false,
    val isGuestUser: Boolean = false,
    val soundVolume: Float = 1f,
    val appLanguage: AppLanguage = AppLanguage.English,
    val themeContrast: ThemeContrast = ThemeContrast.Low,
    val forceCompactTables: Boolean = false,
    val darkThemePreference: DarkThemePreference = DarkThemePreference.System,
) : MVIBaseState
