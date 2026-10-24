@file:Suppress("LongMethod")

package es.sebas1705.offlinegame.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import es.sebas1705.core.resources.R
import es.sebas1705.offlinegame.models.OfflineGameResult
import es.sebas1705.offlinegame.models.OfflineWinner
import es.sebas1705.offlinegame.models.screenLogI
import es.sebas1705.offlinegame.ui.components.EmptyResultCard
import es.sebas1705.offlinegame.ui.components.ResultCloseButton
import es.sebas1705.offlinegame.ui.components.ResultDetailsCard
import es.sebas1705.offlinegame.ui.components.ResultEntrance
import es.sebas1705.offlinegame.ui.components.ResultPlayerRolesCard
import es.sebas1705.offlinegame.ui.components.ResultRateCluesButton
import es.sebas1705.offlinegame.ui.components.ResultVoteAccuracyCard
import es.sebas1705.offlinegame.ui.components.ResultVotesRow
import es.sebas1705.offlinegame.ui.components.ResultWinnerCard
import es.sebas1705.ui.theme.AppDimens

private val LANDSCAPE_BREAKPOINT = 600.dp
private const val STAGGER_STEP_MS = 120

@Composable
internal fun OfflineGameResultScreen(
    result: OfflineGameResult?,
    showImpostorsInResult: Boolean,
    players: List<String>,
    impostorIndexes: Set<Int>,
    onBack: () -> Unit,
    onRateClues: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = AppDimens.spacing

    LaunchedEffect(result) {
        screenLogI(
            if (result == null) "result phase reached with null result"
            else "result phase winner=${result.winner} word='${result.word}' " +
                    "correctVotes=${result.correctVotes} incorrectVotes=${result.incorrectVotes}"
        )
    }

    if (result == null) {
        LazyColumn(modifier = modifier, verticalArrangement = Arrangement.spacedBy(spacing.lg)) {
            item(contentType = "empty") { EmptyResultCard(onBack = onBack) }
        }
        return
    }

    val winnerTitle = when (result.winner) {
        OfflineWinner.Civilians -> stringResource(R.string.core_resources_result_civilians_win)
        OfflineWinner.Impostors -> stringResource(R.string.core_resources_result_impostors_win)
        OfflineWinner.Tie -> stringResource(R.string.core_resources_result_tie)
    }

    BoxWithConstraints(modifier = modifier) {
        if (maxWidth >= LANDSCAPE_BREAKPOINT) {
            Row(Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(end = spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(spacing.lg)
                ) {
                    item(contentType = "winner") {
                        ResultWinnerCard(
                            winner = result.winner,
                            winnerTitle = winnerTitle,
                            reason = result.reason
                        )
                    }
                    item(contentType = "details") {
                        ResultEntrance(delayMillis = STAGGER_STEP_MS) {
                            ResultDetailsCard(
                                word = result.word,
                                impostorNames = result.impostorNames,
                                showImpostors = showImpostorsInResult
                            )
                        }
                    }
                    item(contentType = "votes") {
                        ResultEntrance(delayMillis = STAGGER_STEP_MS * 2) {
                            ResultVotesRow(
                                correctVotes = result.correctVotes,
                                incorrectVotes = result.incorrectVotes
                            )
                        }
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
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(start = spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(spacing.lg)
                ) {
                    item(contentType = "accuracy") {
                        ResultVoteAccuracyCard(
                            correctVotes = result.correctVotes,
                            incorrectVotes = result.incorrectVotes
                        )
                    }
                    if (players.isNotEmpty()) {
                        item(contentType = "roles") {
                            ResultPlayerRolesCard(
                                players = players,
                                impostorIndexes = impostorIndexes,
                                showImpostorsInResult = showImpostorsInResult
                            )
                        }
                    }
                    item(contentType = "rate") { ResultRateCluesButton(onClick = onRateClues) }
                    item(contentType = "close") { ResultCloseButton(onBack = onBack) }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(spacing.lg)
            ) {
                item(contentType = "winner") {
                    ResultWinnerCard(
                        winner = result.winner,
                        winnerTitle = winnerTitle,
                        reason = result.reason
                    )
                }
                item(contentType = "details") {
                    ResultEntrance(delayMillis = STAGGER_STEP_MS) {
                        ResultDetailsCard(
                            word = result.word,
                            impostorNames = result.impostorNames,
                            showImpostors = showImpostorsInResult
                        )
                    }
                }
                item(contentType = "votes") {
                    ResultEntrance(delayMillis = STAGGER_STEP_MS * 2) {
                        ResultVotesRow(
                            correctVotes = result.correctVotes,
                            incorrectVotes = result.incorrectVotes
                        )
                    }
                }
                item(contentType = "accuracy") {
                    ResultVoteAccuracyCard(
                        correctVotes = result.correctVotes,
                        incorrectVotes = result.incorrectVotes
                    )
                }
                if (players.isNotEmpty()) {
                    item(contentType = "roles") {
                        ResultPlayerRolesCard(
                            players = players,
                            impostorIndexes = impostorIndexes,
                            showImpostorsInResult = showImpostorsInResult
                        )
                    }
                }
                item(contentType = "rate") { ResultRateCluesButton(onClick = onRateClues) }
                item(contentType = "close") { ResultCloseButton(onBack = onBack) }
            }
        }
    }
}
