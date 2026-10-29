package es.sebas1705.datastore.migrations

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import es.sebas1705.datastore.GamePreferences
import es.sebas1705.datastore.serializers.GameSerializer
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
 * The game setup defaults are written by a DataStore migration before the first read, filling only
 * the fields that are still empty, and never again once `default_set` is true.
 */
class GameDefaultsMigrationTest {

    @get:Rule
    val tmp = TemporaryFolder()

    @Test
    fun `first launch writes the defaults`() = runBlocking {
        val written = GameDefaultsMigration.migrate(GamePreferences.getDefaultInstance())

        assertTrue(written.defaultSet)
        assertEquals(GameDefaultsMigration.DEFAULT_PLAYERS, written.playersList)
        assertEquals(GameDefaultsMigration.DEFAULT_MODE, written.mode)
        assertEquals(GameDefaultsMigration.DEFAULT_IMPOSTORS, written.impostors)
        assertEquals(GameDefaultsMigration.DEFAULT_SHOW_IMPOSTORS_IN_RESULT, written.showImpostorsInResult)
        assertEquals(GameDefaultsMigration.DEFAULT_DISCUSSION_TIMER_SECONDS, written.discussionTimerSeconds)
    }

    @Test
    fun `fields already set are kept when the defaults are written`() {
        val partial = GamePreferences.newBuilder()
            .addAllPlayers(listOf("Ana", "Luis", "Eva"))
            .setMode("Custom")
            .setImpostors(2)
            .setDiscussionTimerSeconds(60)
            .build()

        val written = GameDefaultsMigration.withDefaults(partial)

        assertTrue(written.defaultSet)
        assertEquals(listOf("Ana", "Luis", "Eva"), written.playersList)
        assertEquals("Custom", written.mode)
        assertEquals(2, written.impostors)
        assertEquals(60, written.discussionTimerSeconds)
    }

    @Test
    fun `migration only runs while the defaults are missing`() = runBlocking {
        val stored = GameDefaultsMigration.withDefaults(GamePreferences.getDefaultInstance())
            .toBuilder().clearPlayers().setShowImpostorsInResult(false).build()

        assertFalse(GameDefaultsMigration.shouldMigrate(stored))
        assertEquals(stored, GameDefaultsMigration.withDefaults(stored))
    }

    @Test
    fun `very first read of a fresh store already sees the defaults`() = runBlocking {
        withStore(File(tmp.root, "game.pb")) { store ->
            val first = store.data.first()
            assertTrue(first.defaultSet)
            assertEquals(GameDefaultsMigration.DEFAULT_PLAYERS, first.playersList)
        }
    }

    @Test
    fun `opening an existing store keeps the user's values`() = runBlocking {
        val file = File(tmp.root, "game.pb")
        // What a previous launch left on disk: defaults written, then changed by the user.
        val stored = GameDefaultsMigration.withDefaults(GamePreferences.getDefaultInstance())
            .toBuilder().clearPlayers().addPlayers("Solo").build()
        file.outputStream().use { stored.writeTo(it) }

        withStore(file) { store ->
            assertEquals(listOf("Solo"), store.data.first().playersList)
        }
    }

    private suspend fun withStore(file: File, block: suspend (DataStore<GamePreferences>) -> Unit) {
        val job = SupervisorJob()
        try {
            block(
                DataStoreFactory.create(
                    serializer = GameSerializer(),
                    migrations = listOf(GameDefaultsMigration),
                    scope = CoroutineScope(Dispatchers.IO + job),
                    produceFile = { file }
                )
            )
        } finally {
            job.cancelAndJoin()
        }
    }
}
