package es.sebas1705.mappers

import es.sebas1705.common.theme.ThemeContrast
import es.sebas1705.datastore.model.SettingsData
import es.sebas1705.models.AppLanguage
import es.sebas1705.models.ClueHelp
import es.sebas1705.models.DarkThemePreference
import es.sebas1705.models.SettingsModel

/**
 * Mapper to convert between [SettingsData] and [SettingsModel].
 *
 * @since 0.1.0
 * @author Sebas1705 01/03/2025
 */
fun SettingsData.toModel() = SettingsModel(
    firstTime = firstTime,
    musicVolume = musicVolume,
    soundVolume = soundVolume,
    appContrast = ThemeContrast.entries[appContrast],
    appLanguage = AppLanguage.fromCode(appLanguage),
    forceCompactTables = forceCompactTables,
    darkThemePreference = DarkThemePreference.fromValue(darkTheme),
    clueHelp = ClueHelp.fromValue(clueHelp),
)

/**
 * Mapper to convert between [SettingsModel] and [SettingsData].
 *
 * @since 0.1.0
 * @author Sebas1705 01/03/2025
 */
fun SettingsModel.toData() = SettingsData(
    firstTime = firstTime,
    musicVolume = musicVolume,
    soundVolume = soundVolume,
    appContrast = appContrast.ordinal,
    defaultSet = true,
    appLanguage = appLanguage.code,
    forceCompactTables = forceCompactTables,
    darkTheme = darkThemePreference.value,
    clueHelp = clueHelp.value,
)