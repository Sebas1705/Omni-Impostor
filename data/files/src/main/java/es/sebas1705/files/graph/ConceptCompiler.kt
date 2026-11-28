package es.sebas1705.files.graph

import es.sebas1705.files.json.WordJson
import java.text.Normalizer
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * Turns the concept map into the playable words of one language: each concept becomes a word whose clues are its
 * hints and the labels of the concepts it links to, ranked and trimmed (spec section 6).
 *
 * This is a port of `scripts/words/lib.mjs` (`compileConcept` and `compileAll`). Both must give the same words for the
 * same map: `ConceptCompilerParityTest` and `scripts/words/words.test.mjs` check them against one shared fixture.
 *
 * Compiling is cheap per concept but the map has thousands of them, so [compile] can be limited to some categories
 * (a game only needs the ones the players picked) and the text normalisation is memoised. An instance is not
 * thread-safe: use one per thread or guard it.
 */
class ConceptCompiler(private val map: ConceptMap) {

    private class Candidate(val text: String, val type: String, val w: Int, val factor: Double) {
        val score: Double get() = (w / 5.0) * factor
    }

    private class Compiled(val label: String, val clues: List<String>, val reveal: List<Int>)

    private val byId = map.concepts.associateBy { it.id }
    private val approvedLinks = map.links.filter { approved(it.status) }
    private val outgoing: Map<String, List<ConceptLink>> = approvedLinks.groupBy { it.from }
    private val inDegree: Map<String, Int> = approvedLinks.groupingBy { it.to }.eachCount()
    private val hasRatings = map.ratings.isNotEmpty()
    private val normCache = HashMap<String, String>()

    /** [norm], remembered: the same labels come back as clues of many concepts. */
    private fun n(text: String): String = normCache.getOrPut(text) { norm(text) }

    private val ratings: Map<String, Double> by lazy {
        map.ratings.associate { "${it.id}|${it.lang}|${n(it.clue)}" to ratingFactor(it.pos, it.neg) }
    }

    private fun ratingOf(id: String, lang: String, text: String): Double =
        if (hasRatings) ratings["$id|$lang|${n(text)}"] ?: 1.0 else 1.0

    /**
     * The label of a concept in a language. A proper name (`translatable: false`) without a label of its own uses the
     * text of another language.
     */
    private fun labelOf(concept: Concept?, lang: String): ConceptLabel? {
        if (concept == null) return null
        concept.labels[lang]?.let { return it }
        if (concept.translatable) return null
        return concept.labels["es"] ?: concept.labels["en"] ?: concept.labels.values.firstOrNull()
    }

    private fun compileConcept(concept: Concept, lang: String): Compiled? {
        val label = labelOf(concept, lang) ?: return null
        val own = HashSet<String>().apply {
            add(n(label.text))
            label.aliases.forEach { add(n(it)) }
        }
        val candidates = ArrayList<Candidate>()
        for (link in outgoing[concept.id].orEmpty()) {
            if (link.langs != null && lang !in link.langs) continue
            val target = labelOf(byId[link.to], lang) ?: continue
            val hub = min(1.0, sqrt(HUB_K / max(inDegree[link.to] ?: 1, 1)))
            candidates += Candidate(target.text, link.type, link.w, hub * ratingOf(concept.id, lang, target.text))
        }
        for (hint in concept.hints[lang].orEmpty()) {
            if (!approved(hint.status)) continue
            candidates += Candidate(hint.t, hint.type, hint.w, ratingOf(concept.id, lang, hint.t))
        }
        val seen = HashSet<String>()
        val usable = candidates
            .filter { it.type in REVEAL } // an unknown type is dropped, as in lib.mjs
            .filter { n(it.text) !in own } // never a clue that is the word itself
            .filter { seen.add(n(it.text)) }
            .sortedWith(compareByDescending<Candidate> { it.score }.thenBy { it.text })
        val clues = usable.take(MAX_CLUES).toMutableList()
        while (clues.size > KEEP_AT_LEAST && clues.last().score < FLOOR) clues.removeAt(clues.lastIndex)
        return Compiled(label.text, clues.map { it.text }, clues.map { REVEAL.getValue(it.type) })
    }

    /**
     * Compiles the concepts for [lang]. A concept in several categories gives one entry per category (the app merges
     * them by `conceptId`). Concepts without a label in [lang], marked `clueOnly`, or with fewer than three clues are
     * left out. With [categories] only those categories are compiled (null means all). Entries are sorted by word then
     * concept id, in UTF-16 code unit order like the Node compiler.
     */
    fun compile(lang: String, categories: Set<String>? = null): List<WordJson> {
        val words = ArrayList<WordJson>()
        for (concept in map.concepts) {
            if (!approved(concept.status) || concept.clueOnly) continue
            val wanted = if (categories == null) concept.categories else concept.categories.filter { it in categories }
            if (wanted.isEmpty()) continue
            val compiled = compileConcept(concept, lang) ?: continue
            if (compiled.clues.size < MIN_CLUES) continue
            // one digit per clue, only written when some clue is not level 1 (keeps the list small)
            val reveal = if (compiled.reveal.all { it == 1 }) null else compiled.reveal.joinToString("")
            for (category in wanted) {
                words += WordJson(compiled.label, compiled.clues, category, reveal, concept.id)
            }
        }
        return words.sortedWith(compareBy<WordJson> { it.word }.thenBy { it.conceptId })
    }

    companion object {

        val LANGUAGES = listOf("es", "en")

        private const val MAX_CLUES = 8
        private const val MIN_CLUES = 3
        private const val FLOOR = 0.2
        private const val HUB_K = 8.0

        /** Weak trailing clues are trimmed only while more than this many remain. */
        private const val KEEP_AT_LEAST = 4

        private const val RATING_MIN_VOTES = 5
        private const val RATING_Z = 1.645
        private const val RATING_FLOOR = 0.25

        /** How much a clue gives the word away: 1 weak, 2 medium, 3 strong. */
        private val REVEAL = mapOf("association" to 1, "context" to 1, "contrast" to 2, "broader" to 2, "part" to 2, "synonym" to 3)

        private val DIACRITICS = Regex("\\p{M}+")
        private val SPACES = Regex("\\s+")
        private val ARTICLE = Regex("^(the|el|la|los|las|un|una)\\s")

        /** Compiles every concept for [lang] (see [compile]). */
        fun compile(map: ConceptMap, lang: String): List<WordJson> = ConceptCompiler(map).compile(lang)

        /** Same normalisation as `norm` in lib.mjs: no accents or case, single spaces, no leading article. */
        fun norm(s: String): String =
            Normalizer.normalize(s, Normalizer.Form.NFD)
                .replace(DIACRITICS, "")
                .lowercase()
                .trim()
                .replace(SPACES, " ")
                .replaceFirst(ARTICLE, "")

        private fun approved(status: String) = status == "approved"

        /** Lower bound of the Wilson interval of the share of positive votes. */
        internal fun wilsonLowerBound(pos: Int, total: Int, z: Double = RATING_Z): Double {
            if (total <= 0) return 0.0
            val p = pos.toDouble() / total
            val z2 = z * z
            return (p + z2 / (2 * total) - z * sqrt((p * (1 - p) + z2 / (4 * total)) / total)) / (1 + z2 / total)
        }

        /** 1 until there are enough votes; then 0.25 plus the lower bound, capped at 1. */
        internal fun ratingFactor(pos: Int, neg: Int): Double {
            val total = pos + neg
            return if (total < RATING_MIN_VOTES) 1.0 else min(1.0, RATING_FLOOR + wilsonLowerBound(pos, total))
        }
    }
}
