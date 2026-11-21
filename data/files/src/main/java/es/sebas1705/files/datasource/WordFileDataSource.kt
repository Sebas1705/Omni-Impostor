package es.sebas1705.files.datasource

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import es.sebas1705.analytics.datasources.LogEventDataSource
import es.sebas1705.common.managers.ClassLogData
import es.sebas1705.files.config.SettingsFL.ASSET_NAME_REGEX
import es.sebas1705.files.config.SettingsFL.WORDS_VERSION_ASSET
import es.sebas1705.files.json.WordJson
import es.sebas1705.files.json.WordsJson
import kotlinx.serialization.json.Json
import javax.inject.Inject

/**
 * Class to represent the repository of the files
 *
 * @property context [Context]: Application context
 * @property logEventDataSource [LogEventDataSource]: Data source for logging events
 *
 * @since 0.1.0
 * @author Sebas1705 01/03/2025
 */
class WordFileDataSource @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val logEventDataSource: LogEventDataSource
) : ClassLogData() {

    private val jsonParser = Json { ignoreUnknownKeys = true }

    /**
     * Version of the bundled word lists (`words_version.txt`, a hash of their content kept in sync by a unit test),
     * or null when the file is missing or unreadable.
     */
    fun readWordsVersion(): String? =
        try {
            context.assets.open(WORDS_VERSION_ASSET).bufferedReader().use { it.readText() }.trim().ifEmpty { null }
        } catch (ex: java.io.IOException) {
            logEventDataSource.logError(this, "words version unavailable: ${ex.message}")
            null
        }

    fun readDefaultWords(): Map<String, List<WordJson>> =
        try {
            val wordMap: MutableMap<String, List<WordJson>> = mutableMapOf()
            context.assets
                .list("")
                .orEmpty()
                .filter { ASSET_NAME_REGEX.matches(it) }
                .forEach { assetName ->
                    val languageCode = ASSET_NAME_REGEX.matchEntire(assetName)
                        ?.groupValues
                        ?.getOrNull(1)
                        .orEmpty()

                    val payload = context.assets
                        .open(assetName)
                        .bufferedReader()
                        .use { it.readText() }

                    val fileWords = jsonParser
                        .decodeFromString<WordsJson>(payload)

                    wordMap[languageCode] = (wordMap[languageCode] as? MutableList ?: emptyList()) + fileWords.words
                }
            wordMap
        } catch (ex: Exception) {
            logEventDataSource.logError(this, ex.message.toString())
            emptyMap()
        }

}