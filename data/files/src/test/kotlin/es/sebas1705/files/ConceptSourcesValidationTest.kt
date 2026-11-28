package es.sebas1705.files

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.text.Normalizer

/**
 * Guards the concept map sources (`data/files/concepts`, design in docs/studies/word-collection/concept-map-spec.md,
 * section 7). The words compiled from them are checked by [CompiledWordsValidationTest]; the Kotlin and Node compilers
 * are kept equal by [ConceptCompilerParityTest].
 */
class ConceptSourcesValidationTest {

    private val dir: File = listOf("concepts", "data/files/concepts").map(::File).first { it.isDirectory }
    private val categoriesFile: File = listOf(
        "../../domain/models/src/main/java/es/sebas1705/models/Categories.kt",
        "domain/models/src/main/java/es/sebas1705/models/Categories.kt"
    ).map(::File).first { it.isFile }

    private fun jsonl(name: String): List<JsonObject> =
        File(dir, name).readLines().filter { it.isNotBlank() }.map { Json.parseToJsonElement(it).jsonObject }

    private val concepts by lazy { jsonl("concepts.jsonl") }
    private val links by lazy { jsonl("links.jsonl") }
    /** The categories the app offers: the entries of the `Categories` enum, in storage (lower case) form. */
    private val categories by lazy {
        categoriesFile.readLines().mapNotNull { Regex("^\\s{4}([A-Z][A-Z0-9_]*)\\(").find(it)?.groupValues?.get(1)?.lowercase() }.toSet()
    }

    private fun JsonObject.str(key: String) = this[key]?.jsonPrimitive?.content
    private fun JsonObject.arr(key: String): JsonArray = this[key]?.jsonArray ?: JsonArray(emptyList())
    private fun JsonObject.obj(key: String): JsonObject = this[key]?.jsonObject ?: JsonObject(emptyMap())
    private val JsonObject.id get() = str("id").orEmpty()

    private fun normalize(text: String) =
        Normalizer.normalize(text, Normalizer.Form.NFD).replace(Regex("\\p{M}+"), "").lowercase().trim().replace(Regex("\\s+"), " ")

    private fun assertNone(rule: String, offenders: List<String>) = assertTrue(
        "$rule: ${offenders.size} violation(s)\n" + offenders.take(30).joinToString("\n") { " - $it" },
        offenders.isEmpty()
    )

    @Test
    fun `ids are unique and well formed`() {
        assertNone("Duplicate id", concepts.groupBy { it.id }.filterValues { it.size > 1 }.keys.toList())
        assertNone("Id pattern", concepts.map { it.id }.filter { !ID_PATTERN.matches(it) })
    }

    @Test
    fun `links reference existing concepts and are unique`() {
        val ids = concepts.map { it.id }.toSet()
        assertNone("Dangling link", links.filter { it.str("from") !in ids || it.str("to") !in ids }.map { "${it.str("from")} -> ${it.str("to")}" })
        assertNone("Self link", links.filter { it.str("from") == it.str("to") }.map { it.str("from").orEmpty() })
        assertNone("Duplicate link", links.groupBy { it.str("from") to it.str("to") }.filterValues { it.size > 1 }.keys.map { "${it.first} -> ${it.second}" })
        assertNone("Link type", links.filter { (it.str("type") ?: "association") !in TYPES }.map { "${it.str("from")} -> ${it.str("to")}" })
        assertNone(
            "Weight must be 1 to 5",
            links.filter { l -> l["w"]?.jsonPrimitive?.int?.let { it !in 1..5 } == true }.map { "${it.str("from")} -> ${it.str("to")}" }
        )
    }

    @Test
    fun `every concept has a spanish label unless it is marked todo`() {
        assertNone(
            "es label required",
            concepts.filter {
                it.obj("labels")["es"] == null && it.str("translatable") != "false" &&
                    it.arr("todo").none { t -> t.jsonPrimitive.content == "es" }
            }.map { it.id }
        )
        assertNone("A concept needs at least one label", concepts.filter { it.obj("labels").isEmpty() }.map { it.id })
    }

    @Test
    fun `labels and hints are short clean texts`() {
        val texts = concepts.flatMap { c ->
            c.obj("labels").flatMap { (lang, l) -> listOf("${c.id} label $lang" to l.jsonObject.str("text").orEmpty()) } +
                c.obj("hints").flatMap { (lang, hs) -> hs.jsonArray.map { "${c.id} hint $lang" to it.jsonObject.str("t").orEmpty() } }
        }
        assertNone("Blank text", texts.filter { it.second.isBlank() }.map { it.first })
        assertNone(
            "A label has at most 4 words and a hint at most 3",
            texts.filter { it.second.trim().split(Regex("\\s+")).size > (if (" label " in it.first) 4 else 3) }.map { "${it.first}: ${it.second}" }
        )
        assertNone("No parentheses or separators", texts.filter { t -> t.second.any { it in "()「」|;" } }.map { "${it.first}: ${it.second}" })
        assertNone("No leading or trailing spaces", texts.filter { it.second != it.second.trim() }.map { "${it.first}: \"${it.second}\"" })
    }

    @Test
    fun `categories exist and are not empty`() {
        assertNone("Concept without categories", concepts.filter { it.arr("categories").isEmpty() }.map { it.id })
        assertNone(
            "Unknown category",
            concepts.flatMap { c -> c.arr("categories").map { it.jsonPrimitive.content }.filter { it !in categories }.map { "${c.id}: $it" } }
        )
    }

    @Test
    fun `a label is used by one concept per language unless the ids carry a sense tag`() {
        val offenders = concepts.flatMap { c -> c.obj("labels").map { (lang, l) -> Triple(lang, normalize(l.jsonObject.str("text").orEmpty()), c.id) } }
            .groupBy { it.first to it.second }
            .filterValues { group -> group.size > 1 && group.any { '~' !in it.third } }
            .map { (key, group) -> "${key.first}: \"${key.second}\" used by ${group.map { it.third }}" }
        assertNone("Same label without sense tags", offenders)
    }

    @Test
    fun `no hint repeats its own label or another hint`() {
        assertNone(
            "Hint equal to the label or repeated",
            concepts.filter { c ->
                c.obj("hints").any { (lang, hs) ->
                    val label = c.obj("labels")[lang]?.jsonObject?.str("text")?.let(::normalize)
                    val texts = hs.jsonArray.map { normalize(it.jsonObject.str("t").orEmpty()) }
                    label in texts || texts.size != texts.toSet().size
                }
            }.map { it.id }
        )
    }

    private companion object {
        val ID_PATTERN = Regex("^[a-z0-9]+(-[a-z0-9]+)*(~[a-z0-9]+(-[a-z0-9]+)*)?$")
        val TYPES = setOf("association", "context", "part", "broader", "contrast", "synonym")
    }
}
