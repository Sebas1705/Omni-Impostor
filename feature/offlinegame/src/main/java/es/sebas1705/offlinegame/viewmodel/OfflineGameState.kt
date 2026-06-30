package es.sebas1705.offlinegame.viewmodel

import es.sebas1705.common.mvi.MVIBaseState
import es.sebas1705.models.WordModel
import es.sebas1705.offlinegame.models.OfflineGameResult
import es.sebas1705.offlinegame.models.OfflineGameStep

data class OfflineGameState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val step: OfflineGameStep = OfflineGameStep.Reveal,
    val players: List<String> = emptyList(),
    val alivePlayerIndexes: Set<Int> = emptySet(),
    val impostorPlayerIndexes: Set<Int> = emptySet(),
    val currentRevealIndex: Int = 0,
    val revealedCurrentCard: Boolean = false,
    val currentSpeakerIndex: Int = 0,
    val wordEntry: WordModel? = null,
    val correctVotes: Int = 0,
    val incorrectVotes: Int = 0,
    val guessFeedback: String? = null,
    val result: OfflineGameResult? = null,
    val discussionTimerSeconds: Int = 180,
    val impostorsKnowEachOther: Boolean = false,
    val showNumOfImpostors: Boolean = false,
    // clue text → thumbs up; only populated for impostors' clues
    val clueRatings: Map<String, Boolean> = emptyMap(),
    val ratingsSubmitted: Boolean = false,
) : MVIBaseState {
    val hasMoreRevealPlayers: Boolean
        get() = currentRevealIndex < players.lastIndex

    val currentPlayerName: String
        get() = players.getOrElse(currentRevealIndex) { "Unknown" }
}


