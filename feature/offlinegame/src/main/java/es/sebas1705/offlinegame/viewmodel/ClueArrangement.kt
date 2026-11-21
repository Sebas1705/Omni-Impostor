package es.sebas1705.offlinegame.viewmodel

import kotlin.random.Random

/**
 * Decides which clue each impostor gets. The word lists rank a word's clues best first (player ratings, hub clues and
 * clue types feed that ranking), and the game gives player `i` the clue at position `i % size`. Instead of always
 * handing the first clues to the first players, the clues the impostors will use are drawn at random with a weight that
 * falls with the rank, so good clues are likely but not certain, and every impostor still gets a different clue
 * while there are clues left.
 */
internal object ClueArrangement {

    /** Weight of the clue at rank 0 is 1; every following rank is worth this share of the previous one. */
    private const val RANK_DECAY = 0.75

    /**
     * Returns the clues of a word in the order the game must read them: the positions the [impostorIndexes] map to
     * hold a weighted random draw, the others keep the remaining clues in their ranked order.
     */
    fun arrange(clues: List<String>, impostorIndexes: Set<Int>, random: Random = Random.Default): List<String> {
        val size = clues.size
        if (size < 2) return clues
        val remaining = clues.indices.toMutableList()
        val placed = arrayOfNulls<Int>(size)
        impostorIndexes.sorted().forEach { player ->
            val position = player % size
            if (placed[position] != null || remaining.isEmpty()) return@forEach
            val pick = drawByRank(remaining, random)
            placed[position] = pick
            remaining.remove(pick)
        }
        val rest = remaining.iterator()
        return List(size) { position -> clues[placed[position] ?: rest.next()] }
    }

    private fun drawByRank(ranks: List<Int>, random: Random): Int {
        val weights = ranks.map { Math.pow(RANK_DECAY, it.toDouble()) }
        var point = random.nextDouble() * weights.sum()
        ranks.forEachIndexed { i, rank ->
            point -= weights[i]
            if (point <= 0) return rank
        }
        return ranks.last()
    }
}
