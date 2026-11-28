package es.sebas1705.repositories.interfaces

import es.sebas1705.couchbase.documents.WordDoc

interface IWordRepository {

    /** The words of a language, all of them when [categories] is empty. */
    suspend fun findByCategories(
        languageCode: String,
        categories: Set<String>
    ): List<WordDoc>

    /** Loads the bundled concept map; true when both languages have words. */
    suspend fun importDefaultFromFiles(): Boolean

    /** Forgets what was compiled and loads the concept map again. */
    suspend fun resetToDefault(): Boolean
}
