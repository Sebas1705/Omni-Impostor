@file:Suppress("TooManyFunctions")

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
import es.sebas1705.game.words.SubmitClueRatingsUseCase
import es.sebas1705.models.ClueRating
import es.sebas1705.models.Modes
import es.sebas1705.offlinegame.models.OfflineGameResult
import es.sebas1705.offlinegame.models.OfflineGameStep
import es.sebas1705.offlinegame.models.OfflineWinner
import es.sebas1705.ranking.RecordOfflineMatchResultUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update
import java.text.Normalizer
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
@Suppress("TooManyFunctions")
class OfflineGameViewModel internal constructor(
    context: Context,
    private val searchWordsUseCase: SearchWordsUseCase,
    private val recordOfflineMatchResultUseCase: RecordOfflineMatchResultUseCase,
    private val submitClueRatingsUseCase: SubmitClueRatingsUseCase,
    private val mediaPlayerManager: MediaPlayerManager,
    private val soundPoolManager: SoundPoolManager,
    // Background work (word search, ranking and rating writes). Injectable so tests can run it on
    // their own scheduler instead of the real Dispatchers.IO.
    private val ioDispatcher: CoroutineDispatcher,
) : MVIBaseViewModel<OfflineGameState, OfflineGameIntent>(context) {

    @Inject
    constructor(
        @ApplicationContext context: Context,
        searchWordsUseCase: SearchWordsUseCase,
        recordOfflineMatchResultUseCase: RecordOfflineMatchResultUseCase,
        submitClueRatingsUseCase: SubmitClueRatingsUseCase,
        mediaPlayerManager: MediaPlayerManager,
        soundPoolManager: SoundPoolManager,
    ) : this(
        context,
        searchWordsUseCase,
        recordOfflineMatchResultUseCase,
        submitClueRatingsUseCase,
        mediaPlayerManager,
        soundPoolManager,
        Dispatchers.IO,
    )

    private companion object {
        const val PREVIEW_MAX_ITEMS = 3
        const val MIN_PLAYERS = 3
    }

    override fun initState(): OfflineGameState = OfflineGameState()

    override fun intentHandler(intent: OfflineGameIntent) =
        when (intent) {
            is OfflineGameIntent.Initialize -> initialize(intent)
            OfflineGameIntent.MarkRevealDone -> markRevealDone()
            OfflineGameIntent.NextRevealPlayer -> nextRevealPlayer()
            is OfflineGameIntent.VotePlayer -> votePlayer(intent.playerIndex)
            is OfflineGameIntent.SubmitGuess -> submitGuess(intent.value)
            OfflineGameIntent.GoToRating -> goToRating()
            is OfflineGameIntent.RateClue -> rateClue(intent.clueText, intent.isPositive)
            OfflineGameIntent.SubmitRatings -> submitRatings()
        }

    @Suppress("LongMethod")
    private fun initialize(
        intent: OfflineGameIntent.Initialize
    ) = execute(ioDispatcher) {
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

        // The setup screen already requires 3 players, but trimming and de-duplicating names can leave fewer.
        if (players.size < MIN_PLAYERS) {
            logW("initialize aborted reason=not_enough_players players=${players.size}")
            updateUi {
                it.copy(
                    isLoading = false,
                    errorMessage = context.getString(R.string.core_resources_game_error_min_players)
                )
            }
            return@execute
        }

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
                showNumOfImpostors = intent.showNumOfImpostors,
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

    private fun nextRevealPlayer() = execute {
        // The flag is set while the transition is applied, in the same coroutine that starts the
        // timer. Reset on every run because `update` may retry the lambda.
        var startTimer = false
        _uiState.update { state ->
            startTimer = false
            if (state.step != OfflineGameStep.Reveal || !state.revealedCurrentCard) {
                logW(
                    "nextRevealPlayer ignored step=${state.step} " +
                        "revealedCurrentCard=${state.revealedCurrentCard}"
                )
                return@update state
            }

            if (!state.hasMoreRevealPlayers) {
                logI(
                    "nextRevealPlayer transition=Discussion startingSpeakerIndex=${state.currentSpeakerIndex} " +
                        "startingSpeaker=${state.players[state.currentSpeakerIndex]}"
                )
                startTimer = state.discussionTimerSeconds > 0
                return@update state.copy(
                    step = OfflineGameStep.Discussion,
                    timerSecondsLeft = state.discussionTimerSeconds,
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
        if (startTimer) startDiscussionTimer()
    }

    // Only delays and cheap state updates, so it does not need Dispatchers.IO.
    private fun startDiscussionTimer() = execute {
        logD("startDiscussionTimer started seconds=${_uiState.value.timerSecondsLeft}")
        while (true) {
            delay(1000L)
            val current = _uiState.value
            if (current.step != OfflineGameStep.Discussion || current.timerSecondsLeft <= 0) break
            _uiState.update { it.copy(timerSecondsLeft = (it.timerSecondsLeft - 1).coerceAtLeast(0)) }
        }
        logD("startDiscussionTimer ended")
    }

    // Side effects (sfx, ranking) run after `update` has settled: its lambda may be retried and must stay pure.
    @Suppress("LongMethod")
    private fun votePlayer(
        playerIndex: Int
    ) = execute {
        var sfx: Sounds? = null
        var finished: OfflineGameState? = null
        _uiState.update { state ->
            sfx = null
            finished = null
            logD(
                "votePlayer attempt index=$playerIndex " +
                    "name=${state.players.getOrNull(playerIndex)} step=${state.step} alive=${state.alivePlayerIndexes.size}"
            )
            if (state.step != OfflineGameStep.Discussion || playerIndex !in state.alivePlayerIndexes) {
                logW(
                    "votePlayer ignored reason=${if (state.step != OfflineGameStep.Discussion) "invalid_step" else "player_not_alive"}"
                )
                return@update state
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

            if (result == null) {
                sfx = Sounds.CLK_CASUAL
                state.copy(
                    alivePlayerIndexes = newAlive,
                    correctVotes = correctVotes,
                    incorrectVotes = incorrectVotes,
                    guessFeedback = voteFeedback(playerName, isImpostor)
                )
            } else {
                logI("votePlayer finished result=${describeResult(result)}")
                sfx = when (result.winner) {
                    OfflineWinner.Civilians -> Sounds.SND_WIN
                    OfflineWinner.Impostors -> Sounds.SND_LOSE
                    OfflineWinner.Tie -> Sounds.SND_BOWING
                }
                state.copy(
                    alivePlayerIndexes = newAlive,
                    correctVotes = correctVotes,
                    incorrectVotes = incorrectVotes,
                    step = OfflineGameStep.Result,
                    result = result,
                    guessFeedback = null
                ).also { finished = it }
            }
        }
        sfx?.let { sound ->
            runCatching { soundPoolManager.play(sound) }
                .onFailure { logW("sfx $sound failed: ${it.message}") }
        }
        finished?.let { done ->
            recordRankingForWinner(
                players = done.players,
                impostorIndexes = done.impostorPlayerIndexes,
                winner = requireNotNull(done.result).winner
            )
        }
    }

    private fun submitGuess(
        value: String
    ) = execute {
        var impostorsWon: OfflineGameState? = null
        _uiState.update { state ->
            impostorsWon = null
            logD(
                "submitGuess attempt raw='${value.trim()}' step=${state.step} hasWord=${state.wordEntry != null}"
            )
            if (state.step != OfflineGameStep.Discussion || state.wordEntry == null) {
                logW("submitGuess ignored reason=invalid_state")
                return@update state
            }

            val guess = normalize(value)
            val solution = normalize(state.wordEntry.word)
            if (guess.isBlank()) {
                logW("submitGuess rejected reason=blank_guess")
                return@update state.copy(
                    guessFeedback = context.getString(R.string.core_resources_game_guess_empty)
                )
            }

            if (guess == solution) {
                logI("submitGuess success winner=Impostors guessedWord='${state.wordEntry.word}'")
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
                ).also { impostorsWon = it }
            } else {
                logD("submitGuess failed normalizedGuess='$guess' solution='$solution'")
                state.copy(guessFeedback = context.getString(R.string.core_resources_game_guess_wrong))
            }
        }
        impostorsWon?.let { done ->
            runCatching { soundPoolManager.play(Sounds.SND_LOSE) }
                .onFailure { logW("sfx guess-win failed: ${it.message}") }
            recordRankingForWinner(
                players = done.players,
                impostorIndexes = done.impostorPlayerIndexes,
                winner = OfflineWinner.Impostors
            )
        }
    }

    private fun goToRating() = updateUi { state ->
        if (state.step != OfflineGameStep.Result) {
            logW("goToRating ignored reason=invalid_step currentStep=${state.step}")
            return@updateUi state
        }
        val impostorClues = state.impostorPlayerIndexes.mapNotNull { playerIdx ->
            val clue = state.wordEntry?.clue
                ?.getOrNull(playerIdx % (state.wordEntry.clue.size.coerceAtLeast(1)))
            clue
        }.distinct()
        val initialRatings = impostorClues.associateWith { true }
        logD("goToRating impostorClues=$impostorClues")
        state.copy(step = OfflineGameStep.Rating, clueRatings = initialRatings, ratingsSubmitted = false)
    }

    private fun rateClue(clueText: String, isPositive: Boolean) = updateUi { state ->
        state.copy(clueRatings = state.clueRatings + (clueText to isPositive))
    }

    private fun submitRatings() = execute(ioDispatcher) {
        val state = _uiState.value
        val word = state.wordEntry?.word.orEmpty()
        val category = state.wordEntry?.category?.name.orEmpty()
        val ratings = state.clueRatings.map { (clue, positive) ->
            ClueRating(word = word, category = category, clue = clue, isPositive = positive)
        }
        logI("submitRatings count=${ratings.size} word='$word'")
        runCatching { submitClueRatingsUseCase(ratings) }
            .onFailure { logW("submitRatings failed: ${it.message}") }
        updateUi { it.copy(ratingsSubmitted = true) }
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
            Modes.Chaos -> Random.nextInt(from = 1, until = requested.coerceAtMost(maxImpostors) + 1)
        }
        logD(
            "resolveImpostorCount mode=$mode requested=$requested playerCount=$playerCount " +
                "max=$maxImpostors resolved=$resolved"
        )
        return resolved
    }

    // Role is only revealed in debug builds; release builds keep the vote result neutral.
    private fun voteFeedback(playerName: String, isImpostor: Boolean): String = context.getString(
        when {
            !BuildConfig.DEBUG -> R.string.core_resources_game_vote_reveal_neutral
            isImpostor -> R.string.core_resources_game_vote_reveal_impostor
            else -> R.string.core_resources_game_vote_reveal_civilian
        },
        playerName
    )

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

        execute(ioDispatcher) {
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


