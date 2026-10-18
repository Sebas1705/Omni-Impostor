package es.sebas1705.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Spacing scale shared by every screen. */
@Immutable
data class Spacing(
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 12.dp,
    val lg: Dp = 16.dp,
    val xl: Dp = 24.dp,
    val xxl: Dp = 32.dp,
)

/** Elevation scale shared by every surface. */
@Immutable
data class Elevations(
    val none: Dp = 0.dp,
    val low: Dp = 1.dp,
    val medium: Dp = 3.dp,
    val high: Dp = 6.dp,
)

val LocalSpacing = staticCompositionLocalOf { Spacing() }
val LocalElevations = staticCompositionLocalOf { Elevations() }

/** Entry point to read the tokens: `AppDimens.spacing.lg`, `AppDimens.elevation.low`. */
object AppDimens {
    val spacing: Spacing
        @Composable @ReadOnlyComposable get() = LocalSpacing.current

    val elevation: Elevations
        @Composable @ReadOnlyComposable get() = LocalElevations.current
}
