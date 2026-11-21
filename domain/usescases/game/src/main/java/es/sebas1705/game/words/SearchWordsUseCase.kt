package es.sebas1705.game.words

import es.sebas1705.common.utlis.extensions.types.logD
import es.sebas1705.models.Categories
import es.sebas1705.models.ClueHelp
import es.sebas1705.models.WordModel
import es.sebas1705.repositories.interfaces.IAppSettingsRepository
import es.sebas1705.repositories.interfaces.IWordRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

/**
 * Reads game words from the Couchbase language collection selected in app settings.
 */
class SearchWordsUseCase @Inject constructor(
    private val gameWordRepository: IWordRepository,
    private val settingsRepository: IAppSettingsRepository
) {
    companion object {
        /** Fewer clues than this at the chosen help level and the impostor gets all of them instead. */
        internal const val MIN_CLUES = 3

        /**
         * Keeps the clues whose reveal level (one digit per clue) the [help] level allows. Entries without reveal data
         * keep every clue, and so do entries that would be left with fewer than [MIN_CLUES].
         */
        internal fun selectClues(clues: List<String>, reveal: String?, help: ClueHelp): List<String> {
            if (reveal == null || reveal.length != clues.size) return clues
            val allowed = clues.filterIndexed { i, _ -> (reveal[i].digitToIntOrNull() ?: 1) <= help.maxReveal }
            return if (allowed.size >= MIN_CLUES) allowed else clues
        }
    }

    private fun Categories.toStorageKey(): String = name.lowercase()

    private fun String.toCategoryEnumOrNull(): Categories? = runCatching {
        Categories.valueOf(trim().uppercase())
    }.getOrNull()

    suspend operator fun invoke(
        categories: Set<Categories>
    ): List<WordModel> {
        val settings = withTimeoutOrNull(5_000L) { settingsRepository.read().first() }
            ?: return emptyList()
        val categoryFilters = categories
            .flatMap { category ->
                // Keep compatibility with legacy docs stored with enum-name categories.
                listOf(category.toStorageKey(), category.name)
            }
            .toSet()

        logD("Invoke SearchWordsUseCase with categories: $categories, resolved filters: $categoryFilters, language: ${settings.appLanguage}")

        return gameWordRepository
            .findByCategories(
                languageCode = settings.appLanguage,
                categories = categoryFilters
            )
            .mapNotNull { document ->
                val category = document.category.toCategoryEnumOrNull() ?: return@mapNotNull null
                WordModel(
                    word = document.word,
                    clue = selectClues(document.clue, document.reveal, ClueHelp.fromValue(settings.clueHelp)),
                    category = category,
                    conceptId = document.conceptId
                )
            }
            // A concept in several selected categories is one word: pick uniformly over concepts, not entries.
            .distinctBy { it.conceptId ?: "${it.category}:${it.word}" }
    }
}

