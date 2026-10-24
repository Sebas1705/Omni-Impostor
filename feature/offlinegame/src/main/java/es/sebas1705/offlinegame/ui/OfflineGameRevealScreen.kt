@file:Suppress("LongMethod")

package es.sebas1705.offlinegame.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import es.sebas1705.core.resources.R
import es.sebas1705.offlinegame.models.screenLogD
import es.sebas1705.offlinegame.ui.components.RevealCard
import es.sebas1705.offlinegame.ui.components.RevealContinueButton
import es.sebas1705.offlinegame.ui.components.RevealGameInfoBar
import es.sebas1705.offlinegame.ui.components.RevealHeader
import es.sebas1705.offlinegame.ui.components.RevealProgressDots
import es.sebas1705.offlinegame.viewmodel.OfflineGameState
import es.sebas1705.ui.components.PrimaryActionButton
import es.sebas1705.ui.theme.AppDimens

private const val REVEALED_OFFSET_PX = -280f
private val LANDSCAPE_BREAKPOINT = 600.dp

@Composable
internal fun OfflineGameRevealScreen(
    uiState: OfflineGameState,
    onRevealDone: () -> Unit,
    onNextRevealPlayer: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val playerIndex = uiState.currentRevealIndex
    val isImpostor = playerIndex in uiState.impostorPlayerIndexes
    val wordEntry = uiState.wordEntry
    val clue = wordEntry?.clue
        ?.getOrNull(playerIndex % (wordEntry.clue.size.coerceAtLeast(1)))
        .orEmpty()

    var dragOffset by remember(playerIndex) { mutableFloatStateOf(0f) }
    // rememberSaveable survives rotation; a plain remember would reset the overlay on config change,
    // causing the current player's card to be visible to the wrong player.
    var showPassPhoneOverlay by rememberSaveable(playerIndex) { mutableStateOf(false) }
    val rotationY by animateFloatAsState(
        targetValue = if (uiState.revealedCurrentCard) 180f else 0f,
        animationSpec = if (uiState.revealedCurrentCard) tween(durationMillis = 600) else snap(),
        label = "reveal_card_rotation"
    )
    val density = LocalDensity.current

    LaunchedEffect(uiState.revealedCurrentCard, playerIndex) {
        screenLogD(
            "reveal phase playerIndex=$playerIndex player=${uiState.currentPlayerName} " +
                "revealed=${uiState.revealedCurrentCard}"
        )
        dragOffset = if (uiState.revealedCurrentCard) REVEALED_OFFSET_PX else 0f
    }

    Box(modifier = modifier) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            if (maxWidth >= LANDSCAPE_BREAKPOINT) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(end = AppDimens.spacing.lg),
                        verticalArrangement = Arrangement.SpaceEvenly,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        RevealHeader(
                            playerIndex = playerIndex,
                            playersSize = uiState.players.size,
                            currentPlayerName = uiState.currentPlayerName
                        )
                        RevealProgressDots(
                            currentRevealIndex = playerIndex,
                            totalPlayers = uiState.players.size
                        )
                        RevealGameInfoBar(
                            totalPlayers = uiState.players.size,
                            impostorCount = uiState.impostorPlayerIndexes.size,
                            showImpostorCount = uiState.showNumOfImpostors
                        )
                        RevealContinueButton(
                            hasMoreRevealPlayers = uiState.hasMoreRevealPlayers,
                            revealedCurrentCard = uiState.revealedCurrentCard,
                            onNextRevealPlayer = { showPassPhoneOverlay = true }
                        )
                    }
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(start = AppDimens.spacing.lg)
                    ) {
                        RevealCard(
                            uiState = uiState,
                            isImpostor = isImpostor,
                            clue = clue,
                            word = wordEntry?.word.orEmpty(),
                            dragOffset = dragOffset,
                            rotationY = rotationY,
                            density = density.density,
                            fillHeight = true,
                            onDragOffsetChange = { dragOffset = it },
                            onRevealDone = onRevealDone
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .navigationBarsPadding()
                ) {
                    RevealHeader(
                        playerIndex = playerIndex,
                        playersSize = uiState.players.size,
                        currentPlayerName = uiState.currentPlayerName
                    )
                    Spacer(Modifier.height(AppDimens.spacing.sm))
                    RevealProgressDots(
                        currentRevealIndex = playerIndex,
                        totalPlayers = uiState.players.size
                    )
                    Spacer(Modifier.weight(1f))
                    RevealCard(
                        uiState = uiState,
                        isImpostor = isImpostor,
                        clue = clue,
                        word = wordEntry?.word.orEmpty(),
                        dragOffset = dragOffset,
                        rotationY = rotationY,
                        density = density.density,
                        onDragOffsetChange = { dragOffset = it },
                        onRevealDone = onRevealDone
                    )
                    Spacer(Modifier.weight(1f))
                    RevealGameInfoBar(
                        totalPlayers = uiState.players.size,
                        impostorCount = uiState.impostorPlayerIndexes.size,
                        showImpostorCount = uiState.showNumOfImpostors
                    )
                    Spacer(Modifier.height(AppDimens.spacing.sm))
                    RevealContinueButton(
                        hasMoreRevealPlayers = uiState.hasMoreRevealPlayers,
                        revealedCurrentCard = uiState.revealedCurrentCard,
                        onNextRevealPlayer = { showPassPhoneOverlay = true }
                    )
                }
            }
        }

        if (showPassPhoneOverlay) {
            val nextPlayerName = uiState.players.getOrElse(playerIndex + 1) { "" }
            PassPhoneOverlay(
                hasNextPlayer = uiState.hasMoreRevealPlayers,
                nextPlayerName = nextPlayerName,
                onReady = {
                    showPassPhoneOverlay = false
                    onNextRevealPlayer()
                }
            )
        }
    }
}

@Composable
private fun PassPhoneOverlay(
    hasNextPlayer: Boolean,
    nextPlayerName: String,
    onReady: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
            // Swallow touches so the (already revealed) card underneath cannot be dragged.
            .pointerInput(Unit) {},
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(fraction = 0.85f)
                .verticalScroll(rememberScrollState())
                .padding(AppDimens.spacing.xxl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppDimens.spacing.lg)
        ) {
            if (hasNextPlayer) {
                Text(
                    text = stringResource(R.string.core_resources_pass_phone_title),
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = nextPlayerName,
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
            } else {
                Text(
                    text = stringResource(R.string.core_resources_all_revealed_title),
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = stringResource(R.string.core_resources_all_revealed_body),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
            Spacer(Modifier.height(AppDimens.spacing.sm))
            PrimaryActionButton(
                text = stringResource(R.string.core_resources_ready),
                onClick = onReady
            )
        }
    }
}
