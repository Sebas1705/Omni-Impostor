package es.sebas1705.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * Custom shapes to be used in the app.
 *
 * @since 0.1.0
 * @author Sebas1705 01/03/2025
 */
val appShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

/** Shape used by cards: the theme's `large` shape, so it follows `appShapes`. */
val MaterialTheme.cardShape: Shape
    @Composable @ReadOnlyComposable get() = shapes.large
