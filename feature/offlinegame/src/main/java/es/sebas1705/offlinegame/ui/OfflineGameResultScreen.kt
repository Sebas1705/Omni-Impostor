@file:Suppress("LongMethod")

package es.sebas1705.offlinegame.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
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
import es.sebas1705.offlinegame.ui.components.ResultPlayerRolesCard
import es.sebas1705.offlinegame.ui.components.ResultRateCluesButton
import es.sebas1705.offlinegame.ui.components.ResultVoteAccuracyCard
import es.sebas1705.offlinegame.ui.components.ResultVotesRow
import es.sebas1705.offlinegame.ui.components.ResultWinnerCard

private val LANDSCAPE_BREAKPOINT = 600.dp

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
    val resultImpostors = stringResource(R.string.core_resources_result_impostors)
    val resultImpostorsHidden = stringResource(R.string.core_resources_result_impostors_hidden_by_mode)

    LaunchedEffect(result) {
        screenLogI(
            if (result == null) "result phase reached with null result"
            else "result phase winner=${result.winner} word='${result.word}' " +
                    "correctVotes=${result.correctVotes} incorrectVotes=${result.incorrectVotes}"
        )
    }

    if (result == null) {
        LazyColumn(modifier = modifier, verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item(contentType = "empty") { EmptyResultCard(onBack = onBack) }
        }
        return
    }

    val winnerTitle = when (result.winner) {
        OfflineWinner.Civilians -> stringResource(R.string.core_resources_result_civilians_win)
        OfflineWinner.Impostors -> stringResource(R.string.core_resources_result_impostors_win)
        OfflineWinner.Tie -> stringResource(R.string.core_resources_result_tie)
    }
    val impostorsText = remember(showImpostorsInResult, result) {
        if (showImpostorsInResult)
            String.format(resultImpostors, result.impostorNames.joinToString())
        else resultImpostorsHidden
    }

    BoxWithConstraints(modifier = modifier) {
        if (maxWidth >= LANDSCAPE_BREAKPOINT) {
            Row(Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(end = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item(contentType = "winner") {
                        ResultWinnerCard(
                            winner = result.winner,
                            winnerTitle = winnerTitle,
                            reason = result.reason
                        )
                    }
                    item(contentType = "details") {
                        ResultDetailsCard(word = result.word, impostorsText = impostorsText)
                    }
                    item(contentType = "votes") {
                        ResultVotesRow(
                            correctVotes = result.correctVotes,
                            incorrectVotes = result.incorrectVotes
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
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(start = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
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
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item(contentType = "contentType2") {
                    ResultWinnerCard(
                        winner = result.winner,
                        winnerTitle = winnerTitle,
                        reason = result.reason
                    )
                }
                item(contentType = "contentType3") {
                    ResultDetailsCard(word = result.word, impostorsText = impostorsText)
                }
                item(contentType = "contentType4") {
                    ResultVotesRow(
                        correctVotes = result.correctVotes,
                        incorrectVotes = result.incorrectVotes
                    )
                }
                item(contentType = "contentType_accuracy") {
                    ResultVoteAccuracyCard(
                        correctVotes = result.correctVotes,
                        incorrectVotes = result.incorrectVotes
                    )
                }
                if (players.isNotEmpty()) {
                    item(contentType = "contentType_roles") {
                        ResultPlayerRolesCard(
                            players = players,
                            impostorIndexes = impostorIndexes,
                            showImpostorsInResult = showImpostorsInResult
                        )
                    }
                }
                item(contentType = "contentType5") { Spacer(modifier = Modifier.height(4.dp)) }
                item(contentType = "contentType_rate") { ResultRateCluesButton(onClick = onRateClues) }
                item(contentType = "contentType6") { ResultCloseButton(onBack = onBack) }
            }
        }
    }
}
