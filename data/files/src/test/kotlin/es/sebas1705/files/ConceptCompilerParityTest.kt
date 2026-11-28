package es.sebas1705.files

import es.sebas1705.files.graph.ConceptCompiler
import es.sebas1705.files.graph.ConceptMapParser
import es.sebas1705.files.json.WordsJson
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

/**
 * The Kotlin compiler (used by the app) and `scripts/words/lib.mjs` (used to author and report) must produce the same
 * words from the same map. `scripts/words/fixtures/graph` holds a small map that exercises every rule (hubs, aliases,
 * proper names, clue-only topics, language-restricted links, ratings, drafts, unknown link types) and the lists Node
 * compiles it to; `node scripts/words/compile.mjs --check` keeps those lists current.
 */
class ConceptCompilerParityTest {

    private val dir: File = listOf("../../scripts/words/fixtures/graph", "scripts/words/fixtures/graph")
        .map(::File).first { it.isDirectory }

    private val json = Json { ignoreUnknownKeys = true }

    private val map by lazy {
        ConceptMapParser.parse(
            File(dir, "concepts.jsonl").inputStream(),
            File(dir, "links.jsonl").inputStream(),
            File(dir, "ratings.jsonl").inputStream()
        )
    }

    private fun expected(lang: String) =
        json.decodeFromString<WordsJson>(File(dir, "expected_$lang.json").readText().removePrefix("﻿")).words

    @Test
    fun `spanish words equal the node compile`() = assertEquals(expected("es"), ConceptCompiler.compile(map, "es"))

    @Test
    fun `english words equal the node compile`() = assertEquals(expected("en"), ConceptCompiler.compile(map, "en"))
}
