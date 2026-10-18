package es.sebas1705.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import es.sebas1705.common.theme.ThemeContrast

internal val lowContrastLightScheme = lightColorScheme(
    primary = primaryLightLowContrast,
    onPrimary = onPrimaryLightLowContrast,
    primaryContainer = primaryContainerLightLowContrast,
    onPrimaryContainer = onPrimaryContainerLightLowContrast,
    secondary = secondaryLightLowContrast,
    onSecondary = onSecondaryLightLowContrast,
    secondaryContainer = secondaryContainerLightLowContrast,
    onSecondaryContainer = onSecondaryContainerLightLowContrast,
    tertiary = tertiaryLightLowContrast,
    onTertiary = onTertiaryLightLowContrast,
    tertiaryContainer = tertiaryContainerLightLowContrast,
    onTertiaryContainer = onTertiaryContainerLightLowContrast,
    error = errorLightLowContrast,
    onError = onErrorLightLowContrast,
    errorContainer = errorContainerLightLowContrast,
    onErrorContainer = onErrorContainerLightLowContrast,
    background = backgroundLightLowContrast,
    onBackground = onBackgroundLightLowContrast,
    surface = surfaceLightLowContrast,
    onSurface = onSurfaceLightLowContrast,
    surfaceVariant = surfaceVariantLightLowContrast,
    onSurfaceVariant = onSurfaceVariantLightLowContrast,
    outline = outlineLightLowContrast,
    outlineVariant = outlineVariantLightLowContrast,
    scrim = scrimLightLowContrast,
    inverseSurface = inverseSurfaceLightLowContrast,
    inverseOnSurface = inverseOnSurfaceLightLowContrast,
    inversePrimary = inversePrimaryLightLowContrast,
    surfaceDim = surfaceDimLightLowContrast,
    surfaceBright = surfaceBrightLightLowContrast,
    surfaceContainerLowest = surfaceContainerLowestLightLowContrast,
    surfaceContainerLow = surfaceContainerLowLightLowContrast,
    surfaceContainer = surfaceContainerLightLowContrast,
    surfaceContainerHigh = surfaceContainerHighLightLowContrast,
    surfaceContainerHighest = surfaceContainerHighestLightLowContrast,
)

internal val lowContrastDarkScheme = darkColorScheme(
    primary = primaryDarkLowContrast,
    onPrimary = onPrimaryDarkLowContrast,
    primaryContainer = primaryContainerDarkLowContrast,
    onPrimaryContainer = onPrimaryContainerDarkLowContrast,
    secondary = secondaryDarkLowContrast,
    onSecondary = onSecondaryDarkLowContrast,
    secondaryContainer = secondaryContainerDarkLowContrast,
    onSecondaryContainer = onSecondaryContainerDarkLowContrast,
    tertiary = tertiaryDarkLowContrast,
    onTertiary = onTertiaryDarkLowContrast,
    tertiaryContainer = tertiaryContainerDarkLowContrast,
    onTertiaryContainer = onTertiaryContainerDarkLowContrast,
    error = errorDarkLowContrast,
    onError = onErrorDarkLowContrast,
    errorContainer = errorContainerDarkLowContrast,
    onErrorContainer = onErrorContainerDarkLowContrast,
    background = backgroundDarkLowContrast,
    onBackground = onBackgroundDarkLowContrast,
    surface = surfaceDarkLowContrast,
    onSurface = onSurfaceDarkLowContrast,
    surfaceVariant = surfaceVariantDarkLowContrast,
    onSurfaceVariant = onSurfaceVariantDarkLowContrast,
    outline = outlineDarkLowContrast,
    outlineVariant = outlineVariantDarkLowContrast,
    scrim = scrimDarkLowContrast,
    inverseSurface = inverseSurfaceDarkLowContrast,
    inverseOnSurface = inverseOnSurfaceDarkLowContrast,
    inversePrimary = inversePrimaryDarkLowContrast,
    surfaceDim = surfaceDimDarkLowContrast,
    surfaceBright = surfaceBrightDarkLowContrast,
    surfaceContainerLowest = surfaceContainerLowestDarkLowContrast,
    surfaceContainerLow = surfaceContainerLowDarkLowContrast,
    surfaceContainer = surfaceContainerDarkLowContrast,
    surfaceContainerHigh = surfaceContainerHighDarkLowContrast,
    surfaceContainerHighest = surfaceContainerHighestDarkLowContrast,
)

