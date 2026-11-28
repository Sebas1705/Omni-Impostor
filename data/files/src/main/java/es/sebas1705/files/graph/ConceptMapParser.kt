package es.sebas1705.files.graph

import kotlinx.serialization.json.Json
import java.io.InputStream

/** Reads the JSON Lines sources of the concept map (`concepts.jsonl`, `links.jsonl`, optional `ratings.jsonl`). */
object ConceptMapParser {

    private val json = Json { ignoreUnknownKeys = true }

    private inline fun <reified T> lines(stream: InputStream?): List<T> =
        stream?.bufferedReader(Charsets.UTF_8)?.use { reader ->
            reader.lineSequence()
                .map { it.removePrefix("\uFEFF").trim() }
                .filter { it.isNotEmpty() }
                .map { json.decodeFromString<T>(it) }
                .toList()
        }.orEmpty()

    fun parse(concepts: InputStream, links: InputStream, ratings: InputStream? = null): ConceptMap =
        ConceptMap(lines(concepts), lines(links), lines(ratings))
}
