@file:Suppress("LongMethod", "TooManyFunctions")

package es.sebas1705.offlinegame.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Balance
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.HowToVote
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.TheaterComedy
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import es.sebas1705.common.utlis.UiModePreviews
import es.sebas1705.core.resources.R
import es.sebas1705.core.resources.Sounds
import es.sebas1705.offlinegame.models.OfflineWinner
import es.sebas1705.ui.components.AppCard
import es.sebas1705.ui.components.PrimaryActionButton
import es.sebas1705.ui.components.StatusChip
import es.sebas1705.ui.components.StatusChipVariant
import es.sebas1705.ui.sound.LocalSoundPlayer
import es.sebas1705.ui.theme.AppDimens
import es.sebas1705.ui.theme.AppTheme
import es.sebas1705.ui.theme.cardShape
import es.sebas1705.ui.theme.makeTitle

private const val ENTRANCE_BASE_DELAY_MS = 120
private const val ENTRANCE_DURATION_MS = 400
private const val ICON_POP_INITIAL_SCALE = 0.4f
private const val VOTE_ACCURACY_MIN = 0.5f

/**
 * Plays a one-shot fade + slide entrance. The flag is saveable, so rotating the device
 * does not replay the animation.
 */
@Composable
internal fun ResultEntrance(
    delayMillis: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var visible by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(tween(ENTRANCE_DURATION_MS, ENTRANCE_BASE_DELAY_MS + delayMillis)) +
            slideInVertically(tween(ENTRANCE_DURATION_MS, ENTRANCE_BASE_DELAY_MS + delayMillis)) { it / 4 }
    ) {
        content()
    }
}

@Composable
internal fun EmptyResultCard(onBack: () -> Unit) {
    val sound = LocalSoundPlayer.current
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.core_resources_no_result_available),
            style = MaterialTheme.typography.titleMedium
        )
        FilledTonalButton(
            onClick = { sound(Sounds.CLK_TAP); onBack() },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
        ) {
            Text(stringResource(R.string.core_resources_back))
        }
    }
}

/** Large winner header: solid emphasis color per winner, big pop-in icon, title and reason. */
@Composable
internal fun ResultWinnerCard(
    winner: OfflineWinner,
    winnerTitle: String,
    reason: String,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val containerColor = when (winner) {
        OfflineWinner.Civilians -> scheme.secondary
        OfflineWinner.Impostors -> scheme.error
        OfflineWinner.Tie -> scheme.tertiary
    }
    val contentColor = when (winner) {
        OfflineWinner.Civilians -> scheme.onSecondary
        OfflineWinner.Impostors -> scheme.onError
        OfflineWinner.Tie -> scheme.onTertiary
    }
    val icon = when (winner) {
        OfflineWinner.Civilians -> Icons.Outlined.EmojiEvents
        OfflineWinner.Impostors -> Icons.Outlined.TheaterComedy
        OfflineWinner.Tie -> Icons.Outlined.Balance
    }

    var popped by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) { popped = true }
    val iconScale by animateFloatAsState(
        targetValue = if (popped) 1f else ICON_POP_INITIAL_SCALE,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "winner_icon_scale"
    )

    val spacing = AppDimens.spacing
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.cardShape,
        colors = CardDefaults.cardColors(containerColor = containerColor, contentColor = contentColor),
        elevation = CardDefaults.cardElevation(defaultElevation = AppDimens.elevation.high)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.lg, vertical = spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(spacing.md)
        ) {
            Surface(
                shape = CircleShape,
                color = contentColor.copy(alpha = 0.18f),
                contentColor = contentColor,
                modifier = Modifier
                    .size(88.dp)
                    .graphicsLayer {
                        scaleX = iconScale
                        scaleY = iconScale
                    }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(52.dp)
                    )
                }
            }
            Text(
                text = winnerTitle,
                style = MaterialTheme.typography.displaySmall.makeTitle(),
                textAlign = TextAlign.Center
            )
            Text(
                text = reason,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}

