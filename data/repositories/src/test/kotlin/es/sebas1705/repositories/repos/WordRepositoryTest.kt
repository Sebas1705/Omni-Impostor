package es.sebas1705.repositories.repos

import es.sebas1705.couchbase.datasources.WordsEnCBDataSource
import es.sebas1705.couchbase.datasources.WordsEsCBDataSource
import es.sebas1705.files.datasource.WordFileDataSource
import es.sebas1705.files.graph.Concept
import es.sebas1705.files.graph.ConceptHint
import es.sebas1705.files.graph.ConceptLabel
import es.sebas1705.files.graph.ConceptMap
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/** The repository serves words compiled from the bundled concept map and keeps nothing in the database. */
class WordRepositoryTest {

    private val en: WordsEnCBDataSource = mock()
    private val es: WordsEsCBDataSource = mock()
    private val files: WordFileDataSource = mock()
    private val repository = WordRepository(en, es, files)

    private fun concept(id: String, spanish: String, english: String, vararg categories: String): Concept = Concept(
        id = id,
        labels = mapOf("es" to ConceptLabel(spanish), "en" to ConceptLabel(english)),
        categories = categories.toList(),
        hints = mapOf(
            "es" to listOf("Uno", "Dos", "Tres").map { ConceptHint("$it $spanish") },
            "en" to listOf("One", "Two", "Three").map { ConceptHint("$it $english") },
        ),
    )

    private val map = ConceptMap(
        concepts = listOf(
            concept("lion", "León", "Lion", "animals", "nature_and_weather"),
            concept("guitar", "Guitarra", "Guitar", "music"),
        ),
        links = emptyList(),
    )

    private fun givenLegacyWords(count: Long) {
        whenever(en.count()).thenReturn(count)
        whenever(es.count()).thenReturn(count)
        whenever(en.deleteAll()).thenReturn(true)
        whenever(es.deleteAll()).thenReturn(true)
    }

    @Test
    fun `words come from the concept map in the requested language`() = runTest {
        whenever(files.readConceptMap()).thenReturn(map)

        assertEquals(setOf("León", "Guitarra"), repository.findByCategories("es", emptySet()).map { it.word }.toSet())
        assertEquals(setOf("Lion", "Guitar"), repository.findByCategories("en", emptySet()).map { it.word }.toSet())
    }

    @Test
    fun `an unknown language falls back to spanish`() = runTest {
        whenever(files.readConceptMap()).thenReturn(map)

        assertEquals(setOf("León", "Guitarra"), repository.findByCategories("fr", emptySet()).map { it.word }.toSet())
    }

    @Test
    fun `categories filter the words and a concept in two categories keeps its id in both`() = runTest {
        whenever(files.readConceptMap()).thenReturn(map)

        val animals = repository.findByCategories("en", setOf("animals"))
        assertEquals(listOf("Lion"), animals.map { it.word })
        assertEquals("lion", animals.single().conceptId)
        assertEquals(1, repository.findByCategories("en", setOf("nature_and_weather")).size)
        assertEquals(0, repository.findByCategories("en", setOf("sports")).size)
    }

    @Test
    fun `the map is compiled once`() = runTest {
        whenever(files.readConceptMap()).thenReturn(map)

        repository.findByCategories("es", emptySet())
        repository.findByCategories("en", setOf("music"))

        verify(files, times(1)).readConceptMap()
    }

    @Test
    fun `an unreadable map gives no words and is tried again next time`() = runTest {
        whenever(files.readConceptMap()).thenReturn(null, map)

        assertTrue(repository.findByCategories("es", emptySet()).isEmpty())
        assertEquals(3, repository.findByCategories("es", emptySet()).size)
    }

    @Test
    fun `import drops the words an older version stored`() = runTest {
        whenever(files.readConceptMap()).thenReturn(map)
        givenLegacyWords(100)

        assertTrue(repository.importDefaultFromFiles())

        verify(en).deleteAll()
        verify(es).deleteAll()
    }

    @Test
    fun `import leaves an empty database alone`() = runTest {
        whenever(files.readConceptMap()).thenReturn(map)
        givenLegacyWords(0)

        assertTrue(repository.importDefaultFromFiles())

        verify(en, never()).deleteAll()
    }

    @Test
    fun `import reports failure when the map cannot be read`() = runTest {
        whenever(files.readConceptMap()).thenReturn(null)
        givenLegacyWords(0)

        assertFalse(repository.importDefaultFromFiles())
    }

    @Test
    fun `reset reads the map again`() = runTest {
        whenever(files.readConceptMap()).thenReturn(map)
        givenLegacyWords(0)
        repository.findByCategories("es", emptySet())

        assertTrue(repository.resetToDefault())

        verify(files, times(2)).readConceptMap()
    }
}
