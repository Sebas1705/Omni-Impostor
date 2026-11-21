package es.sebas1705.files.config

import es.sebas1705.files.config.SettingsFL.ASSET_NAME_REGEX


/**
 * Object to store the settings of the files
 *
 * @property ASSET_NAME_REGEX [Regex]: Regular expression to match the asset names for default game words
 *
 * @since 0.1.0
 * @author Sebas1705 01/03/2025
 */
object SettingsFL {
    val ASSET_NAME_REGEX = Regex(".*_(en|es)\\.json$")

    /** Asset holding the version (content hash) of the bundled word lists. */
    const val WORDS_VERSION_ASSET = "words_version.txt"
}