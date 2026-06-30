package es.sebas1705.offlinegame.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import es.sebas1705.core.resources.R
import es.sebas1705.offlinegame.viewmodel.OfflineGameState

@Composable
internal fun OfflineGameRatingScreen(
    uiState: OfflineGameState,
    onRateClue: (String, Boolean) -> Unit,
    onSubmit: () -> Unit,
    onSkip: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(uiState.ratingsSubmitted) {
        if (uiState.ratingsSubmitted) onDone()
    }

    val word = uiState.wordEntry?.word.orEmpty()
    val clues = uiState.clueRatings.keys.toList()

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.core_resources_clue_rating_title),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.core_resources_clue_rating_word, word),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))

        if (clues.isEmpty()) {
            Text(
                text = stringResource(R.string.core_resources_clue_rating_no_clues),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(clues, key = { it }) { clue ->
                    val isPositive = uiState.clueRatings[clue] ?: true
                    ClueRatingCard(
                        clue = clue,
                        isPositive = isPositive,
                        onRate = { positive -> onRateClue(clue, positive) }
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(32.dp))
        } else {
            Button(
                onClick = onSubmit,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.core_resources_clue_rating_submit))
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = onSkip,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.core_resources_clue_rating_skip))
            }
        }
    }
}

@Composable
private fun ClueRatingCard(
    clue: String,
    isPositive: Boolean,
    onRate: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = clue,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilledTonalIconButton(
                    onClick = { onRate(true) },
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = if (isPositive) MaterialTheme.colorScheme.primaryContainer
                                         else MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                ) {
                    Icon(Icons.Outlined.ThumbUp, contentDescription = null)
                }
                FilledTonalIconButton(
                    onClick = { onRate(false) },
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = if (!isPositive) MaterialTheme.colorScheme.errorContainer
                                          else MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                ) {
                    Icon(Icons.Outlined.ThumbDown, contentDescription = null)
                }
            }
        }
    }
}
