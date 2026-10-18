@file:Suppress("LongMethod", "CyclomaticComplexMethod", "TooManyFunctions")

package es.sebas1705.offlinegame.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.HourglassBottom
import androidx.compose.material.icons.outlined.RecordVoiceOver
import androidx.compose.material.icons.outlined.RemoveCircleOutline
import androidx.compose.material.icons.outlined.TheaterComedy
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import es.sebas1705.common.utlis.UiModePreviews
import es.sebas1705.core.resources.R
import es.sebas1705.feature.offlinegame.BuildConfig
import es.sebas1705.ui.components.AppCard
import es.sebas1705.ui.components.SectionHeader
import es.sebas1705.ui.components.StatusChip
import es.sebas1705.ui.components.StatusChipVariant
import es.sebas1705.ui.theme.AppDimens
import es.sebas1705.ui.theme.AppTheme
import es.sebas1705.ui.theme.cardShape

private const val TIMER_WARNING_THRESHOLD = 30
private const val TIMER_CRITICAL_THRESHOLD = 10
private const val TIMER_COLOR_ANIMATION_MS = 600
private const val TIMER_PULSE_MS = 500
private const val TIMER_PULSE_SCALE = 1.12f

/** Visual urgency of the discussion timer; thresholds match the 30 s / 10 s haptic alerts. */
internal enum class TimerUrgency { Normal, Warning, Critical, TimeUp }

internal fun timerUrgency(secondsLeft: Int): TimerUrgency = when {
    secondsLeft <= 0 -> TimerUrgency.TimeUp
    secondsLeft <= TIMER_CRITICAL_THRESHOLD -> TimerUrgency.Critical
    secondsLeft <= TIMER_WARNING_THRESHOLD -> TimerUrgency.Warning
    else -> TimerUrgency.Normal
}

/** Only call it with the timer enabled (`totalSeconds > 0`). */
@Composable
internal fun DiscussionTimer(secondsLeft: Int, totalSeconds: Int, modifier: Modifier = Modifier) {
    val urgency = timerUrgency(secondsLeft)
    val progress = (secondsLeft.toFloat() / totalSeconds.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
    val timeText = "%d:%02d".format(secondsLeft / 60, secondsLeft % 60)
    val scheme = MaterialTheme.colorScheme
    val spacing = AppDimens.spacing

    val containerTarget = when (urgency) {
        TimerUrgency.Normal -> scheme.surfaceContainerHigh
        TimerUrgency.Warning -> scheme.tertiaryContainer
        TimerUrgency.Critical -> scheme.errorContainer
        TimerUrgency.TimeUp -> scheme.error
    }
    val contentTarget = when (urgency) {
        TimerUrgency.Normal -> scheme.onSurface
        TimerUrgency.Warning -> scheme.onTertiaryContainer
        TimerUrgency.Critical -> scheme.onErrorContainer
        TimerUrgency.TimeUp -> scheme.onError
    }
    val indicatorTarget = when (urgency) {
        TimerUrgency.Normal -> scheme.primary
        TimerUrgency.Warning -> scheme.tertiary
        TimerUrgency.Critical -> scheme.error
        TimerUrgency.TimeUp -> scheme.onError
    }
    val containerColor by animateColorAsState(containerTarget, tween(TIMER_COLOR_ANIMATION_MS), label = "timer_container")
    val contentColor by animateColorAsState(contentTarget, tween(TIMER_COLOR_ANIMATION_MS), label = "timer_content")
    val indicatorColor by animateColorAsState(indicatorTarget, tween(TIMER_COLOR_ANIMATION_MS), label = "timer_indicator")

    val pulse by rememberInfiniteTransition(label = "timer_pulse").animateFloat(
        initialValue = 1f,
        targetValue = TIMER_PULSE_SCALE,
        animationSpec = infiniteRepeatable(tween(TIMER_PULSE_MS), RepeatMode.Reverse),
        label = "timer_pulse_scale"
    )
    val timeScale = if (urgency == TimerUrgency.Critical) pulse else 1f

    val label = when (urgency) {
        TimerUrgency.Normal -> stringResource(R.string.core_resources_discussion_timer_label)
        TimerUrgency.Warning -> stringResource(R.string.core_resources_discussion_timer_warning)
        TimerUrgency.Critical -> stringResource(R.string.core_resources_phase2_timer_critical)
        TimerUrgency.TimeUp -> stringResource(R.string.core_resources_phase2_timer_up)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.cardShape,
        colors = CardDefaults.cardColors(containerColor = containerColor, contentColor = contentColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.lg, vertical = spacing.md),
            verticalArrangement = Arrangement.spacedBy(spacing.sm)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (urgency == TimerUrgency.TimeUp) Icons.Outlined.HourglassBottom
                    else Icons.Outlined.Timer,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier
                        .weight(1f)
                        .semantics { liveRegion = LiveRegionMode.Polite }
                )
                Text(
                    text = timeText,
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.graphicsLayer {
                        scaleX = timeScale
                        scaleY = timeScale
                    }
                )
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
                color = indicatorColor,
                trackColor = MaterialTheme.colorScheme.surfaceContainerLow
            )
            if (urgency == TimerUrgency.TimeUp) {
                Text(
                    text = stringResource(R.string.core_resources_phase2_timer_up_hint),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun DiscussionSessionChips(
    totalPlayers: Int,
    alivePlayers: Int,
    impostorCount: Int,
    modifier: Modifier = Modifier
) {
    val eliminated = totalPlayers - alivePlayers
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppDimens.spacing.sm),
        verticalArrangement = Arrangement.spacedBy(AppDimens.spacing.sm)
    ) {
        StatusChip(
            text = stringResource(R.string.core_resources_info_alive, alivePlayers),
            icon = Icons.Outlined.CheckCircle
        )
        if (eliminated > 0) {
            StatusChip(
                text = stringResource(R.string.core_resources_info_eliminated, eliminated),
                variant = StatusChipVariant.Error,
                icon = Icons.Outlined.RemoveCircleOutline
            )
        }
        StatusChip(
            text = stringResource(R.string.core_resources_info_impostors, impostorCount),
            variant = StatusChipVariant.Warning,
            icon = Icons.Outlined.TheaterComedy
        )
    }
}

@Composable
internal fun DiscussionStartPlayerHeader(speakerName: String, modifier: Modifier = Modifier) {
    AppCard(
        modifier = modifier.fillMaxWidth(),
        icon = Icons.Outlined.RecordVoiceOver,
        title = stringResource(R.string.core_resources_starts_player, speakerName)
    ) {}
}

@Composable
internal fun DiscussionFeedbackCard(feedback: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.cardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
    ) {
        Text(
            text = feedback,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
            modifier = Modifier.padding(AppDimens.spacing.lg)
        )
    }
}

