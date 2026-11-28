package es.sebas1705.files.config

/**
 * Names of the bundled assets of the concept map (the sources in `data/files/concepts`, added to the assets by
 * `data/files/build.gradle.kts`).
 *
 * @since 0.1.0
 * @author Sebas1705 01/03/2025
 */
object SettingsFL {
    const val CONCEPTS_ASSET = "concepts.jsonl"
    const val LINKS_ASSET = "links.jsonl"

    /** Optional player votes on clues; absent until the first ratings are exported. */
    const val RATINGS_ASSET = "ratings.jsonl"
}
