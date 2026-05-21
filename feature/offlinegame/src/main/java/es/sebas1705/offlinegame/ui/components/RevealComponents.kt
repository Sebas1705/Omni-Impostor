package es.sebas1705.offlinegame.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.TheaterComedy
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import es.sebas1705.core.resources.R
import es.sebas1705.offlinegame.viewmodel.OfflineGameState
import es.sebas1705.core.resources.Sounds
import es.sebas1705.ui.sound.LocalSoundPlayer
import kotlin.math.roundToInt

private const val REVEAL_THRESHOLD_PX = -180f
private const val REVEALED_OFFSET_PX = -280f

@Composable
internal fun RevealProgressDots(currentRevealIndex: Int, totalPlayers: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalPlayers) { index ->
            val isDone = index < currentRevealIndex
            val isCurrent = index == currentRevealIndex
            val dotColor by animateColorAsState(
                targetValue = when {
                    isDone || isCurrent -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.surfaceContainerHighest
                },
                label = "dot_color_$index"
            )
            val dotSize by animateDpAsState(
                targetValue = if (isCurrent) 14.dp else 9.dp,
                animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
                label = "dot_size_$index"
            )
            Box(
                modifier = Modifier
                    .size(dotSize)
                    .background(color = dotColor, shape = CircleShape)
                    .border(
                        width = if (isCurrent) 2.dp else 1.dp,
                        color = if (isCurrent || isDone) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outlineVariant,
                        shape = CircleShape
                    )
            )
        }
    }
}

@Composable
internal fun RevealGameInfoBar(
    totalPlayers: Int,
    impostorCount: Int,
    showImpostorCount: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
    ) {
        AssistChip(
            onClick = {},
            label = { Text(stringResource(R.string.core_resources_info_players, totalPlayers)) },
            leadingIcon = {
                Icon(
                    Icons.Outlined.People,
                    contentDescription = null,
                    modifier = Modifier.size(AssistChipDefaults.IconSize)
                )
            }
        )
        if(showImpostorCount) AssistChip(
            onClick = {},
            label = { Text(stringResource(R.string.core_resources_info_impostors, impostorCount)) },
            leadingIcon = {
                Icon(
                    Icons.Outlined.TheaterComedy,
                    contentDescription = null,
                    modifier = Modifier.size(AssistChipDefaults.IconSize)
                )
            }
        )
    }
}

@Composable
internal fun RevealHeader(playerIndex: Int, playersSize: Int, currentPlayerName: String) {
    Text(
        text = stringResource(
            R.string.core_resources_player_progress,
            playerIndex + 1,
            playersSize,
            currentPlayerName
        ),
        style = MaterialTheme.typography.titleLarge
    )
    Text(
        text = stringResource(R.string.core_resources_swipe_card_reveal),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
internal fun RevealCard(
    uiState: OfflineGameState,
    isImpostor: Boolean,
    clue: String,
    word: String,
    dragOffset: Float,
    rotationY: Float,
    density: Float,
    fillHeight: Boolean = false,
    onDragOffsetChange: (Float) -> Unit,
    onRevealDone: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    Box(modifier = Modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (fillHeight) Modifier.fillMaxHeight() else Modifier.height(260.dp))
                .offset { IntOffset(0, dragOffset.roundToInt()) }
                .graphicsLayer {
                    this.rotationY = rotationY
                    cameraDistance = 16f * density
                }
                .draggable(
                    orientation = Orientation.Vertical,
                    state = rememberDraggableState { delta ->
                        if (!uiState.revealedCurrentCard) {
                            onDragOffsetChange((dragOffset + delta).coerceIn(REVEALED_OFFSET_PX, 0f))
                        }
                    },
                    onDragStopped = {
                        if (dragOffset <= REVEAL_THRESHOLD_PX) {
                            onDragOffsetChange(REVEALED_OFFSET_PX)
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onRevealDone()
                        } else {
                            onDragOffsetChange(0f)
                        }
                    }
                ),
            onClick = {
                if (!uiState.revealedCurrentCard) {
                    onDragOffsetChange(REVEALED_OFFSET_PX)
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onRevealDone()
                }
            },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            RevealCardContent(
                uiState = uiState,
                isImpostor = isImpostor,
                clue = clue,
                word = word,
                rotationY = rotationY
            )
        }
    }
}

@Composable
private fun RevealCardContent(
    uiState: OfflineGameState,
    isImpostor: Boolean,
    clue: String,
    word: String,
    rotationY: Float
) {
    val otherImpostorNames = if (isImpostor && uiState.impostorsKnowEachOther) {
        (uiState.impostorPlayerIndexes - uiState.currentRevealIndex)
            .mapNotNull { uiState.players.getOrNull(it) }
    } else emptyList()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                if (rotationY > 90f) this.rotationY = 180f
            }
            .padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!uiState.revealedCurrentCard) {
            Text(
                text = uiState.currentPlayerName,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.core_resources_swipe_up_reveal),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        } else {
            Text(
                text = if (isImpostor) {
                    stringResource(R.string.core_resources_role_impostor)
                } else {
                    stringResource(R.string.core_resources_role_civilian)
                },
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = if (isImpostor) {
                    stringResource(R.string.core_resources_clue_prefix, clue)
                } else {
                    stringResource(R.string.core_resources_word_prefix, word)
                },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            if (otherImpostorNames.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = stringResource(
                        R.string.core_resources_impostor_teammates,
                        otherImpostorNames.joinToString(", ")
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
internal fun RevealContinueButton(
    hasMoreRevealPlayers: Boolean,
    revealedCurrentCard: Boolean,
    onNextRevealPlayer: () -> Unit
) {
    val sound = LocalSoundPlayer.current
    FilledTonalButton(
        onClick = { sound(Sounds.CLK_ARCADE); onNextRevealPlayer() },
        enabled = revealedCurrentCard,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            if (hasMoreRevealPlayers) {
                stringResource(R.string.core_resources_next_player)
            } else {
                stringResource(R.string.core_resources_start_game)
            }
        )
    }
}

