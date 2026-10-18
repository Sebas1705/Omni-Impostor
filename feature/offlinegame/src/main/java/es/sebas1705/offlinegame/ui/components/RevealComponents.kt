@file:Suppress("LongMethod")

package es.sebas1705.offlinegame.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.TheaterComedy
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import es.sebas1705.common.utlis.UiModePreviews
import es.sebas1705.core.resources.R
import es.sebas1705.offlinegame.viewmodel.OfflineGameState
import es.sebas1705.ui.components.PrimaryActionButton
import es.sebas1705.ui.components.SectionHeader
import es.sebas1705.ui.components.StatusChip
import es.sebas1705.ui.components.StatusChipVariant
import es.sebas1705.ui.theme.AppDimens
import es.sebas1705.ui.theme.AppTheme
import es.sebas1705.ui.theme.cardShape
import kotlin.math.roundToInt

private const val REVEAL_THRESHOLD_PX = -180f
private const val REVEALED_OFFSET_PX = -280f
private const val REVEAL_CARD_MIN_HEIGHT_DP = 260
private const val ARROW_BOB_DP = 10f
private const val ARROW_BOB_MS = 700

@Composable
internal fun RevealProgressDots(currentRevealIndex: Int, totalPlayers: Int) {
    Row(
        // The header already announces "player X of N", so the dots are decorative.
        modifier = Modifier
            .fillMaxWidth()
            .clearAndSetSemantics {},
        horizontalArrangement = Arrangement.spacedBy(AppDimens.spacing.sm, Alignment.CenterHorizontally),
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
        horizontalArrangement = Arrangement.spacedBy(AppDimens.spacing.sm, Alignment.CenterHorizontally)
    ) {
        StatusChip(
            text = stringResource(R.string.core_resources_info_players, totalPlayers),
            icon = Icons.Outlined.People
        )
        if (showImpostorCount) {
            StatusChip(
                text = stringResource(R.string.core_resources_info_impostors, impostorCount),
                variant = StatusChipVariant.Warning,
                icon = Icons.Outlined.TheaterComedy
            )
        }
    }
}

@Composable
internal fun RevealHeader(playerIndex: Int, playersSize: Int, currentPlayerName: String) {
    SectionHeader(
        title = stringResource(
            R.string.core_resources_player_progress,
            playerIndex + 1,
            playersSize,
            currentPlayerName
        ),
        subtitle = stringResource(R.string.core_resources_swipe_card_reveal),
        modifier = Modifier.fillMaxWidth()
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
    val cardColor by animateColorAsState(
        targetValue = when {
            !uiState.revealedCurrentCard -> MaterialTheme.colorScheme.primaryContainer
            isImpostor -> MaterialTheme.colorScheme.errorContainer
            else -> MaterialTheme.colorScheme.secondaryContainer
        },
        animationSpec = if (uiState.revealedCurrentCard) tween(durationMillis = 400) else snap(),
        label = "card_container_color"
    )
    Box(modifier = Modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (fillHeight) Modifier.fillMaxHeight()
                    else Modifier.heightIn(min = REVEAL_CARD_MIN_HEIGHT_DP.dp)
                )
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
            shape = MaterialTheme.cardShape,
            colors = CardDefaults.cardColors(containerColor = cardColor),
            elevation = CardDefaults.cardElevation(defaultElevation = AppDimens.elevation.high)
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
    val spacing = AppDimens.spacing
    val otherImpostorNames = if (isImpostor && uiState.impostorsKnowEachOther) {
        (uiState.impostorPlayerIndexes - uiState.currentRevealIndex)
            .mapNotNull { uiState.players.getOrNull(it) }
    } else emptyList()
    val onFace = if (isImpostor) MaterialTheme.colorScheme.onErrorContainer
    else MaterialTheme.colorScheme.onSecondaryContainer

    Column(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                if (rotationY > 90f) this.rotationY = 180f
            }
            .padding(spacing.xl),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!uiState.revealedCurrentCard) {
            val bob by rememberInfiniteTransition(label = "reveal_arrow").animateFloat(
                initialValue = 0f,
                targetValue = -ARROW_BOB_DP,
                animationSpec = infiniteRepeatable(tween(ARROW_BOB_MS), RepeatMode.Reverse),
                label = "reveal_arrow_bob"
            )
            Icon(
                imageVector = Icons.Outlined.KeyboardArrowUp,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier
                    .size(48.dp)
                    .offset(y = bob.dp)
            )
            Text(
                text = uiState.currentPlayerName,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(spacing.md))
            Text(
                text = stringResource(R.string.core_resources_swipe_up_reveal),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center
            )
        } else {
            Icon(
                imageVector = if (isImpostor) Icons.Outlined.TheaterComedy else Icons.Outlined.Person,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = onFace
            )
            Spacer(Modifier.height(spacing.sm))
            Text(
                text = if (isImpostor) {
                    stringResource(R.string.core_resources_role_impostor)
                } else {
                    stringResource(R.string.core_resources_role_civilian)
                },
                style = MaterialTheme.typography.headlineSmall,
                color = onFace,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(spacing.md))
            Text(
                text = if (isImpostor) {
                    stringResource(R.string.core_resources_clue_prefix, clue)
                } else {
                    stringResource(R.string.core_resources_word_prefix, word)
                },
                style = MaterialTheme.typography.titleLarge,
                color = onFace,
                textAlign = TextAlign.Center
            )
            if (otherImpostorNames.isNotEmpty()) {
                Spacer(Modifier.height(spacing.md))
                Text(
                    text = stringResource(
                        R.string.core_resources_impostor_teammates,
                        otherImpostorNames.joinToString(", ")
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    textAlign = TextAlign.Center
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
    PrimaryActionButton(
        text = if (hasMoreRevealPlayers) {
            stringResource(R.string.core_resources_next_player)
        } else {
            stringResource(R.string.core_resources_start_game)
        },
        onClick = onNextRevealPlayer,
        enabled = revealedCurrentCard
    )
}

@UiModePreviews
@Composable
private fun RevealChromePreview() {
    AppTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RevealHeader(playerIndex = 1, playersSize = 4, currentPlayerName = "Ana")
            RevealProgressDots(currentRevealIndex = 1, totalPlayers = 4)
            RevealGameInfoBar(totalPlayers = 4, impostorCount = 1, showImpostorCount = true)
            RevealContinueButton(hasMoreRevealPlayers = true, revealedCurrentCard = true, onNextRevealPlayer = {})
        }
    }
}
