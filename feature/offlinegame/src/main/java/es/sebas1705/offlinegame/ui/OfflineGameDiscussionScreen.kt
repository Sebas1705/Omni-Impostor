@file:Suppress("LongMethod")

package es.sebas1705.offlinegame.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import es.sebas1705.core.resources.R
import es.sebas1705.feature.offlinegame.BuildConfig
import es.sebas1705.offlinegame.models.screenLogD
import es.sebas1705.offlinegame.ui.components.DebugVotesSummary
import es.sebas1705.offlinegame.ui.components.DiscussionFeedbackCard
import es.sebas1705.offlinegame.ui.components.DiscussionSessionChips
import es.sebas1705.offlinegame.ui.components.DiscussionStartPlayerHeader
import es.sebas1705.offlinegame.ui.components.DiscussionTimer
import es.sebas1705.offlinegame.ui.components.GuessWordButton
import es.sebas1705.offlinegame.ui.components.GuessWordInput
import es.sebas1705.offlinegame.ui.components.VotePlayerItem
import es.sebas1705.offlinegame.ui.components.VoteSectionHeader
import es.sebas1705.offlinegame.viewmodel.OfflineGameState
import es.sebas1705.ui.theme.AppDimens

private val LANDSCAPE_BREAKPOINT = 600.dp

@Composable
internal fun OfflineGameDiscussionScreen(
    uiState: OfflineGameState,
    onVotePlayer: (Int) -> Unit,
    onGuess: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val timerSeconds = uiState.discussionTimerSeconds
    val secondsLeft = uiState.timerSecondsLeft
    val enabledAnswer = timerSeconds == 0 || secondsLeft > 0
    var guessValue by rememberSaveable { mutableStateOf("") }
    val haptic = LocalHapticFeedback.current
    val spacing = AppDimens.spacing

    LaunchedEffect(secondsLeft) {
        if (secondsLeft == 30 || secondsLeft == 10) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    LaunchedEffect(uiState.alivePlayerIndexes.size, uiState.guessFeedback) {
        screenLogD(
            "discussion phase alive=${uiState.alivePlayerIndexes.size} " +
                    "feedback=${uiState.guessFeedback.orEmpty()}"
        )
    }

    LaunchedEffect(enabledAnswer) {
        screenLogD("Discussion phase enabledAnswer=$enabledAnswer")
    }

    BoxWithConstraints(modifier = modifier) {
        if (maxWidth >= LANDSCAPE_BREAKPOINT) {
            Row(Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .weight(0.42f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                        .padding(end = spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(spacing.md)
                ) {
                    if (timerSeconds > 0) {
                        DiscussionTimer(
                            secondsLeft = secondsLeft,
                            totalSeconds = timerSeconds
                        )
                    }
                    DiscussionSessionChips(
                        totalPlayers = uiState.players.size,
                        alivePlayers = uiState.alivePlayerIndexes.size,
                        impostorCount = uiState.impostorPlayerIndexes.size
                    )
                    DiscussionStartPlayerHeader(
                        speakerName = uiState.players.getOrElse(uiState.currentSpeakerIndex) {
                            stringResource(R.string.core_resources_unknown)
                        }
                    )
                    if (!uiState.guessFeedback.isNullOrBlank()) {
                        DiscussionFeedbackCard(uiState.guessFeedback)
                    }
                    GuessWordInput(
                        value = guessValue,
                        onValueChange = { guessValue = it },
                        enabled = enabledAnswer
                    )
                    GuessWordButton(
                        onClick = { onGuess(guessValue) },
                        enabled = enabledAnswer
                    )
                    if (BuildConfig.DEBUG) {
                        DebugVotesSummary(
                            correctVotes = uiState.correctVotes,
                            incorrectVotes = uiState.incorrectVotes
                        )
                    }
                }
                Box(
                    Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
                LazyColumn(
                    modifier = Modifier
                        .weight(0.58f)
                        .fillMaxHeight()
                        .padding(start = spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(spacing.md)
                ) {
                    item(contentType = "header") { VoteSectionHeader() }
                    itemsIndexed(
                        items = uiState.players,
                        key = { index, _ -> index },
                        contentType = { _, _ -> "vote_item" }
                    ) { index, playerName ->
                        VotePlayerItem(
                            index = index,
                            playerName = playerName,
                            isAlive = index in uiState.alivePlayerIndexes,
                            isImpostor = index in uiState.impostorPlayerIndexes,
                            onVotePlayer = onVotePlayer
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(spacing.md)
            ) {
                if (timerSeconds > 0) {
                    item(contentType = "contentType0") {
                        DiscussionTimer(secondsLeft = secondsLeft, totalSeconds = timerSeconds)
                    }
                }
                item(contentType = "contentType_chips") {
                    DiscussionSessionChips(
                        totalPlayers = uiState.players.size,
                        alivePlayers = uiState.alivePlayerIndexes.size,
                        impostorCount = uiState.impostorPlayerIndexes.size
                    )
                }
                item(contentType = "contentType1") {
                    DiscussionStartPlayerHeader(
                        speakerName = uiState.players.getOrElse(uiState.currentSpeakerIndex) {
                            stringResource(R.string.core_resources_unknown)
                        }
                    )
                }
                if (!uiState.guessFeedback.isNullOrBlank()) {
                    item(contentType = "contentType2") { DiscussionFeedbackCard(uiState.guessFeedback) }
                }
                item(contentType = "contentType3") {
                    GuessWordInput(
                        value = guessValue,
                        onValueChange = { guessValue = it },
                        enabled = enabledAnswer
                    )
                }
                item(contentType = "contentType4") {
                    GuessWordButton(
                        onClick = { onGuess(guessValue) },
                        enabled = enabledAnswer
                    )
                }
                item(contentType = "contentType5") { VoteSectionHeader() }
                itemsIndexed(
                    items = uiState.players,
                    key = { index, _ -> index },
                    contentType = { _, _ -> "contentType6" }
                ) { index, playerName ->
                    VotePlayerItem(
                        index = index,
                        playerName = playerName,
                        isAlive = index in uiState.alivePlayerIndexes,
                        isImpostor = index in uiState.impostorPlayerIndexes,
                        onVotePlayer = onVotePlayer
                    )
                }
                if (BuildConfig.DEBUG) {
                    item(contentType = "contentType7") {
                        DebugVotesSummary(
                            correctVotes = uiState.correctVotes,
                            incorrectVotes = uiState.incorrectVotes
                        )
                    }
                }
            }
        }
    }
}
