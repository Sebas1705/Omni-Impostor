package es.sebas1705.game.words

import es.sebas1705.couchbase.documents.WordDoc
import es.sebas1705.datastore.model.SettingsData
import es.sebas1705.models.Categories
import es.sebas1705.repositories.interfaces.IAppSettingsRepository
import es.sebas1705.repositories.interfaces.IWordRepository
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * A concept that lives in several categories is stored once per category; when more than one of them is selected it
 * must still count as one word, so the round word is picked uniformly over concepts, not over entries.
 */
class SearchWordsUseCaseTest {

    private val words: IWordRepository = mock()
    private val settings: IAppSettingsRepository = mock {
        on { read() } doReturn flowOf(
            SettingsData(
                firstTime = false, musicVolume = 1f, soundVolume = 1f, appContrast = 0,
                defaultSet = true, appLanguage = "es", forceCompactTables = false
            )
        )
    }
    private val useCase = SearchWordsUseCase(words, settings)

    private fun doc(id: String, word: String, category: String, conceptId: String?) =
        WordDoc(id = id, word = word, clue = listOf("a", "b", "c"), category = category, conceptId = conceptId)

    @Test
    fun `a concept found in two selected categories is returned once`() = runTest {
        whenever(words.findByCategories(any(), any())).thenReturn(
            listOf(
                doc("1", "Hogar", "home_and_furniture", "hogar"),
                doc("2", "Hogar", "emotions_and_feelings", "hogar"),
                doc("3", "León", "animals", "leon"),
            )
        )

        val result = useCase(setOf(Categories.HOME_AND_FURNITURE, Categories.EMOTIONS_AND_FEELINGS, Categories.ANIMALS))

        assertEquals(listOf("Hogar", "León"), result.map { it.word })
        assertEquals(listOf("hogar", "leon"), result.map { it.conceptId })
    }

    @Test
    fun `homonyms with different concept ids are both kept`() = runTest {
        whenever(words.findByCategories(any(), any())).thenReturn(
            listOf(
                doc("1", "Espacio", "space_and_astronomy", "espacio~universo"),
                doc("2", "Espacio", "board_games_and_tabletop", "espacio~casilla"),
            )
        )

        val result = useCase(setOf(Categories.SPACE_AND_ASTRONOMY, Categories.BOARD_GAMES_AND_TABLETOP))

        assertEquals(2, result.size)
    }

    @Test
    fun `entries without a concept id are never merged`() = runTest {
        whenever(words.findByCategories(any(), any())).thenReturn(
            listOf(
                doc("1", "Beer", "food_and_drinks", null),
                doc("2", "Beer", "adult_18_plus", null),
            )
        )

        val result = useCase(setOf(Categories.FOOD_AND_DRINKS, Categories.ADULT_18_PLUS))

        assertEquals(2, result.size)
    }
}
