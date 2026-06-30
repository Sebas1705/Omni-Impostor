package es.sebas1705.offlinegame.viewmodel

import es.sebas1705.common.mvi.MVIBaseIntent
import es.sebas1705.models.Categories
import es.sebas1705.models.Modes

sealed interface OfflineGameIntent : MVIBaseIntent {
    data class Initialize(
        val players: List<String>,
        val categories: Set<Categories>,
        val mode: Modes,
        val impostors: Int,
        val discussionTimerSeconds: Int,
        val impostorsKnowEachOther: Boolean,
        val showNumOfImpostors: Boolean,
    ) : OfflineGameIntent

    data object MarkRevealDone : OfflineGameIntent
    data object NextRevealPlayer : OfflineGameIntent
    data class VotePlayer(val playerIndex: Int) : OfflineGameIntent
    data class SubmitGuess(val value: String) : OfflineGameIntent
    data object GoToRating : OfflineGameIntent
    data class RateClue(val clueText: String, val isPositive: Boolean) : OfflineGameIntent
    data object SubmitRatings : OfflineGameIntent
}


