package es.sebas1705.offlinegame.viewmodel

import android.content.Context
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import es.sebas1705.common.BuildConfig
import es.sebas1705.common.mvi.MVIBaseViewModel
import es.sebas1705.common.utlis.extensions.types.logD
import es.sebas1705.common.utlis.extensions.types.logI
import es.sebas1705.common.utlis.extensions.types.logW
import es.sebas1705.core.resources.Musics
import es.sebas1705.core.resources.R
import es.sebas1705.core.resources.Sounds
import es.sebas1705.domain.managers.MediaPlayerManager
import es.sebas1705.domain.managers.SoundPoolManager
import es.sebas1705.game.words.SearchWordsUseCase
import es.sebas1705.models.Modes
import es.sebas1705.offlinegame.models.OfflineGameResult
import es.sebas1705.offlinegame.models.OfflineGameStep
import es.sebas1705.offlinegame.models.OfflineWinner
import es.sebas1705.ranking.RecordOfflineMatchResultUseCase
import kotlinx.coroutines.Dispatchers
import java.text.Normalizer
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
@Suppress("TooManyFunctions")
class OfflineGameViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val searchWordsUseCase: SearchWordsUseCase,
    private val recordOfflineMatchResultUseCase: RecordOfflineMatchResultUseCase,
    private val mediaPlayerManager: MediaPlayerManager,
    private val soundPoolManager: SoundPoolManager,
) : MVIBaseViewModel<OfflineGameState, OfflineGameIntent>(context) {

    private companion object {
        const val PREVIEW_MAX_ITEMS = 3
    }

    override fun initState(): OfflineGameState = OfflineGameState()

    override fun intentHandler(intent: OfflineGameIntent) =
        when (intent) {
            is OfflineGameIntent.Initialize -> initialize(intent)
            OfflineGameIntent.MarkRevealDone -> markRevealDone()
            OfflineGameIntent.NextRevealPlayer -> nextRevealPlayer()
            is OfflineGameIntent.VotePlayer -> votePlayer(intent.playerIndex)
            is OfflineGameIntent.SubmitGuess -> submitGuess(intent.value)
        }

    @Suppress("LongMethod")
    private fun initialize(
        intent: OfflineGameIntent.Initialize
    ) = execute(Dispatchers.IO) {
        if (_uiState.value.wordEntry != null) {
            logD("initialize skipped reason=already_initialized")
            return@execute
        }
        runCatching { mediaPlayerManager.changeSong(Musics.GAME) }
            .onFailure { logW("audio switch failed: ${it.message}") }
        logI(
            "initialize start playersRaw=${intent.players.size} categories=${intent.categories.size} " +
                "mode=${intent.mode} impostorsRequested=${intent.impostors}"
        )
        val players = intent.players
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
        logD("initialize sanitizedPlayers=${players.size} preview=${players.preview()}")

        updateUi {
            it.copy(isLoading = true, errorMessage = null)
        }

        val words = searchWordsUseCase(intent.categories)
        logD("initialize wordsFound=${words.size}")
        if (words.isEmpty()) {
            logW("initialize aborted reason=no_words")
            updateUi {
                it.copy(
                    isLoading = false,
                    errorMessage = context.getString(R.string.core_resources_game_error_no_words)
                )
            }
            return@execute
        }

        val selectedWord = words.random()
        val impostorCount = resolveImpostorCount(
            mode = intent.mode,
            requested = intent.impostors,
            playerCount = players.size
        )
        logD(
            "initialize selectedWord='${selectedWord.word}' clues=${selectedWord.clue.size} " +
                "impostorCount=$impostorCount"
        )

        val impostorIndexes = players.indices.shuffled().take(impostorCount).toSet()
        val startingPlayerIndex = players.indices.random()

        logD(
            "initialize setup ready players=${players.size} alive=${players.size} " +
                "impostorIndexes=$impostorIndexes impostorNames=${impostorIndexes.map { players[it] }} " +
                "startingPlayerIndex=$startingPlayerIndex startingPlayer=${players[startingPlayerIndex]}"
        )

        updateUi {
            it.copy(
                isLoading = false,
                errorMessage = null,
                step = OfflineGameStep.Reveal,
                players = players,
                alivePlayerIndexes = players.indices.toSet(),
                impostorPlayerIndexes = impostorIndexes,
                currentRevealIndex = 0,
                revealedCurrentCard = false,
                currentSpeakerIndex = startingPlayerIndex,
                wordEntry = selectedWord,
                correctVotes = 0,
                incorrectVotes = 0,
                guessFeedback = null,
                result = null,
                discussionTimerSeconds = intent.discussionTimerSeconds,
                impostorsKnowEachOther = intent.impostorsKnowEachOther,
            )
        }
        logI("initialize done step=${OfflineGameStep.Reveal}")
    }

    private fun markRevealDone() = updateUi { state ->
        if (state.step != OfflineGameStep.Reveal) {
            logW("markRevealDone ignored reason=invalid_step currentStep=${state.step}")
            state
        } else {
            logD("markRevealDone playerIndex=${state.currentRevealIndex} player=${state.currentPlayerName}")
            state.copy(revealedCurrentCard = true)
        }
    }

    private fun nextRevealPlayer() =
        updateUi { state ->
            if (state.step != OfflineGameStep.Reveal || !state.revealedCurrentCard) {
                logW(
                    "nextRevealPlayer ignored step=${state.step} " +
                        "revealedCurrentCard=${state.revealedCurrentCard}"
                )
                return@updateUi state
            }

            if (!state.hasMoreRevealPlayers) {
                logI(
                    "nextRevealPlayer transition=Discussion startingSpeakerIndex=${state.currentSpeakerIndex} " +
                        "startingSpeaker=${state.players[state.currentSpeakerIndex]}"
                )
                return@updateUi state.copy(
                    step = OfflineGameStep.Discussion,
                    guessFeedback = context.getString(
                        R.string.core_resources_game_starts_discussion,
                        state.players[state.currentSpeakerIndex]
                    )
                )
            }

            val nextIndex = state.currentRevealIndex + 1
            logD(
                "nextRevealPlayer advance from=${state.currentRevealIndex}:${state.currentPlayerName} " +
                    "to=$nextIndex:${state.players[nextIndex]}"
            )

            state.copy(
                currentRevealIndex = nextIndex,
                revealedCurrentCard = false
            )
        }

    @Suppress("LongMethod")
    private fun votePlayer(
        playerIndex: Int
    ) = updateUi { state ->
            logD(
                "votePlayer attempt index=$playerIndex " +
                    "name=${state.players.getOrNull(playerIndex)} step=${state.step} alive=${state.alivePlayerIndexes.size}"
            )
            if (state.step != OfflineGameStep.Discussion || playerIndex !in state.alivePlayerIndexes) {
                logW(
                    "votePlayer ignored reason=${if (state.step != OfflineGameStep.Discussion) "invalid_step" else "player_not_alive"}"
                )
                return@updateUi state
            }

            val isImpostor = playerIndex in state.impostorPlayerIndexes
            val newAlive = state.alivePlayerIndexes - playerIndex
            val correctVotes = if (isImpostor) state.correctVotes + 1 else state.correctVotes
            val incorrectVotes = if (isImpostor) state.incorrectVotes else state.incorrectVotes + 1
            val result = buildResultIfFinished(state, newAlive, correctVotes, incorrectVotes)
            val playerName = state.players.getOrElse(playerIndex) { "?" }
            logD(
                "votePlayer processed name=$playerName role=${if (isImpostor) "impostor" else "civilian"} " +
                    "aliveAfter=${newAlive.size} correctVotes=$correctVotes incorrectVotes=$incorrectVotes"
            )

            runCatching { soundPoolManager.play(Sounds.CLK_CASUAL) }
                .onFailure { logW("sfx vote failed: ${it.message}") }

            if (result == null) {
                val voteFeedback = if (BuildConfig.DEBUG) {
                    if (isImpostor) {
                        context.getString(
                            R.string.core_resources_game_vote_reveal_impostor,
                            playerName
                        )
                    } else {
                        context.getString(
                            R.string.core_resources_game_vote_reveal_civilian,
                            playerName
                        )
                    }
                } else {
                    context.getString(
                        R.string.core_resources_game_vote_reveal_neutral,
                        playerName
                    )
                }

                state.copy(
                    alivePlayerIndexes = newAlive,
                    correctVotes = correctVotes,
                    incorrectVotes = incorrectVotes,
                    guessFeedback = voteFeedback
                )
            } else {
                logI("votePlayer finished result=${describeResult(result)}")
                val resultSfx = when (result.winner) {
                    OfflineWinner.Civilians -> Sounds.SND_WIN
                    OfflineWinner.Impostors -> Sounds.SND_LOSE
                    OfflineWinner.Tie -> Sounds.SND_BOWING
                }
                runCatching { soundPoolManager.play(resultSfx) }
                    .onFailure { logW("sfx result failed: ${it.message}") }
                recordRankingForWinner(
                    players = state.players,
                    impostorIndexes = state.impostorPlayerIndexes,
                    winner = result.winner
                )
                state.copy(
                    alivePlayerIndexes = newAlive,
                    correctVotes = correctVotes,
                    incorrectVotes = incorrectVotes,
                    step = OfflineGameStep.Result,
                    result = result,
                    guessFeedback = null
                )
            }
        }

    private fun submitGuess(
        value: String
    ) = updateUi { state ->
            logD(
                "submitGuess attempt raw='${value.trim()}' step=${state.step} hasWord=${state.wordEntry != null}"
            )
            if (state.step != OfflineGameStep.Discussion || state.wordEntry == null) {
                logW("submitGuess ignored reason=invalid_state")
                return@updateUi state
            }

            val guess = normalize(value)
            val solution = normalize(state.wordEntry.word)
            if (guess.isBlank()) {
                logW("submitGuess rejected reason=blank_guess")
                return@updateUi state.copy(
                    guessFeedback = context.getString(R.string.core_resources_game_guess_empty)
                )
            }

            if (guess == solution) {
                logI("submitGuess success winner=Impostors guessedWord='${state.wordEntry.word}'")
                runCatching { soundPoolManager.play(Sounds.SND_LOSE) }
                    .onFailure { logW("sfx guess-win failed: ${it.message}") }
                recordRankingForWinner(
                    players = state.players,
                    impostorIndexes = state.impostorPlayerIndexes,
                    winner = OfflineWinner.Impostors
                )
                state.copy(
                    step = OfflineGameStep.Result,
                    result = OfflineGameResult(
                        winner = OfflineWinner.Impostors,
                        reason = context.getString(R.string.core_resources_game_reason_guess_win),
                        word = state.wordEntry.word,
                        impostorNames = state.impostorPlayerIndexes.map { state.players[it] },
                        correctVotes = state.correctVotes,
                        incorrectVotes = state.incorrectVotes
                    ),
                    guessFeedback = null
                )
            } else {
                logD("submitGuess failed normalizedGuess='$guess' solution='$solution'")
                state.copy(guessFeedback = context.getString(R.string.core_resources_game_guess_wrong))
            }
        }

    private fun buildResultIfFinished(
        state: OfflineGameState,
        aliveIndexes: Set<Int>,
        correctVotes: Int,
        incorrectVotes: Int
    ): OfflineGameResult? {
        val aliveImpostors = aliveIndexes.intersect(state.impostorPlayerIndexes)
        val aliveCivilians = aliveIndexes - state.impostorPlayerIndexes
        val isFinalDuel = aliveCivilians.size == 1 && aliveImpostors.size == 1
        val impostorsForceWin = aliveImpostors.size > aliveCivilians.size
        logD(
            "buildResultIfFinished aliveImpostors=${aliveImpostors.size} aliveCivilians=${aliveCivilians.size} " +
                "isFinalDuel=$isFinalDuel impostorsForceWin=$impostorsForceWin"
        )

        val winnerAndReason = when {
            aliveImpostors.isEmpty() -> {
                OfflineWinner.Civilians to context.getString(
                    R.string.core_resources_game_reason_all_impostors_voted
                )
            }

            aliveCivilians.isEmpty() -> {
                OfflineWinner.Impostors to context.getString(
                    R.string.core_resources_game_reason_no_civilians
                )
            }

            impostorsForceWin -> {
                OfflineWinner.Impostors to context.getString(
                    R.string.core_resources_game_reason_impostors_outnumber
                )
            }

            isFinalDuel -> {
                OfflineWinner.Tie to context.getString(R.string.core_resources_game_reason_tie)
            }

            else -> return null
        }

        val winner = winnerAndReason.first
        val reason = winnerAndReason.second

        return OfflineGameResult(
            winner = winner,
            reason = reason,
            word = state.wordEntry?.word.orEmpty(),
            impostorNames = state.impostorPlayerIndexes.map { state.players[it] },
            correctVotes = correctVotes,
            incorrectVotes = incorrectVotes
        ).also { logI("buildResultIfFinished resolved=${describeResult(it)}") }
    }

    private fun resolveImpostorCount(
        mode: Modes,
        requested: Int,
        playerCount: Int
    ): Int {
        val maxImpostors = (playerCount - 1).coerceAtLeast(1)
        val resolved = when (mode) {
            Modes.Classic -> requested.coerceIn(1, maxImpostors)
            Modes.Chaos -> Random.nextInt(from = 1, until = requested + 1)
        }
        logD(
            "resolveImpostorCount mode=$mode requested=$requested playerCount=$playerCount " +
                "max=$maxImpostors resolved=$resolved"
        )
        return resolved
    }

    private fun normalize(text: String): String {
        val normalized = Normalizer.normalize(text.trim(), Normalizer.Form.NFD)
        return normalized.replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "").lowercase()
    }

    private fun recordRankingForWinner(
        players: List<String>,
        impostorIndexes: Set<Int>,
        winner: OfflineWinner
    ) {
        val impostorNames = impostorIndexes.map { players[it] }.toSet()
        val civilianNames = players.filterIndexed { index, _ -> index !in impostorIndexes }.toSet()

        val civilianWinnerNames = when (winner) {
            OfflineWinner.Civilians -> civilianNames
            OfflineWinner.Impostors,
            OfflineWinner.Tie -> emptySet()
        }
        val impostorWinnerNames = when (winner) {
            OfflineWinner.Impostors -> impostorNames
            OfflineWinner.Civilians,
            OfflineWinner.Tie -> emptySet()
        }
        logI(
            "recordRankingForWinner winner=$winner players=${players.size} " +
                "civilianWinners=${civilianWinnerNames.preview()} impostorWinners=${impostorWinnerNames.preview()}"
        )

        execute(Dispatchers.IO) {
            runCatching {
                recordOfflineMatchResultUseCase(
                    allPlayerNames = players.toSet(),
                    civilianWinnerNames = civilianWinnerNames,
                    impostorWinnerNames = impostorWinnerNames
                )
                logD("recordRankingForWinner persisted allPlayers=${players.size}")
            }.onFailure { throwable ->
                logW("recordRankingForWinner failed: ${throwable.message}")
            }
        }
    }


    override fun onCleared() {
        super.onCleared()
        runCatching { mediaPlayerManager.changeSong(Musics.BACKGROUND) }
            .onFailure { logW("audio restore background failed: ${it.message}") }
    }

    private fun describeResult(result: OfflineGameResult): String =
        "winner=${result.winner} word='${result.word}' impostors=${result.impostorNames.preview()} " +
            "votes(correct=${result.correctVotes}, incorrect=${result.incorrectVotes})"

    private fun Iterable<*>.preview(limit: Int = PREVIEW_MAX_ITEMS): String {
        val values = this.toList()
        if (values.isEmpty()) return "[]"
        val head = values.take(limit).joinToString(prefix = "[", postfix = "")
        return if (values.size > limit) "$head, ...] (size=${values.size})" else "$head]"
    }
}


