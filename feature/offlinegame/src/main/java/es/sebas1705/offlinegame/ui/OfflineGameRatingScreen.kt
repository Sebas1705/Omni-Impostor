@file:Suppress("LongMethod")

package es.sebas1705.offlinegame.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
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
import es.sebas1705.common.utlis.UiModePreviews
import es.sebas1705.core.resources.R
import es.sebas1705.ui.components.AppCard
import es.sebas1705.ui.components.PrimaryActionButton
import es.sebas1705.ui.components.SectionHeader
import es.sebas1705.ui.theme.AppDimens
import es.sebas1705.ui.theme.AppTheme
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

    val spacing = AppDimens.spacing
    val word = uiState.wordEntry?.word.orEmpty()
    val clues = uiState.clueRatings.keys.toList()

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SectionHeader(
            title = stringResource(R.string.core_resources_clue_rating_title),
            subtitle = stringResource(R.string.core_resources_clue_rating_word, word),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(spacing.lg))

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
                verticalArrangement = Arrangement.spacedBy(spacing.md)
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

        Spacer(Modifier.height(spacing.lg))

        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(32.dp))
        } else {
            PrimaryActionButton(
                text = stringResource(R.string.core_resources_clue_rating_submit),
                onClick = onSubmit
            )
            Spacer(Modifier.height(spacing.sm))
            OutlinedButton(
                onClick = onSkip,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
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
    val spacing = AppDimens.spacing
    val scheme = MaterialTheme.colorScheme
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.md)
        ) {
            Text(
                text = clue,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
                FilledTonalIconButton(
                    onClick = { onRate(true) },
                    modifier = Modifier.size(48.dp),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = if (isPositive) scheme.secondaryContainer
                        else scheme.surfaceContainerHighest,
                        contentColor = if (isPositive) scheme.onSecondaryContainer
                        else scheme.onSurfaceVariant
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ThumbUp,
                        contentDescription = stringResource(R.string.core_resources_phase2_rate_good)
                    )
                }
                FilledTonalIconButton(
                    onClick = { onRate(false) },
                    modifier = Modifier.size(48.dp),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = if (!isPositive) scheme.errorContainer
                        else scheme.surfaceContainerHighest,
                        contentColor = if (!isPositive) scheme.onErrorContainer
                        else scheme.onSurfaceVariant
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ThumbDown,
                        contentDescription = stringResource(R.string.core_resources_phase2_rate_bad)
                    )
                }
            }
        }
    }
}

@UiModePreviews
@Composable
private fun OfflineGameRatingScreenPreview() {
    AppTheme {
        OfflineGameRatingScreen(
            uiState = OfflineGameState(
                clueRatings = mapOf("Hot" to true, "Mountain" to false)
            ),
            onRateClue = { _, _ -> },
            onSubmit = {},
            onSkip = {},
            onDone = {}
        )
    }
}
