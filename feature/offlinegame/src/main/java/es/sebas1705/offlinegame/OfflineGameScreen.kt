@file:Suppress("LongMethod")

package es.sebas1705.offlinegame

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import es.sebas1705.core.resources.R
import es.sebas1705.models.Categories
import es.sebas1705.models.Modes
import es.sebas1705.offlinegame.models.OfflineGameStep
import es.sebas1705.offlinegame.models.screenLogD
import es.sebas1705.offlinegame.models.screenLogI
import es.sebas1705.offlinegame.ui.OfflineGameDiscussionScreen
import es.sebas1705.offlinegame.ui.OfflineGameRatingScreen
import es.sebas1705.offlinegame.ui.OfflineGameResultScreen
import es.sebas1705.offlinegame.ui.OfflineGameRevealScreen
import es.sebas1705.offlinegame.viewmodel.OfflineGameIntent
import es.sebas1705.offlinegame.viewmodel.OfflineGameState
import es.sebas1705.offlinegame.viewmodel.OfflineGameViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet

private const val PHASE_TRANSITION_DURATION_MS = 350

@Composable
fun OfflineGameScreen(
    players: ImmutableList<String>,
    categories: ImmutableSet<Categories>,
    mode: Modes,
    impostors: Int,
    showImpostorsInResult: Boolean,
    discussionTimerSeconds: Int,
    impostorsKnowEachOther: Boolean,
    showNumOfImpostors: Boolean,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    viewModel: OfflineGameViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(null) {
        screenLogI(
            "start players=${players.size} categories=${categories.size} mode=$mode " +
                "impostors=$impostors showImpostorsInResult=$showImpostorsInResult"
        )
        viewModel.eventHandler(
            OfflineGameIntent.Initialize(
                players = players,
                categories = categories,
                mode = mode,
                impostors = impostors,
                discussionTimerSeconds = discussionTimerSeconds,
                impostorsKnowEachOther = impostorsKnowEachOther,
                showNumOfImpostors = showNumOfImpostors
            )
        )
    }

    LaunchedEffect(uiState.step, uiState.isLoading, uiState.errorMessage) {
        screenLogD(
            "state step=${uiState.step} loading=${uiState.isLoading} " +
                "error=${uiState.errorMessage != null} alive=${uiState.alivePlayerIndexes.size}"
        )
    }

    OfflineGameDesign(
        modifier = modifier,
        uiState = uiState,
        onBack = {
            screenLogI("action back_pressed step=${uiState.step}")
            onBack()
        },
        onRevealDone = {
            screenLogD("action reveal_done playerIndex=${uiState.currentRevealIndex}")
            viewModel.eventHandler(OfflineGameIntent.MarkRevealDone)
        },
        onNextRevealPlayer = {
            screenLogD("action next_reveal_player currentIndex=${uiState.currentRevealIndex}")
            viewModel.eventHandler(OfflineGameIntent.NextRevealPlayer)
        },
        onVotePlayer = { index ->
            screenLogD("action vote_player index=$index name=${uiState.players.getOrNull(index)}")
            viewModel.eventHandler(OfflineGameIntent.VotePlayer(index))
        },
        onGuess = { value ->
            screenLogD("action submit_guess valueLength=${value.length}")
            viewModel.eventHandler(OfflineGameIntent.SubmitGuess(value))
        },
        onRateClues = {
            screenLogD("action go_to_rating")
            viewModel.eventHandler(OfflineGameIntent.GoToRating)
        },
        onRateClue = { clue, positive ->
            viewModel.eventHandler(OfflineGameIntent.RateClue(clue, positive))
        },
        onSubmitRatings = {
            screenLogI("action submit_ratings")
            viewModel.eventHandler(OfflineGameIntent.SubmitRatings)
        },
        showImpostorsInResult = showImpostorsInResult
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OfflineGameDesign(
    uiState: OfflineGameState,
    onBack: () -> Unit,
    onRevealDone: () -> Unit,
    onNextRevealPlayer: () -> Unit,
    onVotePlayer: (Int) -> Unit,
    onGuess: (String) -> Unit,
    onRateClues: () -> Unit,
    onRateClue: (String, Boolean) -> Unit,
    onSubmitRatings: () -> Unit,
    showImpostorsInResult: Boolean,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(uiState.step) {
        screenLogI("design rendering step=${uiState.step}")
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.core_resources_offline_game_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.core_resources_back)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        if (!uiState.errorMessage.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = uiState.errorMessage,
                    color = MaterialTheme.colorScheme.error
                )
            }
            return@Scaffold
        }

        AnimatedContent(
            targetState = uiState.step,
            transitionSpec = {
                val enterOffset = if (targetState.ordinal > initialState.ordinal) 1 else -1
                (fadeIn(tween(PHASE_TRANSITION_DURATION_MS)) +
                    slideInVertically(tween(PHASE_TRANSITION_DURATION_MS)) { it / 5 * enterOffset })
                    .togetherWith(
                        fadeOut(tween(PHASE_TRANSITION_DURATION_MS / 2)) +
                            slideOutVertically(tween(PHASE_TRANSITION_DURATION_MS)) { -it / 10 * enterOffset }
                    )
            },
            label = "game_step_transition"
        ) { step ->
            when (step) {
                OfflineGameStep.Reveal -> {
                    OfflineGameRevealScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(16.dp),
                        uiState = uiState,
                        onRevealDone = onRevealDone,
                        onNextRevealPlayer = onNextRevealPlayer
                    )
                }

                OfflineGameStep.Discussion -> {
                    OfflineGameDiscussionScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(16.dp),
                        uiState = uiState,
                        onVotePlayer = onVotePlayer,
                        onGuess = onGuess
                    )
                }

                OfflineGameStep.Result -> {
                    OfflineGameResultScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(16.dp),
                        result = uiState.result,
                        showImpostorsInResult = showImpostorsInResult,
                        players = uiState.players,
                        impostorIndexes = uiState.impostorPlayerIndexes,
                        onBack = onBack,
                        onRateClues = onRateClues
                    )
                }

                OfflineGameStep.Rating -> {
                    OfflineGameRatingScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(16.dp),
                        uiState = uiState,
                        onRateClue = onRateClue,
                        onSubmit = onSubmitRatings,
                        onSkip = onBack,
                        onDone = onBack
                    )
                }
            }
        }
    }
}




