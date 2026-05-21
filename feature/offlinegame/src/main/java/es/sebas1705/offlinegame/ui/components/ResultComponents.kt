package es.sebas1705.offlinegame.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Balance
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.TheaterComedy
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import es.sebas1705.core.resources.R
import es.sebas1705.offlinegame.models.OfflineWinner
import es.sebas1705.core.resources.Sounds
import es.sebas1705.ui.sound.LocalSoundPlayer

@Composable
internal fun EmptyResultCard(onBack: () -> Unit) {
    val sound = LocalSoundPlayer.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = stringResource(R.string.core_resources_no_result_available),
                style = MaterialTheme.typography.titleMedium
            )
            FilledTonalButton(onClick = { sound(Sounds.CLK_TAP); onBack() }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.core_resources_back))
            }
        }
    }
}

@Composable
internal fun ResultWinnerCard(winner: OfflineWinner, winnerTitle: String, reason: String) {
    val containerColor = when (winner) {
        OfflineWinner.Civilians -> MaterialTheme.colorScheme.primaryContainer
        OfflineWinner.Impostors -> MaterialTheme.colorScheme.errorContainer
        OfflineWinner.Tie -> MaterialTheme.colorScheme.secondaryContainer
    }
    val contentColor = when (winner) {
        OfflineWinner.Civilians -> MaterialTheme.colorScheme.onPrimaryContainer
        OfflineWinner.Impostors -> MaterialTheme.colorScheme.onErrorContainer
        OfflineWinner.Tie -> MaterialTheme.colorScheme.onSecondaryContainer
    }
    val icon = when (winner) {
        OfflineWinner.Civilians -> Icons.Outlined.EmojiEvents
        OfflineWinner.Impostors -> Icons.Outlined.TheaterComedy
        OfflineWinner.Tie -> Icons.Outlined.Balance
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(40.dp)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = winnerTitle,
                style = MaterialTheme.typography.displaySmall,
                color = contentColor
            )
            Text(
                text = reason,
                style = MaterialTheme.typography.bodyLarge,
                color = contentColor
            )
        }
    }
}

@Composable
internal fun ResultDetailsCard(word: String, impostorsText: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = stringResource(R.string.core_resources_result_secret_word, word),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = impostorsText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
internal fun ResultVotesRow(correctVotes: Int, incorrectVotes: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ResultVotesCard(
            votes = correctVotes,
            label = stringResource(R.string.core_resources_votes_correct_label),
            modifier = Modifier.weight(1f)
        )
        ResultVotesCard(
            votes = incorrectVotes,
            label = stringResource(R.string.core_resources_votes_incorrect_label),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ResultVotesCard(votes: Int, label: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = "$votes", style = MaterialTheme.typography.headlineSmall)
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ResultPlayerRolesCard(
    players: List<String>,
    impostorIndexes: Set<Int>,
    showImpostorsInResult: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Outlined.People,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = stringResource(R.string.core_resources_result_player_roles),
                    style = MaterialTheme.typography.titleMedium
                )
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                players.forEachIndexed { index, name ->
                    val isImpostor = index in impostorIndexes
                    val showRole = showImpostorsInResult || !isImpostor
                    SuggestionChip(
                        onClick = {},
                        label = { Text(name) },
                        icon = {
                            Icon(
                                imageVector = if (isImpostor) Icons.Outlined.TheaterComedy
                                else Icons.Outlined.People,
                                contentDescription = null,
                                modifier = Modifier.size(SuggestionChipDefaults.IconSize)
                            )
                        },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = when {
                                !showRole -> MaterialTheme.colorScheme.surfaceContainerHigh
                                isImpostor -> MaterialTheme.colorScheme.errorContainer
                                else -> MaterialTheme.colorScheme.primaryContainer
                            }
                        )
                    )
                }
            }
        }
    }
}

@Composable
internal fun ResultVoteAccuracyCard(correctVotes: Int, incorrectVotes: Int) {
    val total = correctVotes + incorrectVotes
    if (total == 0) return
    val accuracy = correctVotes.toFloat() / total.toFloat()
    val accuracyPct = (accuracy * 100).toInt()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.core_resources_result_vote_accuracy),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$accuracyPct%",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (accuracy >= 0.5f) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.error
                )
            }
            LinearProgressIndicator(
                progress = { accuracy },
                modifier = Modifier.fillMaxWidth(),
                color = if (accuracy >= 0.5f) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.error,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )
        }
    }
}

@Composable
internal fun ResultCloseButton(onBack: () -> Unit) {
    val sound = LocalSoundPlayer.current
    FilledTonalButton(onClick = { sound(Sounds.CLK_TAP); onBack() }, modifier = Modifier
        .fillMaxWidth()
        .heightIn(min = 48.dp)) {
        Text(stringResource(R.string.core_resources_close))
    }
}

