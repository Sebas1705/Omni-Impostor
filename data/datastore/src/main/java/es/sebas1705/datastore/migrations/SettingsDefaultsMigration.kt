package es.sebas1705.datastore.migrations

import androidx.datastore.core.DataMigration
import es.sebas1705.datastore.SettingsPreferences
import es.sebas1705.datastore.config.DefaultValuesDS

/**
 * Writes the default settings the first time the store is opened.
 *
 * DataStore runs migrations before it serves the first value, so no reader can observe the
 * un-initialised proto (volumes at 0, empty language, `defaultSet = false`). The previous approach
 * launched the write from the data source's `init` in a detached `CoroutineScope(Dispatchers.IO)`,
 * which raced with the first read and swallowed any failure.
 *
 * Once `defaultSet` is true the migration is skipped, so values the user changed are kept.
 *
 * @param initialLanguage Resolves the language to start with (the device locale); only called
 * when the defaults are actually written.
 *
 * @since 1.2.1
 * @author Sebas1705 29/10/2026
 */
class SettingsDefaultsMigration(
    private val initialLanguage: () -> String
) : DataMigration<SettingsPreferences> {

    override suspend fun shouldMigrate(currentData: SettingsPreferences): Boolean =
        !currentData.defaultSet

    override suspend fun migrate(currentData: SettingsPreferences): SettingsPreferences =
        withDefaults(currentData, initialLanguage)

    override suspend fun cleanUp() = Unit

    companion object {
        /** Language used on first launch for a device [localeLanguage]: Spanish or English. */
        fun languageFor(localeLanguage: String?): String =
            if (localeLanguage.equals("es", ignoreCase = true)) "es" else DefaultValuesDS.APP_LANGUAGE

        /** [current] unchanged if the defaults were already written, else the defaults. */
        fun withDefaults(
            current: SettingsPreferences,
            initialLanguage: () -> String
        ): SettingsPreferences =
            if (current.defaultSet) current
            else current.toBuilder()
                .setContrast(DefaultValuesDS.APP_UI_CONTRAST)
                .setFirstTime(DefaultValuesDS.FIRST_TIME)
                .setMusicVolume(DefaultValuesDS.MUSIC_VOLUME)
                .setSoundVolume(DefaultValuesDS.SOUND_VOLUME)
                .setAppLanguage(initialLanguage())
                .setForceCompactTables(DefaultValuesDS.FORCE_COMPACT_TABLES)
                .setDefaultSet(true)
                .build()
    }
}
