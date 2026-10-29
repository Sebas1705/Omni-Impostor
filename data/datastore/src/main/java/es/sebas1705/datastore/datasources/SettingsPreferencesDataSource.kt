package es.sebas1705.datastore.datasources

import androidx.datastore.core.DataStore
import es.sebas1705.datastore.SettingsPreferences
import es.sebas1705.datastore.model.SettingsData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Settings stored in Proto DataStore. The defaults (including the initial language from the
 * device locale) are written by [es.sebas1705.datastore.migrations.SettingsDefaultsMigration],
 * registered in the DataStore factory, before the first value is served.
 */
@Singleton
class SettingsPreferencesDataSource @Inject constructor(
    private val settingsPreferences: DataStore<SettingsPreferences>
) {

    /**
     * Settings data
     */

    private val settingsData = settingsPreferences.data.map {
        SettingsData(
            it.firstTime,
            it.musicVolume,
            it.soundVolume,
            it.contrast,
            it.defaultSet,
            it.appLanguage,
            it.forceCompactTables,
            it.darkTheme,
        )
    }

    /**
     * Saves
     */

    /**
     * Update first time value
     *
     * @param firstTime [Boolean]: First time
     *
     * @return [SettingsData]: Updated settings data
     *
     * @since 0.1.0
     * @author Sebas1705 01/03/2025
     */
    suspend fun saveFirstTime(
        firstTime: Boolean
    ) = settingsPreferences.updateData {
        it.toBuilder().setFirstTime(firstTime).build()
    }

    /**
     * Update music volume
     *
     * @param musicVolume [Float]: Music volume
     *
     * @return [SettingsData]: Updated settings data
     *
     * @since 0.1.0
     * @author Sebas1705 01/03/2025
     */
    suspend fun saveMusicVolume(
        musicVolume: Float
    ) = settingsPreferences.updateData {
        it.toBuilder().setMusicVolume(musicVolume).build()
    }

    /**
     * Update sound volume
     *
     * @param soundVolume [Float]: Sound volume
     *
     * @return [SettingsData]: Updated settings data
     *
     * @since 0.1.0
     * @author Sebas1705 01/03/2025
     */
    suspend fun saveSoundVolume(
        soundVolume: Float
    ) = settingsPreferences.updateData {
        it.toBuilder().setSoundVolume(soundVolume).build()
    }

    /**
     * Update app contrast
     *
     * @param contrast [Int]: App contrast
     *
     * @return [SettingsData]: Updated settings data
     *
     * @since 0.1.0
     * @author Sebas1705 01/03/2025
     */
    suspend fun saveAppContrast(
        contrast: Int
    ) = settingsPreferences.updateData {
        it.toBuilder().setContrast(contrast).build()
    }

    /**
     * Save all settings in one transaction to avoid intermediate stale emissions.
     */
    suspend fun saveSettings(
        settingsData: SettingsData
    ) = settingsPreferences.updateData {
        it.toBuilder()
            .setFirstTime(settingsData.firstTime)
            .setMusicVolume(settingsData.musicVolume)
            .setSoundVolume(settingsData.soundVolume)
            .setContrast(settingsData.appContrast)
            .setDefaultSet(settingsData.defaultSet)
            .setAppLanguage(settingsData.appLanguage)
            .setForceCompactTables(settingsData.forceCompactTables)
            .setDarkTheme(settingsData.darkTheme)
            .build()
    }

    suspend fun saveAuthSessionExpected(
        value: Boolean
    ) = settingsPreferences.updateData {
        it.toBuilder()
            .setAuthSessionExpected(value)
            .build()
    }

    fun getAuthSessionExpected(): Flow<Boolean> = settingsPreferences.data.map {
        it.authSessionExpected
    }

    /**
     * Read
     */

    /**
     * Get settings data
     *
     * @return [SettingsData]: Settings data flow
     *
     * @since 0.1.0
     * @author Sebas1705 01/03/2025
     */
    fun getSettingsData() = settingsData

}