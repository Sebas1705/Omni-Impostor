package es.sebas1705.repositories.repos

import es.sebas1705.common.utlis.extensions.types.logI
import es.sebas1705.couchbase.datasources.WordsEnCBDataSource
import es.sebas1705.couchbase.datasources.WordsEsCBDataSource
import es.sebas1705.couchbase.documents.WordDoc
import es.sebas1705.files.datasource.WordFileDataSource
import es.sebas1705.repositories.interfaces.IWordRepository
import java.util.UUID
import javax.inject.Inject

class WordRepository @Inject constructor(
    private val wordsEnCBDataSource: WordsEnCBDataSource,
    private val wordsEsCBDataSource: WordsEsCBDataSource,
    private val wordFileDataSource: WordFileDataSource
) : IWordRepository {

    companion object {
        private const val ENGLISH_LANGUAGE_CODE = "en"
        private const val SPANISH_LANGUAGE_CODE = "es"
    }

    private fun String.normalizeLanguageCode(): String = this.trim().lowercase()

    override suspend fun findByCategories(
        languageCode: String,
        categories: Set<String>
    ): List<WordDoc> = if (categories.isEmpty())
        if (languageCode.normalizeLanguageCode() == ENGLISH_LANGUAGE_CODE)
            wordsEnCBDataSource.getAll()
        else wordsEsCBDataSource.getAll()
    else if (languageCode.normalizeLanguageCode() == ENGLISH_LANGUAGE_CODE)
        wordsEnCBDataSource.getByParam(WordDoc::category, categories)
    else wordsEsCBDataSource.getByParam(WordDoc::category, categories)

    override suspend fun upsertAll(
        languageCode: String,
        entries: List<WordDoc>
    ): Boolean = if (languageCode.normalizeLanguageCode() == ENGLISH_LANGUAGE_CODE)
        wordsEnCBDataSource.upsertAll(entries)
    else wordsEsCBDataSource.upsertAll(entries)

    override suspend fun importDefaultFromFiles(): Boolean {

        if(wordsEnCBDataSource.count() > 0 || wordsEsCBDataSource.count() > 0) {
            logI("Default words already exist in the database, skipping import")
            return true
        }

        val wordMap = wordFileDataSource
            .readDefaultWords()
        var jsonsUpserted = 0
        var totalToUpsert = 0

        wordMap.forEach { (language, jsons) ->
            totalToUpsert += jsons.size
            val languageCode = language.normalizeLanguageCode()
            val entries = jsons.map {
                WordDoc(
                    id = UUID.randomUUID().toString(),
                    word = it.word,
                    clue = it.clues,
                    category = it.category,
                    relatedWords = it.relatedWords
                )
            }

            if(when (languageCode) {
                ENGLISH_LANGUAGE_CODE -> wordsEnCBDataSource.upsertAll(entries)
                SPANISH_LANGUAGE_CODE -> wordsEsCBDataSource.upsertAll(entries)
                else -> false
            }) jsonsUpserted += entries.size
        }

        logI("Upserted $jsonsUpserted out of $totalToUpsert default words from files")
        return jsonsUpserted == totalToUpsert
    }

    override suspend fun resetToDefault(): Boolean {
        val deleteEn = wordsEnCBDataSource.deleteAll()
        val deleteEs = wordsEsCBDataSource.deleteAll()
        logI("Deleted all existing words. English delete success: $deleteEn, Spanish delete success: $deleteEs. Importing default words from files...")
        val importResult = importDefaultFromFiles()

        return deleteEn && deleteEs && importResult
    }
}

