package es.sebas1705.repositories.repos

import es.sebas1705.couchbase.datasources.WordsEnCBDataSource
import es.sebas1705.couchbase.datasources.WordsEsCBDataSource
import es.sebas1705.datastore.datasources.SettingsPreferencesDataSource
import es.sebas1705.files.datasource.WordFileDataSource
import es.sebas1705.files.json.WordJson
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * The bundled word lists are versioned (`words_version.txt`): a database that holds an older version, or an
 * unknown one, is replaced; an unreadable asset must never leave the player without words.
 */
class WordRepositoryImportTest {

    private val en: WordsEnCBDataSource = mock()
    private val es: WordsEsCBDataSource = mock()
    private val files: WordFileDataSource = mock()
    private val settings: SettingsPreferencesDataSource = mock()
    private val repository = WordRepository(en, es, files, settings)

    private val defaultWords = mapOf(
        "en" to listOf(WordJson("Lion", listOf("Mane", "Roar", "Jungle"), "animals")),
        "es" to listOf(WordJson("León", listOf("Melena", "Rugido", "Selva"), "animals")),
    )

    private fun givenDatabase(words: Long, storedVersion: String = "", bundledVersion: String? = "v2") = runTest {
        whenever(en.count()).thenReturn(words)
        whenever(es.count()).thenReturn(words)
        whenever(files.readWordsVersion()).thenReturn(bundledVersion)
        whenever(files.readDefaultWords()).thenReturn(defaultWords)
        whenever(settings.readDefaultWordsVersion()).thenReturn(storedVersion)
        whenever(en.deleteAll()).thenReturn(true)
        whenever(es.deleteAll()).thenReturn(true)
        whenever(en.upsertAll(any())).thenReturn(true)
        whenever(es.upsertAll(any())).thenReturn(true)
    }

    @Test
    fun `a fresh install imports the words and records the version`() = runTest {
        givenDatabase(words = 0)

        assertTrue(repository.importDefaultFromFiles())

        verify(en, never()).deleteAll()
        verify(en).upsertAll(any())
        verify(es).upsertAll(any())
        verify(settings).saveDefaultWordsVersion("v2")
    }

    @Test
    fun `the same version is left alone`() = runTest {
        givenDatabase(words = 100, storedVersion = "v2")

        assertTrue(repository.importDefaultFromFiles())

        verify(en, never()).deleteAll()
        verify(en, never()).upsertAll(any())
        verify(settings, never()).saveDefaultWordsVersion(any())
    }

    @Test
    fun `an older version is replaced and the new version recorded`() = runTest {
        givenDatabase(words = 100, storedVersion = "v1")

        assertTrue(repository.importDefaultFromFiles())

        verify(en).deleteAll()
        verify(es).deleteAll()
        verify(en).upsertAll(any())
        verify(es).upsertAll(any())
        verify(settings).saveDefaultWordsVersion("v2")
    }

    @Test
    fun `an install that never recorded a version is replaced too`() = runTest {
        givenDatabase(words = 100, storedVersion = "")

        assertTrue(repository.importDefaultFromFiles())

        verify(en).deleteAll()
        verify(settings).saveDefaultWordsVersion("v2")
    }

    @Test
    fun `unreadable files keep the old words and the old version`() = runTest {
        givenDatabase(words = 100, storedVersion = "v1")
        whenever(files.readDefaultWords()).thenReturn(emptyMap())

        assertFalse(repository.importDefaultFromFiles())

        verify(en, never()).deleteAll()
        verify(es, never()).deleteAll()
        verify(settings, never()).saveDefaultWordsVersion(any())
    }

    @Test
    fun `a missing version asset keeps an existing database`() = runTest {
        givenDatabase(words = 100, storedVersion = "v1", bundledVersion = null)

        assertTrue(repository.importDefaultFromFiles())

        verify(en, never()).deleteAll()
        verify(en, never()).upsertAll(any())
    }

    @Test
    fun `a missing version asset still fills an empty database without recording a version`() = runTest {
        givenDatabase(words = 0, bundledVersion = null)

        assertTrue(repository.importDefaultFromFiles())

        verify(en).upsertAll(any())
        verify(settings, never()).saveDefaultWordsVersion(any())
    }

    @Test
    fun `an interrupted import does not record the version so it is redone`() = runTest {
        givenDatabase(words = 100, storedVersion = "v1")
        whenever(es.upsertAll(any())).thenReturn(false)

        assertFalse(repository.importDefaultFromFiles())

        verify(settings, never()).saveDefaultWordsVersion(any())
    }

    @Test
    fun `when the old words cannot be cleared nothing is imported`() = runTest {
        givenDatabase(words = 100, storedVersion = "v1")
        whenever(en.deleteAll()).thenReturn(false)

        assertFalse(repository.importDefaultFromFiles())

        verify(en, never()).upsertAll(any())
        verify(settings, never()).saveDefaultWordsVersion(any())
    }
}
