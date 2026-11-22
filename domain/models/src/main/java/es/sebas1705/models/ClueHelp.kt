package es.sebas1705.models

/**
 * How much the clue given to an impostor may give the secret word away. Every clue carries a reveal level (1 weak,
 * 2 medium, 3 strong, e.g. a synonym); a help level allows the clues up to [maxReveal].
 */
enum class ClueHelp(val value: Int, val maxReveal: Int) {
    Normal(0, 2),
    Low(1, 1),
    High(2, 3);

    companion object {
        fun fromValue(value: Int) = entries.firstOrNull { it.value == value } ?: Normal
    }
}
