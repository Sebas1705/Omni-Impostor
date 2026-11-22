package es.sebas1705.files.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WordJson(
    val word: String,
    @SerialName("clues")
    val clues: List<String>,
    val category: String,
    /** One digit per clue (1 weak, 2 medium, 3 strong giveaway); absent means every clue is level 1. */
    val reveal: String? = null,
    /** Id of the concept in `data/files/concepts` the entry was compiled from (absent in hand-written lists). */
    val conceptId: String? = null,
)


@Serializable
data class WordsJson(
    @SerialName("words")
    val words: List<WordJson>
)
