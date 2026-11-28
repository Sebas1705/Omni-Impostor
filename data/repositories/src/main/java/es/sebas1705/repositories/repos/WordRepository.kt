package es.sebas1705.repositories.repos

import es.sebas1705.common.utlis.extensions.types.logI
import es.sebas1705.common.utlis.extensions.types.logW
import es.sebas1705.couchbase.datasources.WordsEnCBDataSource
import es.sebas1705.couchbase.datasources.WordsEsCBDataSource
import es.sebas1705.couchbase.documents.WordDoc
import es.sebas1705.files.datasource.WordFileDataSource
import es.sebas1705.files.graph.ConceptCompiler
import es.sebas1705.files.json.WordJson
import es.sebas1705.repositories.interfaces.IWordRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Serves the playable words straight from the bundled concept map (`data/files/concepts`).
 *
 * The map is read once and kept in memory. A language/category is compiled the first time it is asked for (clues
 * ranked from hints and links, see [ConceptCompiler]) and remembered, so a game only pays for the categories its
 * players picked. Nothing is stored; words an earlier version saved into the database are deleted on the first import.
 */
class WordRepository @Inject constructor(
    private val wordsEnCBDataSource: WordsEnCBDataSource,
    private val wordsEsCBDataSource: WordsEsCBDataSource,
    private val wordFileDataSource: WordFileDataSource,
) : IWordRepository {

    companion object {
        private const val ENGLISH_LANGUAGE_CODE = "en"
        private const val SPANISH_LANGUAGE_CODE = "es"
    }

    private val lock = Mutex()
    private var compiler: ConceptCompiler? = null

    /** Compiled words by language, then category. */
    private val byCategory = HashMap<String, HashMap<String, List<WordDoc>>>()

    /** Languages whose every category has been compiled (asked for with no category filter). */
    private val complete = HashSet<String>()

    private fun String.normalizeLanguageCode(): String =
        if (trim().lowercase() == ENGLISH_LANGUAGE_CODE) ENGLISH_LANGUAGE_CODE else SPANISH_LANGUAGE_CODE

    private fun millisSince(startedAt: Long) = (System.nanoTime() - startedAt) / 1_000_000

    private fun WordJson.toDoc(lang: String) = WordDoc(
        id = "$lang:$category:$conceptId",
        word = word,
        clue = clues,
        category = category,
        conceptId = conceptId,
        reveal = reveal
    )

    /** The compiler over the concept map, reading the map on first use; a failed read is not remembered. */
    private fun compilerOrNull(): ConceptCompiler? = compiler ?: run {
        val startedAt = System.nanoTime()
        val map = wordFileDataSource.readConceptMap()
        if (map == null) {
            logW("The concept map could not be read, there are no words")
            null
        } else {
            logI("Read the concept map (${map.concepts.size} concepts) in ${millisSince(startedAt)} ms")
            ConceptCompiler(map).also { compiler = it }
        }
    }

    /** Compiles the [categories] (all when null) of [lang] that are not compiled yet. */
    private fun compileMissing(compiler: ConceptCompiler, lang: String, categories: Set<String>?) {
        val cache = byCategory.getOrPut(lang) { HashMap() }
        val missing = categories?.filter { it !in cache }?.toSet()
        if (missing != null && missing.isEmpty()) return
        val startedAt = System.nanoTime()
        val compiled = compiler.compile(lang, missing).map { it.toDoc(lang) }.groupBy { it.category }
        if (missing == null) {
            cache.putAll(compiled)
            complete += lang
        } else {
            missing.forEach { cache[it] = compiled[it].orEmpty() }
        }
        logI("Compiled ${missing?.size ?: "all"} categories of $lang in ${millisSince(startedAt)} ms")
    }

    override suspend fun findByCategories(
        languageCode: String,
        categories: Set<String>
    ): List<WordDoc> = lock.withLock {
        withContext(Dispatchers.Default) {
            val lang = languageCode.normalizeLanguageCode()
            val compiler = compilerOrNull() ?: return@withContext emptyList()
            if (categories.isEmpty()) {
                if (lang !in complete) compileMissing(compiler, lang, null)
                byCategory.getValue(lang).values.flatten()
            } else {
                compileMissing(compiler, lang, categories)
                val cache = byCategory.getValue(lang)
                categories.flatMap { cache[it].orEmpty() }
            }
        }
    }

    /** Loads the concept map ahead of the first game and drops the words an older version stored in the database. */
    override suspend fun importDefaultFromFiles(): Boolean {
        dropLegacyStoredWords()
        return lock.withLock { withContext(Dispatchers.Default) { compilerOrNull() != null } }
    }

    override suspend fun resetToDefault(): Boolean {
        lock.withLock {
            compiler = null
            byCategory.clear()
            complete.clear()
        }
        return importDefaultFromFiles()
    }

    private fun dropLegacyStoredWords() {
        if (wordsEnCBDataSource.count() == 0L && wordsEsCBDataSource.count() == 0L) return
        val cleared = wordsEnCBDataSource.deleteAll() && wordsEsCBDataSource.deleteAll()
        if (cleared) logI("Deleted the words stored by an earlier version")
        else logW("Could not delete the words stored by an earlier version, they will be retried on the next launch")
    }
}