@Composable
internal fun GuessWordInput(
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(stringResource(R.string.core_resources_guess_word_label)) },
        enabled = enabled
    )
}

@Composable
internal fun GuessWordButton(
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp),
        enabled = enabled,
    ) {
        Text(stringResource(R.string.core_resources_guess_word_button))
    }
}

@Composable
internal fun VoteSectionHeader(modifier: Modifier = Modifier) {
    SectionHeader(
        title = stringResource(R.string.core_resources_vote_players),
        modifier = modifier
    )
}

@Composable
internal fun VotePlayerItem(
    index: Int,
    playerName: String,
    isAlive: Boolean,
    isImpostor: Boolean,
    onVotePlayer: (Int) -> Unit
) {
    val cardColor by animateColorAsState(
        targetValue = if (isAlive) MaterialTheme.colorScheme.surfaceContainer
        else MaterialTheme.colorScheme.surfaceContainerLowest,
        animationSpec = tween(300),
        label = "vote_card_color_$index"
    )
    val spacing = AppDimens.spacing
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.cardShape,
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.md),
            horizontalArrangement = Arrangement.spacedBy(spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(spacing.sm)
            ) {
                Text(
                    text = playerName,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isAlive) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
                StatusChip(
                    text = if (isAlive) stringResource(R.string.core_resources_alive)
                    else stringResource(R.string.core_resources_already_voted),
                    variant = if (isAlive) StatusChipVariant.Ready else StatusChipVariant.Error,
                    icon = if (isAlive) Icons.Outlined.CheckCircle else Icons.Outlined.RemoveCircleOutline
                )
                if (BuildConfig.DEBUG) {
                    Text(
                        text = if (isImpostor) stringResource(R.string.core_resources_debug_impostor)
                        else stringResource(R.string.core_resources_debug_civilian),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
            Button(
                onClick = { onVotePlayer(index) },
                enabled = isAlive,
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("vote_player_$index")
            ) {
                Text(stringResource(R.string.core_resources_vote))
            }
        }
    }
}

@Composable
internal fun DebugVotesSummary(correctVotes: Int, incorrectVotes: Int) {
    Text(
        text = stringResource(
            R.string.core_resources_votes_summary,
            correctVotes,
            incorrectVotes
        ),
        style = MaterialTheme.typography.bodyMedium
    )
}

@UiModePreviews
@Composable
private fun DiscussionTimerPreview() {
    AppTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DiscussionTimer(secondsLeft = 120, totalSeconds = 180)
            DiscussionTimer(secondsLeft = 25, totalSeconds = 180)
            DiscussionTimer(secondsLeft = 7, totalSeconds = 180)
            DiscussionTimer(secondsLeft = 0, totalSeconds = 180)
        }
    }
}

@UiModePreviews
@Composable
private fun VotePlayerItemPreview() {
    AppTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            VotePlayerItem(0, "Ana", isAlive = true, isImpostor = false, onVotePlayer = {})
            VotePlayerItem(1, "Luis", isAlive = false, isImpostor = true, onVotePlayer = {})
        }
    }
}
