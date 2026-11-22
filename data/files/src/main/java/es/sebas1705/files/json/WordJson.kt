package es.sebas1705.files.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WordJson(
    val word: String,
    @SerialName("clues")
    val clues: List<String>,
    val category: String,
    /** Id of the concept in `data/files/concepts` the entry was compiled from (absent in hand-written lists). */
    val conceptId: String? = null,
)


@Serializable
data class WordsJson(
    @SerialName("words")
    val words: List<WordJson>
)