internal val mediumContrastLightColorScheme = lightColorScheme(
    primary = primaryLightMediumContrast,
    onPrimary = onPrimaryLightMediumContrast,
    primaryContainer = primaryContainerLightMediumContrast,
    onPrimaryContainer = onPrimaryContainerLightMediumContrast,
    secondary = secondaryLightMediumContrast,
    onSecondary = onSecondaryLightMediumContrast,
    secondaryContainer = secondaryContainerLightMediumContrast,
    onSecondaryContainer = onSecondaryContainerLightMediumContrast,
    tertiary = tertiaryLightMediumContrast,
    onTertiary = onTertiaryLightMediumContrast,
    tertiaryContainer = tertiaryContainerLightMediumContrast,
    onTertiaryContainer = onTertiaryContainerLightMediumContrast,
    error = errorLightMediumContrast,
    onError = onErrorLightMediumContrast,
    errorContainer = errorContainerLightMediumContrast,
    onErrorContainer = onErrorContainerLightMediumContrast,
    background = backgroundLightMediumContrast,
    onBackground = onBackgroundLightMediumContrast,
    surface = surfaceLightMediumContrast,
    onSurface = onSurfaceLightMediumContrast,
    surfaceVariant = surfaceVariantLightMediumContrast,
    onSurfaceVariant = onSurfaceVariantLightMediumContrast,
    outline = outlineLightMediumContrast,
    outlineVariant = outlineVariantLightMediumContrast,
    scrim = scrimLightMediumContrast,
    inverseSurface = inverseSurfaceLightMediumContrast,
    inverseOnSurface = inverseOnSurfaceLightMediumContrast,
    inversePrimary = inversePrimaryLightMediumContrast,
    surfaceDim = surfaceDimLightMediumContrast,
    surfaceBright = surfaceBrightLightMediumContrast,
    surfaceContainerLowest = surfaceContainerLowestLightMediumContrast,
    surfaceContainerLow = surfaceContainerLowLightMediumContrast,
    surfaceContainer = surfaceContainerLightMediumContrast,
    surfaceContainerHigh = surfaceContainerHighLightMediumContrast,
    surfaceContainerHighest = surfaceContainerHighestLightMediumContrast,
)

internal val highContrastLightColorScheme = lightColorScheme(
    primary = primaryLightHighContrast,
    onPrimary = onPrimaryLightHighContrast,
    primaryContainer = primaryContainerLightHighContrast,
    onPrimaryContainer = onPrimaryContainerLightHighContrast,
    secondary = secondaryLightHighContrast,
    onSecondary = onSecondaryLightHighContrast,
    secondaryContainer = secondaryContainerLightHighContrast,
    onSecondaryContainer = onSecondaryContainerLightHighContrast,
    tertiary = tertiaryLightHighContrast,
    onTertiary = onTertiaryLightHighContrast,
    tertiaryContainer = tertiaryContainerLightHighContrast,
    onTertiaryContainer = onTertiaryContainerLightHighContrast,
    error = errorLightHighContrast,
    onError = onErrorLightHighContrast,
    errorContainer = errorContainerLightHighContrast,
    onErrorContainer = onErrorContainerLightHighContrast,
    background = backgroundLightHighContrast,
    onBackground = onBackgroundLightHighContrast,
    surface = surfaceLightHighContrast,
    onSurface = onSurfaceLightHighContrast,
    surfaceVariant = surfaceVariantLightHighContrast,
    onSurfaceVariant = onSurfaceVariantLightHighContrast,
    outline = outlineLightHighContrast,
    outlineVariant = outlineVariantLightHighContrast,
    scrim = scrimLightHighContrast,
    inverseSurface = inverseSurfaceLightHighContrast,
    inverseOnSurface = inverseOnSurfaceLightHighContrast,
    inversePrimary = inversePrimaryLightHighContrast,
    surfaceDim = surfaceDimLightHighContrast,
    surfaceBright = surfaceBrightLightHighContrast,
    surfaceContainerLowest = surfaceContainerLowestLightHighContrast,
    surfaceContainerLow = surfaceContainerLowLightHighContrast,
    surfaceContainer = surfaceContainerLightHighContrast,
    surfaceContainerHigh = surfaceContainerHighLightHighContrast,
    surfaceContainerHighest = surfaceContainerHighestLightHighContrast,
)

