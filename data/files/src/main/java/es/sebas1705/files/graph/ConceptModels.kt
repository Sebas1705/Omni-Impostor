package es.sebas1705.files.graph

import kotlinx.serialization.Serializable

/** Text of a concept in one language, plus other ways of saying it (never used as clues of the concept itself). */
@Serializable
data class ConceptLabel(
    val text: String,
    val aliases: List<String> = emptyList(),
)

/** A clue written by hand on a concept (as opposed to a link to another concept). */
@Serializable
data class ConceptHint(
    val t: String,
    val type: String = "association",
    val w: Int = 3,
    val status: String = "approved",
)

/** One line of `concepts.jsonl` (design: docs/studies/word-collection/concept-map-spec.md). */
@Serializable
data class Concept(
    val id: String,
    val labels: Map<String, ConceptLabel> = emptyMap(),
    val categories: List<String> = emptyList(),
    val hints: Map<String, List<ConceptHint>> = emptyMap(),
    /** `false` marks a proper name: a language without its own label uses the text of another one. */
    val translatable: Boolean = true,
    /** Generic topic word: kept as a clue of other concepts, never offered as a secret word. */
    val clueOnly: Boolean = false,
    val status: String = "approved",
)

/** One line of `links.jsonl`: the label of [to] becomes a clue of [from]. */
@Serializable
data class ConceptLink(
    val from: String,
    val to: String,
    val type: String = "association",
    val w: Int = 3,
    val langs: List<String>? = null,
    val status: String = "approved",
)

/** One line of `ratings.jsonl`: player votes on a clue of a concept in a language. */
@Serializable
data class ClueRating(
    val id: String,
    val lang: String,
    val clue: String,
    val pos: Int = 0,
    val neg: Int = 0,
)

/** The whole concept map as loaded from the JSON Lines sources. */
class ConceptMap(
    val concepts: List<Concept>,
    val links: List<ConceptLink>,
    val ratings: List<ClueRating> = emptyList(),
)
