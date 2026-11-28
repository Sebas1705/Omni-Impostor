package es.sebas1705.files

import es.sebas1705.files.graph.ConceptCompiler
import es.sebas1705.files.graph.ConceptMapParser
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.text.Normalizer

/**
 * Guards the shape rules of the words the app plays (docs/studies/word-collection/word-model-study.md, section 1):
 * a word is one concept written with at most 4 words, every word has at least 3 distinct clues, and no clue gives
 * the word away. The impostor sees one clue, so a clue equal to the word would reveal it.
 *
 * The words are what the app compiles on the device from the bundled concept map, so this is the release check of
 * the data: it compiles the real map with the same [ConceptCompiler] and validates the result.
 */
class CompiledWordsValidationTest {

    private class Entry(
        val lang: String,
        val word: String,
        val clues: List<String>,
        val category: String,
        val reveal: String?,
        val conceptId: String?,
    ) {
        fun label() = "$lang/$category: \"$word\" ($conceptId)"
    }

    private val dir: File = listOf("concepts", "data/files/concepts").map(::File).first { it.isDirectory }

    private val entries: List<Entry> by lazy {
        val map = ConceptMapParser.parse(File(dir, "concepts.jsonl").inputStream(), File(dir, "links.jsonl").inputStream())
        ConceptCompiler.LANGUAGES.flatMap { lang ->
            ConceptCompiler.compile(map, lang).map { Entry(lang, it.word, it.clues, it.category, it.reveal, it.conceptId) }
        }
    }

    private fun normalize(text: String): String =
        Normalizer.normalize(text, Normalizer.Form.NFD)
            .replace(DIACRITICS, "")
            .lowercase()
            .trim()
            .replace(WHITESPACE, " ")

    private fun assertNone(rule: String, offenders: List<String>) {
        assertTrue(
            "$rule: ${offenders.size} violation(s)\n" + offenders.take(MAX_LISTED).joinToString("\n") { " - $it" } +
                if (offenders.size > MAX_LISTED) "\n ... and ${offenders.size - MAX_LISTED} more" else "",
            offenders.isEmpty()
        )
    }

    @Test
    fun `the map compiles to words in both languages`() {
        ConceptCompiler.LANGUAGES.forEach { lang ->
            assertTrue("No words compiled for $lang", entries.any { it.lang == lang })
        }
    }

    @Test
    fun `every category has words in both languages`() {
        val categories = entries.map { it.category }.toSet()
        val missing = categories.flatMap { c -> ConceptCompiler.LANGUAGES.map { c to it } }
            .filter { (c, l) -> entries.none { it.category == c && it.lang == l } }
            .map { (c, l) -> "${c}_$l" }
        assertNone("Each category must have words in en and es", missing)
    }

    @Test
    fun `words are not blank and have at most 4 words`() {
        assertNone("Blank word", entries.filter { it.word.isBlank() }.map { it.label() })
        assertNone(
            "A word has at most $MAX_WORDS words",
            entries.filter { it.word.trim().split(WHITESPACE).size > MAX_WORDS }.map { it.label() }
        )
    }

    @Test
    fun `words carry no parentheses`() {
        assertNone("No parentheses in a word (aliases are clues)", entries.filter { '(' in it.word || ')' in it.word }.map { it.label() })
    }

    @Test
    fun `every word has at least 3 distinct non blank clues`() {
        assertNone(
            "At least $MIN_CLUES clues",
            entries.filter { it.clues.count { c -> c.isNotBlank() } < MIN_CLUES }.map { "${it.label()} has ${it.clues.size}" }
        )
        assertNone("Blank clue", entries.filter { e -> e.clues.any { it.isBlank() } }.map { it.label() })
        assertNone(
            "Repeated clue",
            entries.filter { e -> e.clues.map(::normalize).let { it.size != it.toSet().size } }.map { "${it.label()} ${it.clues}" }
        )
    }

    @Test
    fun `clues are single clues without stray separators`() {
        assertNone(
            "A clue holds one clue (no 「」, | or ; separators)",
            entries.flatMap { e -> e.clues.filter { c -> c.any { it in SEPARATORS } }.map { "${e.label()} -> \"$it\"" } }
        )
        assertNone(
            "Clue with leading/trailing spaces",
            entries.flatMap { e -> e.clues.filter { it != it.trim() }.map { "${e.label()} -> \"$it\"" } }
        )
    }

    @Test
    fun `reveal has one digit from 1 to 3 per clue`() {
        assertNone(
            "reveal must match the clues",
            entries.filter { e -> e.reveal != null && (e.reveal.length != e.clues.size || e.reveal.any { it !in '1'..'3' }) }
                .map { "${it.label()} reveal=${it.reveal} clues=${it.clues.size}" }
        )
    }

    @Test
    fun `no clue gives the word away`() {
        assertNone(
            "A clue must not equal its word",
            entries.filter { e -> e.clues.any { normalize(it) == normalize(e.word) } }.map { "${it.label()} ${it.clues}" }
        )
    }

    @Test
    fun `a word appears once per list`() {
        val duplicates = entries.groupBy { Triple(it.lang, it.category, normalize(it.word)) }
            .filterValues { it.size > 1 }
            .map { (key, list) -> "${key.first}/${key.second}: \"${list.first().word}\" x${list.size}" }
        assertNone("Duplicate word inside a list", duplicates)
    }

    private companion object {
        val DIACRITICS = Regex("\\p{M}+")
        val WHITESPACE = Regex("\\s+")
        val SEPARATORS = setOf('「', '」', '|', ';')
        const val MAX_WORDS = 4
        const val MIN_CLUES = 3
        const val MAX_LISTED = 30
    }
}
