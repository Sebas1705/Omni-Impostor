package es.sebas1705.models

/**
 * Base content unit for the Impostor game.
 */
data class WordModel(
    val word: String,
    val clue: List<String>,
    val category: Categories,

)

