package es.sebas1705.datastore.migrations

import androidx.datastore.core.DataMigration
import es.sebas1705.datastore.GamePreferences

/**
 * Writes the default game setup (players, mode, impostors, timer) the first time the store is
 * opened, before the first value is served. Fields the user already has are kept; once
 * `default_set` is true the migration never runs again.
 *
 * Replaces the fire-and-forget write that `GamePreferencesDataSource` launched from `init`.
 *
 * @since 1.2.1
 * @author Sebas1705 29/10/2026
 */
object GameDefaultsMigration : DataMigration<GamePreferences> {

    val DEFAULT_PLAYERS = listOf("Player1", "Player2", "Player3", "Player4")

    // Must match Modes.Classic.name — keep in sync if the enum is renamed
    const val DEFAULT_MODE = "Classic"
    const val DEFAULT_IMPOSTORS = 1
    const val DEFAULT_SHOW_IMPOSTORS_IN_RESULT = true
    const val DEFAULT_DISCUSSION_TIMER_SECONDS = 180

    override suspend fun shouldMigrate(currentData: GamePreferences): Boolean =
        !currentData.defaultSet

    override suspend fun migrate(currentData: GamePreferences): GamePreferences =
        withDefaults(currentData)

    override suspend fun cleanUp() = Unit

    /** [current] unchanged if the defaults were already written, else completed with defaults. */
    fun withDefaults(current: GamePreferences): GamePreferences =
        if (current.defaultSet) current
        else current.toBuilder()
            .apply {
                if (current.playersCount == 0) addAllPlayers(DEFAULT_PLAYERS)
                if (current.mode.isEmpty()) mode = DEFAULT_MODE
                if (current.impostors <= 0) impostors = DEFAULT_IMPOSTORS
                showImpostorsInResult = DEFAULT_SHOW_IMPOSTORS_IN_RESULT
                if (current.discussionTimerSeconds <= 0) {
                    discussionTimerSeconds = DEFAULT_DISCUSSION_TIMER_SECONDS
                }
                setDefaultSet(true)
            }
            .build()
}
