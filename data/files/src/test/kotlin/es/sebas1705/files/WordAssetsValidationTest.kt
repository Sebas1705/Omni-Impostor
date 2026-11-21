package es.sebas1705.files

import es.sebas1705.files.json.WordsJson
import kotlinx.serialization.json.Json
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.text.Normalizer

/**
 * Guards the shape rules of the bundled word lists (docs/studies/word-collection/word-model-study.md, section 1):
 * a word is one concept written with at most 3 words, every word has at least 3 distinct clues, and no clue gives
 * the word away. The impostor sees one clue, so a clue equal to the word would reveal it.
 */
class WordAssetsValidationTest {

    private class Entry(val file: String, val word: String, val clues: List<String>, val category: String)

    private val assetsDir: File = listOf("src/main/assets", "data/files/src/main/assets")
        .map(::File)
        .first { it.isDirectory }

    private val entries: List<Entry> by lazy {
        val json = Json { ignoreUnknownKeys = true }
        assetsDir.listFiles { f -> f.name.matches(FILE_NAME) }.orEmpty().sortedBy { it.name }.flatMap { file ->
            json.decodeFromString<WordsJson>(file.readText().removePrefix("﻿")).words
                .map { Entry(file.name, it.word, it.clues, it.category) }
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

    private fun Entry.label() = "$file: \"$word\""

    @Test
    fun `there is a list for every category in both languages`() {
        val files = assetsDir.listFiles { f -> f.name.matches(FILE_NAME) }.orEmpty().map { it.name }
        val categories = files.map { it.substringBeforeLast('_') }.toSet()
        val missing = categories.flatMap { c -> LANGUAGES.map { "${c}_$it.json" } }.filter { it !in files }
        assertNone("Each category must exist in en and es", missing)
        assertTrue("No word lists found in $assetsDir", files.isNotEmpty())
    }

    @Test
    fun `entries declare the category of the file they live in`() {
        assertNone(
            "category field must match the file name",
            entries.filter { it.category != it.file.substringBeforeLast('_') }.map { "${it.label()} says \"${it.category}\"" }
        )
    }

    @Test
    fun `words are not blank and have at most 3 words`() {
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
        assertNone(
            "Blank clue",
            entries.filter { e -> e.clues.any { it.isBlank() } }.map { it.label() }
        )
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
    fun `no clue gives the word away`() {
        assertNone(
            "A clue must not equal its word",
            entries.filter { e -> e.clues.any { normalize(it) == normalize(e.word) } }.map { "${it.label()} ${it.clues}" }
        )
    }

    @Test
    fun `a word appears once per list`() {
        val duplicates = entries.groupBy { it.file to normalize(it.word) }
            .filterValues { it.size > 1 }
            .map { (key, list) -> "${key.first}: \"${list.first().word}\" x${list.size}" }
        assertNone("Duplicate word inside a list", duplicates)
    }

    private companion object {
        val FILE_NAME = Regex("^.+_(en|es)\\.json$")
        val LANGUAGES = listOf("en", "es")
        val DIACRITICS = Regex("\\p{M}+")
        val WHITESPACE = Regex("\\s+")
        val SEPARATORS = setOf('「', '」', '|', ';')
        const val MAX_WORDS = 3
        const val MIN_CLUES = 3
        const val MAX_LISTED = 30
    }
}
