package es.sebas1705.models

/**
 * Base content unit for the Impostor game.
 */
data class WordModel(
    val word: String,
    val clue: List<String>,
    val category: Categories,
    /** Concept the word belongs to; entries of one concept in several categories share it. */
    val conceptId: String? = null,
)

