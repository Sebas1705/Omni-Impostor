package es.sebas1705.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

/**
 * Guards WCAG 2.1 AA (4.5:1) for the color pairs the app actually draws text/icons with,
 * in every scheme (3 contrast levels x light/dark).
 */
class ContrastTest {

    private data class Pair(val name: String, val foreground: Color, val background: Color)

    private val schemes = mapOf(
        "low/light" to lowContrastLightScheme,
        "low/dark" to lowContrastDarkScheme,
        "medium/light" to mediumContrastLightColorScheme,
        "medium/dark" to mediumContrastDarkColorScheme,
        "high/light" to highContrastLightColorScheme,
        "high/dark" to highContrastDarkColorScheme,
    )

    private fun pairsOf(s: ColorScheme) = listOf(
        Pair("onPrimary/primary", s.onPrimary, s.primary),
        Pair("onPrimaryContainer/primaryContainer", s.onPrimaryContainer, s.primaryContainer),
        Pair("onSecondary/secondary", s.onSecondary, s.secondary),
        Pair("onSecondaryContainer/secondaryContainer", s.onSecondaryContainer, s.secondaryContainer),
        Pair("onTertiary/tertiary", s.onTertiary, s.tertiary),
        Pair("onTertiaryContainer/tertiaryContainer", s.onTertiaryContainer, s.tertiaryContainer),
        Pair("onError/error", s.onError, s.error),
        Pair("onErrorContainer/errorContainer", s.onErrorContainer, s.errorContainer),
        Pair("onBackground/background", s.onBackground, s.background),
        Pair("onSurface/surface", s.onSurface, s.surface),
        Pair("onSurfaceVariant/surface", s.onSurfaceVariant, s.surface),
        Pair("onSurfaceVariant/surfaceVariant", s.onSurfaceVariant, s.surfaceVariant),
        Pair("primary/background", s.primary, s.background),
        Pair("secondary/background", s.secondary, s.background),
        Pair("tertiary/background", s.tertiary, s.background),
        Pair("error/background", s.error, s.background),
    )

    private fun contrastRatio(a: Color, b: Color): Double {
        val l1 = a.luminance().toDouble()
        val l2 = b.luminance().toDouble()
        return (maxOf(l1, l2) + 0.05) / (minOf(l1, l2) + 0.05)
    }

    @Test
    fun `key color pairs meet WCAG AA in every scheme`() {
        val failures = schemes.flatMap { (schemeName, scheme) ->
            pairsOf(scheme)
                .map { it to contrastRatio(it.foreground, it.background) }
                .filter { (_, ratio) -> ratio < MIN_RATIO }
                .map { (pair, ratio) -> "$schemeName ${pair.name} = ${"%.2f".format(Locale.US, ratio)}" }
        }

        assertTrue(
            "Pairs below $MIN_RATIO:1:\n${failures.joinToString("\n")}",
            failures.isEmpty()
        )
    }

    private companion object {
        const val MIN_RATIO = 4.5
    }
}
