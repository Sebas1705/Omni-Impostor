package es.sebas1705.files.datasource

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import es.sebas1705.analytics.datasources.LogEventDataSource
import es.sebas1705.common.managers.ClassLogData
import es.sebas1705.files.config.SettingsFL.CONCEPTS_ASSET
import es.sebas1705.files.config.SettingsFL.LINKS_ASSET
import es.sebas1705.files.config.SettingsFL.RATINGS_ASSET
import es.sebas1705.files.graph.ConceptMap
import es.sebas1705.files.graph.ConceptMapParser
import java.io.FileNotFoundException
import javax.inject.Inject

/**
 * Reads the bundled concept map (`concepts.jsonl`, `links.jsonl`, optional `ratings.jsonl`) from the assets.
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

    /** The concept map, or null when the assets are missing or unreadable (the cause is logged). */
    fun readConceptMap(): ConceptMap? =
        try {
            val ratings = try {
                context.assets.open(RATINGS_ASSET)
            } catch (_: FileNotFoundException) {
                null
            }
            ConceptMapParser.parse(
                concepts = context.assets.open(CONCEPTS_ASSET),
                links = context.assets.open(LINKS_ASSET),
                ratings = ratings
            )
        } catch (ex: Exception) {
            logEventDataSource.logError(this, "concept map unavailable: ${ex.message}")
            null
        }
}