/** Secret word as the hero element, followed by the impostors (chips) or a hidden notice. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ResultDetailsCard(
    word: String,
    impostorNames: List<String>,
    showImpostors: Boolean,
    modifier: Modifier = Modifier
) {
    val spacing = AppDimens.spacing
    AppCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(spacing.xs)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.sm)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Key,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = stringResource(R.string.core_resources_phase2_result_word_label),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = word,
                style = MaterialTheme.typography.headlineLarge.makeTitle(),
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = spacing.xs))
        Text(
            text = stringResource(R.string.core_resources_phase2_result_impostors_label),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (showImpostors) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                verticalArrangement = Arrangement.spacedBy(spacing.sm)
            ) {
                impostorNames.forEach { name ->
                    StatusChip(text = name, variant = StatusChipVariant.Error, icon = Icons.Outlined.TheaterComedy)
                }
            }
        } else {
            Text(
                text = stringResource(R.string.core_resources_phase2_result_impostors_hidden),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
internal fun ResultVotesRow(
    correctVotes: Int,
    incorrectVotes: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppDimens.spacing.md)
    ) {
        ResultVotesCard(
            votes = correctVotes,
            label = stringResource(R.string.core_resources_votes_correct_label),
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.weight(1f)
        )
        ResultVotesCard(
            votes = incorrectVotes,
            label = stringResource(R.string.core_resources_votes_incorrect_label),
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ResultVotesCard(
    votes: Int,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    AppCard(modifier = modifier) {
        Text(
            text = "$votes",
            style = MaterialTheme.typography.headlineMedium,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ResultPlayerRolesCard(
    players: List<String>,
    impostorIndexes: Set<Int>,
    showImpostorsInResult: Boolean,
    modifier: Modifier = Modifier
) {
    AppCard(
        modifier = modifier.fillMaxWidth(),
        title = stringResource(R.string.core_resources_result_player_roles),
        icon = Icons.Outlined.People
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(AppDimens.spacing.sm),
            verticalArrangement = Arrangement.spacedBy(AppDimens.spacing.sm)
        ) {
            players.forEachIndexed { index, name ->
                val isImpostor = index in impostorIndexes
                val showRole = showImpostorsInResult || !isImpostor
                StatusChip(
                    text = name,
                    variant = if (showRole && isImpostor) StatusChipVariant.Error else StatusChipVariant.Ready,
                    icon = if (showRole && isImpostor) Icons.Outlined.TheaterComedy else Icons.Outlined.People
                )
            }
        }
    }
}

@Composable
internal fun ResultVoteAccuracyCard(
    correctVotes: Int,
    incorrectVotes: Int,
    modifier: Modifier = Modifier
) {
    val total = correctVotes + incorrectVotes
    if (total == 0) return
    val accuracy = correctVotes.toFloat() / total.toFloat()
    val accuracyPct = (accuracy * 100).toInt()
    val color = if (accuracy >= VOTE_ACCURACY_MIN) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.error

    AppCard(
        modifier = modifier.fillMaxWidth(),
        title = stringResource(R.string.core_resources_phase2_result_votes_title),
        icon = Icons.Outlined.HowToVote
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
                color = color
            )
        }
        LinearProgressIndicator(
            progress = { accuracy },
            modifier = Modifier.fillMaxWidth(),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    }
}

@Composable
internal fun ResultRateCluesButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    PrimaryActionButton(
        text = stringResource(R.string.core_resources_result_rate_clues),
        onClick = onClick,
        modifier = modifier
    )
}

@Composable
internal fun ResultCloseButton(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val sound = LocalSoundPlayer.current
    OutlinedButton(
        onClick = { sound(Sounds.CLK_TAP); onBack() },
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
    ) {
        Text(stringResource(R.string.core_resources_close))
    }
}

@UiModePreviews
@Composable
private fun ResultWinnerCardPreview() {
    AppTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ResultWinnerCard(OfflineWinner.Civilians, "Civilians win", "All impostors were voted out.")
            ResultWinnerCard(OfflineWinner.Impostors, "Impostors win", "They take control of the vote.")
            ResultWinnerCard(OfflineWinner.Tie, "Tie", "Nobody got an advantage.")
        }
    }
}

@UiModePreviews
@Composable
private fun ResultDetailsPreview() {
    AppTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ResultDetailsCard("Volcano", listOf("Ana", "Luis"), showImpostors = true)
            ResultVotesRow(correctVotes = 3, incorrectVotes = 1)
            ResultPlayerRolesCard(listOf("Ana", "Luis", "Eva"), setOf(0, 1), showImpostorsInResult = true)
        }
    }
}
