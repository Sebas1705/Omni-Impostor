package es.sebas1705.game.words

import es.sebas1705.common.utlis.extensions.types.logD
import es.sebas1705.models.Categories
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
                    clue = document.clue,
                    category = category,
                    relatedWords = document.relatedWords
                )
            }
    }
}

