package es.sebas1705.offlinegame.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class ClueArrangementTest {

    private val clues = listOf("best", "good", "fine", "ok", "weak")

    @Test
    fun `the arrangement keeps every clue exactly once`() {
        repeat(50) { seed ->
            val arranged = ClueArrangement.arrange(clues, setOf(1, 3), Random(seed))
            assertEquals(clues.sorted(), arranged.sorted())
        }
    }

    @Test
    fun `impostors get different clues while there are clues left`() {
        repeat(50) { seed ->
            val impostors = setOf(0, 2, 4)
            val arranged = ClueArrangement.arrange(clues, impostors, Random(seed))
            val given = impostors.map { arranged[it % arranged.size] }
            assertEquals(given.size, given.toSet().size)
        }
    }

    @Test
    fun `better ranked clues are drawn more often`() {
        val counts = clues.associateWith { 0 }.toMutableMap()
        val random = Random(7)
        repeat(4_000) {
            val first = ClueArrangement.arrange(clues, setOf(0), random)[0]
            counts[first] = counts.getValue(first) + 1
        }
        assertTrue(counts.getValue("best") > counts.getValue("good"))
        assertTrue(counts.getValue("good") > counts.getValue("weak"))
        assertTrue("every clue can still be drawn", counts.values.all { it > 0 })
    }

    @Test
    fun `players that share a position get the same clue`() {
        val arranged = ClueArrangement.arrange(clues, setOf(1, 6), Random(3))
        assertEquals(arranged[1 % 5], arranged[6 % 5])
    }

    @Test
    fun `one clue or none is left alone`() {
        assertEquals(listOf("only"), ClueArrangement.arrange(listOf("only"), setOf(0)))
        assertEquals(emptyList<String>(), ClueArrangement.arrange(emptyList(), setOf(0)))
    }
}
