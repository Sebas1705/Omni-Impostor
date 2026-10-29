package es.sebas1705.datastore.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.MultiProcessDataStoreFactory
import androidx.datastore.dataStoreFile
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import es.sebas1705.datastore.GamePreferences
import es.sebas1705.datastore.SettingsPreferences
import es.sebas1705.datastore.migrations.GameDefaultsMigration
import es.sebas1705.datastore.migrations.SettingsDefaultsMigration
import es.sebas1705.datastore.serializers.GAME_PREFERENCES_FILE_NAME
import es.sebas1705.datastore.serializers.GameSerializer
import es.sebas1705.datastore.serializers.SETTINGS_PREFERENCES_FILE_NAME
import es.sebas1705.datastore.serializers.SettingsSerializer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

/**
 * Module to provide
 *
 * @since 0.1.0
 * @author Sebas1705 01/03/2025
 */
@Module
@InstallIn(SingletonComponent::class)
object DatastoreDataModule {

    /**
     * Provides all datastore
     */

    @Provides
    @Singleton
    fun provideSettingsPreferencesDataStore(
        @ApplicationContext context: Context
    ): DataStore<SettingsPreferences> = MultiProcessDataStoreFactory.create(
        serializer = SettingsSerializer(),
        produceFile = { context.dataStoreFile(SETTINGS_PREFERENCES_FILE_NAME) },
        corruptionHandler = null,
        migrations = listOf(
            SettingsDefaultsMigration {
                SettingsDefaultsMigration.languageFor(
                    context.resources.configuration.locales[0]?.language
                )
            }
        ),
        scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    )

    @Provides
    @Singleton
    fun provideGamePreferencesDataStore(
        @ApplicationContext context: Context
    ): DataStore<GamePreferences> = MultiProcessDataStoreFactory.create(
        serializer = GameSerializer(),
        produceFile = { context.dataStoreFile(GAME_PREFERENCES_FILE_NAME) },
        corruptionHandler = null,
        migrations = listOf(GameDefaultsMigration),
        scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    )
}