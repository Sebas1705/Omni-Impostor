package es.sebas1705.offlinegame.viewmodel

import es.sebas1705.core.resources.R
import es.sebas1705.core.resources.Sounds
import es.sebas1705.domain.managers.MediaPlayerManager
import es.sebas1705.domain.managers.SoundPoolManager
import es.sebas1705.game.words.SearchWordsUseCase
import es.sebas1705.game.words.SubmitClueRatingsUseCase
import es.sebas1705.models.Categories
import es.sebas1705.models.ClueRating
import es.sebas1705.models.Modes
import es.sebas1705.models.WordModel
import es.sebas1705.offlinegame.models.OfflineGameStep
import es.sebas1705.offlinegame.models.OfflineWinner
import es.sebas1705.ranking.RecordOfflineMatchResultUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub
import org.mockito.kotlin.timeout
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.verifyNoInteractions
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/**
 * Main is a [StandardTestDispatcher], like the real Main dispatcher (not immediate): work launched
 * with `execute`/`updateUi` only runs when the test calls [settle]. That is what exposed the
 * discussion-timer race, so these tests must not use an unconfined Main.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class OfflineGameViewModelTest {

    private val scheduler = TestCoroutineScheduler()
    private val collectors = mutableListOf<Job>()

    private val searchWords: SearchWordsUseCase = mock()
    private val recordMatch: RecordOfflineMatchResultUseCase = mock()
    private val submitRatings: SubmitClueRatingsUseCase = mock()
    private val mediaPlayer: MediaPlayerManager = mock()
    private val soundPool: SoundPoolManager = mock()

    private val word = WordModel(
        word = "Árbol",
        clue = listOf("hojas", "raíces", "sombra", "madera"),
        category = Categories.ANIMALS,
    )
    private val names = listOf("Ana", "Beto", "Carla", "Dani")

    private lateinit var vm: OfflineGameViewModel

    private val state get() = vm.uiState.value

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher(scheduler))
        searchWords.stub { onBlocking { invoke(any()) } doReturn listOf(word) }
        submitRatings.stub { onBlocking { invoke(any()) } doReturn true }
        vm = newViewModel()
    }

    @After
    fun tearDown() {
        collectors.forEach { it.cancel() }
        Dispatchers.resetMain()
    }

    // region timer

    @Test
    fun `discussion timer starts counting down once the last reveal is done`() {
        startDiscussion(timerSeconds = 3)

        assertEquals(OfflineGameStep.Discussion, state.step)
        assertEquals(3, state.timerSecondsLeft)

        tick(1_000)
        assertEquals(2, state.timerSecondsLeft)
        tick(2_000)
        assertEquals(0, state.timerSecondsLeft)
        tick(5_000)
        assertEquals(0, state.timerSecondsLeft)
    }

    @Test
    fun `discussion timer does not start when the timer is disabled`() {
        startDiscussion(timerSeconds = 0)

        tick(5_000)

        assertEquals(OfflineGameStep.Discussion, state.step)
        assertEquals(0, state.timerSecondsLeft)
    }

    @Test
    fun `discussion timer stops when the game ends`() {
        startDiscussion(timerSeconds = 30)
        send(OfflineGameIntent.SubmitGuess("arbol"))
        assertEquals(OfflineGameStep.Result, state.step)
        val left = state.timerSecondsLeft

        tick(5_000)

        assertEquals(left, state.timerSecondsLeft)
    }

    // endregion

    // region reveal

    @Test
    fun `reveal walks every player once and then moves to discussion`() {
        init()
        assertEquals(OfflineGameStep.Reveal, state.step)

        names.indices.forEach { index ->
            assertEquals(index, state.currentRevealIndex)
            send(OfflineGameIntent.NextRevealPlayer)
            assertEquals("next before the card is revealed must be ignored", index, state.currentRevealIndex)
            send(OfflineGameIntent.MarkRevealDone)
            send(OfflineGameIntent.NextRevealPlayer)
        }

        assertEquals(OfflineGameStep.Discussion, state.step)
        assertNotNull(state.guessFeedback)
    }

    // endregion

    // region voting

    @Test
    fun `voting out the only impostor makes civilians win and records the ranking once`() {
        startDiscussion()
        val impostor = state.impostorPlayerIndexes.single()

        send(OfflineGameIntent.VotePlayer(impostor))

        val result = requireNotNull(state.result)
        assertEquals(OfflineGameStep.Result, state.step)
        assertEquals(OfflineWinner.Civilians, result.winner)
        assertEquals(1, result.correctVotes)
        assertEquals(0, result.incorrectVotes)
        verify(soundPool).play(Sounds.SND_WIN, 1f)
        verifyBlocking(recordMatch, timeout(2_000).times(1)) {
            invoke(
                eq(names.toSet()),
                eq(names.filterIndexed { i, _ -> i != impostor }.toSet()),
                eq(emptySet()),
            )
        }
    }

    @Test
    fun `voting a civilian keeps playing and does not record the ranking`() {
        startDiscussion()
        val civilian = (state.alivePlayerIndexes - state.impostorPlayerIndexes).first()

        send(OfflineGameIntent.VotePlayer(civilian))

        assertEquals(OfflineGameStep.Discussion, state.step)
        assertFalse(civilian in state.alivePlayerIndexes)
        assertEquals(1, state.incorrectVotes)
        assertNull(state.result)
        verify(soundPool).play(Sounds.CLK_CASUAL, 1f)
        verifyNoInteractions(recordMatch)
    }

    @Test
    fun `final duel between one civilian and one impostor is a tie`() {
        startDiscussion()
        val civilians = (state.alivePlayerIndexes - state.impostorPlayerIndexes).toList()

        send(OfflineGameIntent.VotePlayer(civilians[0]))
        assertEquals(OfflineGameStep.Discussion, state.step)
        send(OfflineGameIntent.VotePlayer(civilians[1]))

        assertEquals(OfflineGameStep.Result, state.step)
        assertEquals(OfflineWinner.Tie, requireNotNull(state.result).winner)
        verify(soundPool).play(Sounds.SND_BOWING, 1f)
        verifyBlocking(recordMatch, timeout(2_000).times(1)) {
            invoke(eq(names.toSet()), eq(emptySet()), eq(emptySet()))
        }
    }

    @Test
    fun `impostors win when they outnumber the remaining civilians`() {
        startDiscussion(
            players = listOf("A", "B", "C", "D", "E"),
            impostors = 2,
        )
        val civilians = (state.alivePlayerIndexes - state.impostorPlayerIndexes).toList()

        send(OfflineGameIntent.VotePlayer(civilians[0]))
        assertEquals(OfflineGameStep.Discussion, state.step)
        send(OfflineGameIntent.VotePlayer(civilians[1]))

        assertEquals(OfflineWinner.Impostors, requireNotNull(state.result).winner)
        verify(soundPool).play(Sounds.SND_LOSE, 1f)
        val impostorNames = state.impostorPlayerIndexes.map { state.players[it] }.toSet()
        verifyBlocking(recordMatch, timeout(2_000).times(1)) {
            invoke(eq(setOf("A", "B", "C", "D", "E")), eq(emptySet()), eq(impostorNames))
        }
    }

    @Test
    fun `voting the same player twice or outside the discussion is ignored`() {
        init()
        send(OfflineGameIntent.VotePlayer(0))
        assertEquals("vote during reveal", names.indices.toSet(), state.alivePlayerIndexes)

        startDiscussion()
        val civilian = (state.alivePlayerIndexes - state.impostorPlayerIndexes).first()
        send(OfflineGameIntent.VotePlayer(civilian))
        val afterFirst = state

        send(OfflineGameIntent.VotePlayer(civilian))

        assertEquals(afterFirst.alivePlayerIndexes, state.alivePlayerIndexes)
        assertEquals(afterFirst.incorrectVotes, state.incorrectVotes)
    }

    // endregion

    // region guess

    @Test
    fun `a correct guess ignoring case and accents makes impostors win`() {
        startDiscussion()

        send(OfflineGameIntent.SubmitGuess("  ARBOL "))

        val result = requireNotNull(state.result)
        assertEquals(OfflineGameStep.Result, state.step)
        assertEquals(OfflineWinner.Impostors, result.winner)
        assertEquals("Árbol", result.word)
        verify(soundPool).play(Sounds.SND_LOSE, 1f)
        val impostorNames = state.impostorPlayerIndexes.map { state.players[it] }.toSet()
        verifyBlocking(recordMatch, timeout(2_000).times(1)) {
            invoke(eq(names.toSet()), eq(emptySet()), eq(impostorNames))
        }
    }

    @Test
    fun `a wrong or blank guess keeps the discussion going with feedback`() {
        startDiscussion()

        send(OfflineGameIntent.SubmitGuess("roble"))
        assertEquals(OfflineGameStep.Discussion, state.step)
        assertEquals(context.getString(R.string.core_resources_game_guess_wrong), state.guessFeedback)

        send(OfflineGameIntent.SubmitGuess("   "))
        assertEquals(OfflineGameStep.Discussion, state.step)
        assertEquals(context.getString(R.string.core_resources_game_guess_empty), state.guessFeedback)
        verifyNoInteractions(recordMatch)
    }

    // endregion

    // region setup

    @Test
    fun `classic mode clamps the requested impostors between 1 and players minus 1`() {
        assertEquals(3, impostorsFor(Modes.Classic, requested = 10, players = names))
        assertEquals(1, impostorsFor(Modes.Classic, requested = 0, players = names))
        assertEquals(2, impostorsFor(Modes.Classic, requested = 2, players = listOf("A", "B", "C", "D", "E")))
    }

    @Test
    fun `chaos mode picks between 1 and the capped requested impostors`() {
        val five = listOf("A", "B", "C", "D", "E")
        val counts = List(25) { impostorsFor(Modes.Chaos, requested = 3, players = five) }
        assertTrue("counts=$counts", counts.all { it in 1..3 })

        val capped = List(25) { impostorsFor(Modes.Chaos, requested = 10, players = names) }
        assertTrue("capped=$capped", capped.all { it in 1..3 })
    }

    @Test
    fun `duplicated names that leave fewer than 3 players are rejected`() {
        send(initialize(listOf("Ana", " Ana ", "Ana", "Beto")))
        awaitUntil { it.errorMessage != null }

        assertEquals(context.getString(R.string.core_resources_game_error_min_players), state.errorMessage)
        assertNull(state.wordEntry)
        assertFalse(state.isLoading)
        verifyNoInteractions(searchWords)
    }

    @Test
    fun `names are trimmed and de-duplicated before the game starts`() {
        send(initialize(listOf(" Ana", "Beto ", "Ana", "Carla", "  ")))
        awaitUntil { it.wordEntry != null }

        assertEquals(listOf("Ana", "Beto", "Carla"), state.players)
    }

    @Test
    fun `no words available shows an error and does not start`() {
        searchWords.stub { onBlocking { invoke(any()) } doReturn emptyList() }

        send(initialize(names))
        awaitUntil { it.errorMessage != null }

        assertEquals(context.getString(R.string.core_resources_game_error_no_words), state.errorMessage)
        assertNull(state.wordEntry)
    }

    @Test
    fun `initialize twice keeps the first game`() {
        init()
        val first = state

        send(initialize(listOf("X", "Y", "Z")))
        settleIo()

        assertEquals(first.players, state.players)
        assertEquals(first.impostorPlayerIndexes, state.impostorPlayerIndexes)
    }

    // endregion

    // region rating

    @Test
    fun `ratings start positive for every impostor clue and are submitted once`() {
        startDiscussion()
        send(OfflineGameIntent.SubmitGuess("arbol"))
        send(OfflineGameIntent.GoToRating)

        val impostorClues = state.impostorPlayerIndexes.map { word.clue[it % word.clue.size] }
        assertEquals(OfflineGameStep.Rating, state.step)
        assertEquals(impostorClues.associateWith { true }, state.clueRatings)

        val disliked = impostorClues.first()
        send(OfflineGameIntent.RateClue(disliked, isPositive = false))
        send(OfflineGameIntent.SubmitRatings)
        awaitUntil { it.ratingsSubmitted }

        val expected = impostorClues.map {
            ClueRating(word = "Árbol", category = "ANIMALS", clue = it, isPositive = it != disliked)
        }
        verifyBlocking(submitRatings, timeout(2_000).times(1)) { invoke(eq(expected)) }
    }

    @Test
    fun `going to rating before the game is over is ignored`() {
        startDiscussion()

        send(OfflineGameIntent.GoToRating)

        assertEquals(OfflineGameStep.Discussion, state.step)
        assertTrue(state.clueRatings.isEmpty())
    }

    // endregion

    // region helpers

    private val context get() = RuntimeEnvironment.getApplication()

    private fun newViewModel(): OfflineGameViewModel = OfflineGameViewModel(
        context = context,
        searchWordsUseCase = searchWords,
        recordOfflineMatchResultUseCase = recordMatch,
        submitClueRatingsUseCase = submitRatings,
        mediaPlayerManager = mediaPlayer,
        soundPoolManager = soundPool,
    ).also { created ->
        // uiState is a WhileSubscribed stateIn: without a subscriber `.value` never moves.
        collectors += CoroutineScope(Dispatchers.Unconfined).launch { created.uiState.collect() }
        settle()
    }

    private fun initialize(
        players: List<String>,
        mode: Modes = Modes.Classic,
        impostors: Int = 1,
        timerSeconds: Int = 3,
    ) = OfflineGameIntent.Initialize(
        players = players,
        categories = setOf(Categories.ANIMALS),
        mode = mode,
        impostors = impostors,
        discussionTimerSeconds = timerSeconds,
        impostorsKnowEachOther = false,
        showNumOfImpostors = false,
    )

    private fun init(players: List<String> = names, impostors: Int = 1, timerSeconds: Int = 3) {
        send(initialize(players, impostors = impostors, timerSeconds = timerSeconds))
        awaitUntil { it.wordEntry != null }
    }

    private fun startDiscussion(
        players: List<String> = names,
        impostors: Int = 1,
        timerSeconds: Int = 3,
    ) {
        init(players, impostors, timerSeconds)
        repeat(players.size) {
            send(OfflineGameIntent.MarkRevealDone)
            send(OfflineGameIntent.NextRevealPlayer)
        }
        assertEquals(OfflineGameStep.Discussion, state.step)
    }

    private fun impostorsFor(mode: Modes, requested: Int, players: List<String>): Int {
        vm = newViewModel()
        send(initialize(players, mode = mode, impostors = requested))
        awaitUntil { it.wordEntry != null }
        return state.impostorPlayerIndexes.size
    }

    private fun send(intent: OfflineGameIntent) {
        vm.eventHandler(intent)
        settle()
    }

    private fun settle() = scheduler.runCurrent()

    private fun tick(millis: Long) {
        scheduler.advanceTimeBy(millis)
        settle()
    }

    /** `initialize` runs on Dispatchers.IO (real time); give it a moment, pumping Main meanwhile. */
    private fun settleIo() {
        val until = System.nanoTime() + 300_000_000L
        while (System.nanoTime() < until) {
            settle()
            Thread.sleep(5)
        }
    }

    private fun awaitUntil(condition: (OfflineGameState) -> Boolean) {
        val deadline = System.nanoTime() + 5_000_000_000L
        while (!condition(state)) {
            check(System.nanoTime() < deadline) { "Timed out waiting for state, last=$state" }
            Thread.sleep(5)
            settle()
        }
    }

    // endregion
}