internal val mediumContrastDarkColorScheme = darkColorScheme(
    primary = primaryDarkMediumContrast,
    onPrimary = onPrimaryDarkMediumContrast,
    primaryContainer = primaryContainerDarkMediumContrast,
    onPrimaryContainer = onPrimaryContainerDarkMediumContrast,
    secondary = secondaryDarkMediumContrast,
    onSecondary = onSecondaryDarkMediumContrast,
    secondaryContainer = secondaryContainerDarkMediumContrast,
    onSecondaryContainer = onSecondaryContainerDarkMediumContrast,
    tertiary = tertiaryDarkMediumContrast,
    onTertiary = onTertiaryDarkMediumContrast,
    tertiaryContainer = tertiaryContainerDarkMediumContrast,
    onTertiaryContainer = onTertiaryContainerDarkMediumContrast,
    error = errorDarkMediumContrast,
    onError = onErrorDarkMediumContrast,
    errorContainer = errorContainerDarkMediumContrast,
    onErrorContainer = onErrorContainerDarkMediumContrast,
    background = backgroundDarkMediumContrast,
    onBackground = onBackgroundDarkMediumContrast,
    surface = surfaceDarkMediumContrast,
    onSurface = onSurfaceDarkMediumContrast,
    surfaceVariant = surfaceVariantDarkMediumContrast,
    onSurfaceVariant = onSurfaceVariantDarkMediumContrast,
    outline = outlineDarkMediumContrast,
    outlineVariant = outlineVariantDarkMediumContrast,
    scrim = scrimDarkMediumContrast,
    inverseSurface = inverseSurfaceDarkMediumContrast,
    inverseOnSurface = inverseOnSurfaceDarkMediumContrast,
    inversePrimary = inversePrimaryDarkMediumContrast,
    surfaceDim = surfaceDimDarkMediumContrast,
    surfaceBright = surfaceBrightDarkMediumContrast,
    surfaceContainerLowest = surfaceContainerLowestDarkMediumContrast,
    surfaceContainerLow = surfaceContainerLowDarkMediumContrast,
    surfaceContainer = surfaceContainerDarkMediumContrast,
    surfaceContainerHigh = surfaceContainerHighDarkMediumContrast,
    surfaceContainerHighest = surfaceContainerHighestDarkMediumContrast,
)

internal val highContrastDarkColorScheme = darkColorScheme(
    primary = primaryDarkHighContrast,
    onPrimary = onPrimaryDarkHighContrast,
    primaryContainer = primaryContainerDarkHighContrast,
    onPrimaryContainer = onPrimaryContainerDarkHighContrast,
    secondary = secondaryDarkHighContrast,
    onSecondary = onSecondaryDarkHighContrast,
    secondaryContainer = secondaryContainerDarkHighContrast,
    onSecondaryContainer = onSecondaryContainerDarkHighContrast,
    tertiary = tertiaryDarkHighContrast,
    onTertiary = onTertiaryDarkHighContrast,
    tertiaryContainer = tertiaryContainerDarkHighContrast,
    onTertiaryContainer = onTertiaryContainerDarkHighContrast,
    error = errorDarkHighContrast,
    onError = onErrorDarkHighContrast,
    errorContainer = errorContainerDarkHighContrast,
    onErrorContainer = onErrorContainerDarkHighContrast,
    background = backgroundDarkHighContrast,
    onBackground = onBackgroundDarkHighContrast,
    surface = surfaceDarkHighContrast,
    onSurface = onSurfaceDarkHighContrast,
    surfaceVariant = surfaceVariantDarkHighContrast,
    onSurfaceVariant = onSurfaceVariantDarkHighContrast,
    outline = outlineDarkHighContrast,
    outlineVariant = outlineVariantDarkHighContrast,
    scrim = scrimDarkHighContrast,
    inverseSurface = inverseSurfaceDarkHighContrast,
    inverseOnSurface = inverseOnSurfaceDarkHighContrast,
    inversePrimary = inversePrimaryDarkHighContrast,
    surfaceDim = surfaceDimDarkHighContrast,
    surfaceBright = surfaceBrightDarkHighContrast,
    surfaceContainerLowest = surfaceContainerLowestDarkHighContrast,
    surfaceContainerLow = surfaceContainerLowDarkHighContrast,
    surfaceContainer = surfaceContainerDarkHighContrast,
    surfaceContainerHigh = surfaceContainerHighDarkHighContrast,
    surfaceContainerHighest = surfaceContainerHighestDarkHighContrast,
)

/**
 * Custom theme for the app.
 *
 * @param darkTheme whether the theme should be dark or light.
 * @param dynamicColor whether to use system dynamic colors on Android 12+.
 * @param themeContrast the contrast level of the theme.
 * @param content the content of the theme.
 *
 * @author: Sebas1705 01/03/2025
 * @since 0.1.0
 */
@Composable
fun AppTheme(
    modifier: Modifier = Modifier,
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    themeContrast: ThemeContrast = ThemeContrast.Low,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val shouldUseDynamicColor = dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val colors = if (shouldUseDynamicColor) {
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        when (themeContrast) {
            ThemeContrast.Low -> when {
                darkTheme -> lowContrastDarkScheme
                else -> lowContrastLightScheme
            }

            ThemeContrast.Medium -> when {
                darkTheme -> mediumContrastDarkColorScheme
                else -> mediumContrastLightColorScheme
            }

            ThemeContrast.High -> when {
                darkTheme -> highContrastDarkColorScheme
                else -> highContrastLightColorScheme
            }
        }
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = !darkTheme
        }
    }
    MaterialTheme(
        colorScheme = colors,
        typography = AppTypography,
        shapes = appShapes,
        content = {
            Box(modifier.background(colors.background)) {
                content()
            }
        }
    )
}

