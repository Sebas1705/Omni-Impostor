package es.sebas1705.datastore.model

data class SettingsData(
    val firstTime: Boolean,
    val musicVolume: Float,
    val soundVolume: Float,
    val appContrast: Int,
    val defaultSet: Boolean,
    val appLanguage: String,
    val forceCompactTables: Boolean,
    val darkTheme: Int = 0,
    val clueHelp: Int = 0,
)
