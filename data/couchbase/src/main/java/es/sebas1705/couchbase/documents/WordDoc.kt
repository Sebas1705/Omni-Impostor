package es.sebas1705.couchbase.documents

import es.sebas1705.couchbase.documents.abstracts.Document

/**
 * Couchbase document model for playable game words.
 */
data class WordDoc(
    val id: String,
    val word: String,
    val clue: List<String>,
    val category: String,
    val relatedWords: List<String> = emptyList()
) : Document(id)

