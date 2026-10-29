package es.sebas1705.datastore.migrations

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import es.sebas1705.datastore.SettingsPreferences
import es.sebas1705.datastore.config.DefaultValuesDS
import es.sebas1705.datastore.serializers.SettingsSerializer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

/**
 * The settings defaults are written by a DataStore migration, so the very first read already sees
 * them (the old fire-and-forget write from `init` raced with it) and later launches keep the
 * user's values.
 */
class SettingsDefaultsMigrationTest {

    @get:Rule
    val tmp = TemporaryFolder()

    private val migration = SettingsDefaultsMigration { "es" }

    @Test
    fun `first launch writes the defaults`() = runBlocking {
        val written = migration.migrate(SettingsPreferences.getDefaultInstance())

        assertTrue(written.defaultSet)
        assertEquals(DefaultValuesDS.FIRST_TIME, written.firstTime)
        assertEquals(DefaultValuesDS.MUSIC_VOLUME, written.musicVolume)
        assertEquals(DefaultValuesDS.SOUND_VOLUME, written.soundVolume)
        assertEquals(DefaultValuesDS.APP_UI_CONTRAST, written.contrast)
        assertEquals(DefaultValuesDS.FORCE_COMPACT_TABLES, written.forceCompactTables)
        assertEquals("es", written.appLanguage)
    }

    @Test
    fun `migration only runs while the defaults are missing`() = runBlocking {
        val empty = SettingsPreferences.getDefaultInstance()
        assertTrue(migration.shouldMigrate(empty))
        assertFalse(migration.shouldMigrate(migration.migrate(empty)))
    }

    @Test
    fun `stored settings are left untouched`() {
        var languageResolved = false
        val stored = SettingsPreferences.newBuilder()
            .setDefaultSet(true).setMusicVolume(0.2f).setAppLanguage("en").build()

        val result = SettingsDefaultsMigration.withDefaults(stored) {
            languageResolved = true
            "es"
        }

        assertEquals(stored, result)
        assertFalse(languageResolved)
    }

    @Test
    fun `initial language follows the device locale`() {
        assertEquals("es", SettingsDefaultsMigration.languageFor("es"))
        assertEquals("es", SettingsDefaultsMigration.languageFor("ES"))
        assertEquals(DefaultValuesDS.APP_LANGUAGE, SettingsDefaultsMigration.languageFor("fr"))
        assertEquals(DefaultValuesDS.APP_LANGUAGE, SettingsDefaultsMigration.languageFor(null))
    }

    @Test
    fun `very first read of a fresh store already sees the defaults`() = runBlocking {
        withStore(File(tmp.root, "settings.pb")) { store ->
            val first = store.data.first()
            assertTrue(first.defaultSet)
            assertEquals(DefaultValuesDS.MUSIC_VOLUME, first.musicVolume)
            assertEquals("es", first.appLanguage)
        }
    }

    @Test
    fun `opening an existing store keeps the user's values`() = runBlocking {
        val file = File(tmp.root, "settings.pb")
        // What a previous launch left on disk: defaults written, then changed by the user.
        val stored = migration.migrate(SettingsPreferences.getDefaultInstance())
            .toBuilder().setFirstTime(true).setSoundVolume(0.3f).build()
        file.outputStream().use { stored.writeTo(it) }

        withStore(file) { store ->
            val reread = store.data.first()
            assertTrue(reread.firstTime)
            assertEquals(0.3f, reread.soundVolume)
        }
    }

    private suspend fun withStore(file: File, block: suspend (DataStore<SettingsPreferences>) -> Unit) {
        val job = SupervisorJob()
        try {
            block(
                DataStoreFactory.create(
                    serializer = SettingsSerializer(),
                    migrations = listOf(migration),
                    scope = CoroutineScope(Dispatchers.IO + job),
                    produceFile = { file }
                )
            )
        } finally {
            job.cancelAndJoin()
        }
    }
}
