package es.sebas1705.datastore.datasources

import androidx.datastore.core.DataStore
import es.sebas1705.datastore.GamePreferences
import es.sebas1705.datastore.migrations.GameDefaultsMigration
import es.sebas1705.datastore.migrations.GameDefaultsMigration.DEFAULT_IMPOSTORS
import es.sebas1705.datastore.model.GameData
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Game setup stored in Proto DataStore. The defaults are written by [GameDefaultsMigration]
 * (registered in the DataStore factory) before the first value is served.
 */
@Singleton
class GamePreferencesDataSource @Inject constructor(
    private val gamePreferences: DataStore<GamePreferences>
) {

    private val gameData = gamePreferences.data.map {
        GameData(
            selectedCategories = it.selectedCategoriesList,
            players = it.playersList,
            mode = it.mode,
            impostors = it.impostors,
            showImpostorsInResult = it.showImpostorsInResult,
            discussionTimerSeconds = it.discussionTimerSeconds,
            impostorsKnowEachOther = it.impostorsKnowEachOther,
        )
    }

    suspend fun saveSelectedCategories(
        selectedCategories: List<String>
    ) = gamePreferences.updateData {
        it.toBuilder()
            .clearSelectedCategories()
            .addAllSelectedCategories(selectedCategories)
            .build()
    }

    suspend fun savePlayers(
        players: List<String>
    ) = gamePreferences.updateData {
        it.toBuilder()
            .clearPlayers()
            .addAllPlayers(players)
            .build()
    }

    suspend fun saveGameData(
        gameData: GameData
    ) = gamePreferences.updateData {
        it.toBuilder()
            .clearSelectedCategories()
            .addAllSelectedCategories(gameData.selectedCategories)
            .clearPlayers()
            .addAllPlayers(gameData.players)
            .setMode(gameData.mode)
            .setImpostors(gameData.impostors.coerceAtLeast(DEFAULT_IMPOSTORS))
            .setShowImpostorsInResult(gameData.showImpostorsInResult)
            .setDiscussionTimerSeconds(gameData.discussionTimerSeconds.coerceAtLeast(0))
            .setImpostorsKnowEachOther(gameData.impostorsKnowEachOther)
            .build()
    }

    suspend fun saveMode(
        mode: String,
        impostors: Int,
        showImpostorsInResult: Boolean,
        discussionTimerSeconds: Int,
        impostorsKnowEachOther: Boolean,
        showNumOfImpostors: Boolean
    ) = gamePreferences.updateData {
        it.toBuilder()
            .setMode(mode)
            .setImpostors(impostors.coerceAtLeast(DEFAULT_IMPOSTORS))
            .setShowImpostorsInResult(showImpostorsInResult)
            .setDiscussionTimerSeconds(discussionTimerSeconds.coerceAtLeast(0))
            .setImpostorsKnowEachOther(impostorsKnowEachOther)
            .setShowNumOfImpostors(showNumOfImpostors)
            .build()
    }

    fun getGameData() = gameData
}
