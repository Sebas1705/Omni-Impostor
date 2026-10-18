# Visual Foundation (Phase 0) Implementation Plan

> **For agentic workers:** Use `mobiai-mobile-executing-plans-with-subagents` (recommended) or `mobiai-mobile-executing-plans` to implement this plan task-by-task. Steps use checkbox syntax for tracking.

**Goal:** Add the shared visual foundation (design tokens, four base components, a WCAG AA contrast guard, a unique icon per category) that phases 1–4 of the visual refresh will build on.

**Architecture:** Everything lives in `core/ui` (tokens and components) plus `CategoryIcons.kt` in `feature/home/face/categories`. Tokens are exposed through `CompositionLocal`s provided by `AppTheme`. No screen is redesigned in this phase and no game/data/domain code changes.

**Tech Stack:** Kotlin 2.3, Jetpack Compose, Material 3, JUnit 4 (plain JVM unit tests).

**Platform:** Android

Spec: `docs/designs/2026-10-18-visual-refresh-design.md` (Phase 0).

**Environment for every Gradle command** (SEBAS-PC, Git Bash). Secrets must already exist (`bash scripts/setup-secrets.sh`; files are git-ignored):

```bash
export JAVA_HOME="/c/Users/sebss/.jdks/jdk-21.0.12.1+1" ANDROID_HOME="/c/Users/sebss/Android/sdk"
```

**Branch:** work on `design/visual-refresh` (it already contains the design doc commit). Commits use the repo's configured user (Sebas1705), no co-author trailer.

## File map

| File | Action |
|------|--------|
| `core/ui/build.gradle.kts` | Modify: unit-test plugin + junit |
| `core/ui/src/main/java/es/sebas1705/ui/theme/Theme.kt` | Modify: expose schemes as `internal`, provide token locals |
| `core/ui/src/main/java/es/sebas1705/ui/theme/Color.kt` | Modify: 2 color values (medium-contrast light) |
| `core/ui/src/test/kotlin/es/sebas1705/ui/theme/ContrastTest.kt` | Create |
| `core/ui/src/main/java/es/sebas1705/ui/theme/Dimens.kt` | Create |
| `core/ui/src/main/java/es/sebas1705/ui/theme/Shapes.kt` | Modify: add `cardShape` |
| `core/ui/src/main/java/es/sebas1705/ui/components/AppCard.kt` | Create |
| `core/ui/src/main/java/es/sebas1705/ui/components/SectionHeader.kt` | Create |
| `core/ui/src/main/java/es/sebas1705/ui/components/StatusChip.kt` | Create |
| `core/ui/src/main/java/es/sebas1705/ui/components/PrimaryActionButton.kt` | Create |
| `feature/home/face/categories/build.gradle.kts` | Modify: unit-test plugin + junit |
| `feature/home/face/categories/src/test/kotlin/es/sebas1705/categories/models/CategoryIconsTest.kt` | Create |
| `feature/home/face/categories/src/main/java/es/sebas1705/categories/models/CategoryIcons.kt` | Modify |
| `docs/design.md` | Modify |
| `CHANGELOG.md` | Modify |

---

### Task 1: Contrast guard (WCAG AA) and the two failing colors

A throwaway audit already ran on `develop`: of 96 checked pairs (16 pairs × 6 schemes) only two are below 4.5:1, both in the **medium-contrast light** scheme: `onSecondaryContainer/secondaryContainer` = 3.88 and `onTertiaryContainer/tertiaryContainer` = 3.71. The test below must fail on exactly those two, then pass after the fix.

**Files:**
- Modify: `core/ui/build.gradle.kts`
- Modify: `core/ui/src/main/java/es/sebas1705/ui/theme/Theme.kt` (declarations at lines 21, 59, 97, 135, 173, 211)
- Modify: `core/ui/src/main/java/es/sebas1705/ui/theme/Color.kt`
- Create: `core/ui/src/test/kotlin/es/sebas1705/ui/theme/ContrastTest.kt`

- [ ] **Step 1: Enable unit tests in `core/ui`**

Replace the whole content of `core/ui/build.gradle.kts` with:

```kotlin
plugins {
    alias(libs.plugins.buildlogic.core)
    alias(libs.plugins.buildlogic.unit.test)
}

android {

    namespace = "es.sebas1705.core.ui"
}

dependencies {
    api(projects.core.common)
    api(projects.core.resources)

    testImplementation(libs.junit.junit)
}
```

- [ ] **Step 2: Make the six color schemes visible to tests**

In `Theme.kt`, change `private val` to `internal val` for these six declarations only: `lowContrastLightScheme`, `lowContrastDarkScheme`, `mediumContrastLightColorScheme`, `highContrastLightColorScheme`, `mediumContrastDarkColorScheme`, `highContrastDarkColorScheme`.

```bash
sed -i -E 's/^private val (lowContrastLightScheme|lowContrastDarkScheme|mediumContrastLightColorScheme|highContrastLightColorScheme|mediumContrastDarkColorScheme|highContrastDarkColorScheme) = /internal val \1 = /' core/ui/src/main/java/es/sebas1705/ui/theme/Theme.kt
grep -n "^internal val" core/ui/src/main/java/es/sebas1705/ui/theme/Theme.kt
```

Expected: 6 lines starting with `internal val`.

- [ ] **Step 3: Write the failing test**

Create `core/ui/src/test/kotlin/es/sebas1705/ui/theme/ContrastTest.kt`:

```kotlin
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
```

- [ ] **Step 4: Run the test and confirm it fails on the two known pairs**

Run: `./gradlew :core:ui:testDebugUnitTest --tests "es.sebas1705.ui.theme.ContrastTest" --console=plain`
Expected: FAILED with a message listing exactly
`medium/light onSecondaryContainer/secondaryContainer = 3.88` and `medium/light onTertiaryContainer/tertiaryContainer = 3.71`.

- [ ] **Step 5: Fix the two colors (darken the containers, keep white text)**

In `core/ui/src/main/java/es/sebas1705/ui/theme/Color.kt` change only these two values:

```kotlin
val secondaryContainerLightMediumContrast = Color(0xFF1F7A6E)   // was 0xFF2A9083 (≈5.2:1 with white)
val tertiaryContainerLightMediumContrast = Color(0xFF0F76A8)    // was 0xFF148DC6 (≈5.0:1 with white)
```

- [ ] **Step 6: Run the test and confirm it passes**

Run: `./gradlew :core:ui:testDebugUnitTest --tests "es.sebas1705.ui.theme.ContrastTest" --console=plain`
Expected: `ContrastTest > key color pairs meet WCAG AA in every scheme PASSED`, BUILD SUCCESSFUL.

- [ ] **Step 7: Commit**

```bash
git add core/ui/build.gradle.kts core/ui/src
git commit -m "feat(ui): guard WCAG AA contrast of the color schemes

Adds ContrastTest (16 key pairs x 6 schemes). Darkens the medium-contrast
light secondary/tertiary containers, the only pairs below 4.5:1."
```

---

### Task 2: Design tokens (spacing, elevation, card shape)

**Files:**
- Create: `core/ui/src/main/java/es/sebas1705/ui/theme/Dimens.kt`
- Modify: `core/ui/src/main/java/es/sebas1705/ui/theme/Shapes.kt`
- Modify: `core/ui/src/main/java/es/sebas1705/ui/theme/Theme.kt` (the `MaterialTheme(...)` call and imports)

- [ ] **Step 1: Create the tokens**

`core/ui/src/main/java/es/sebas1705/ui/theme/Dimens.kt`:

```kotlin
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
```

- [ ] **Step 2: Add the card shape alias**

Append to `Shapes.kt` (and add the two imports `androidx.compose.material3.MaterialTheme`, `androidx.compose.runtime.Composable`, `androidx.compose.runtime.ReadOnlyComposable`, `androidx.compose.ui.graphics.Shape` at the top):

```kotlin

/** Shape used by cards: the theme's `large` shape, so it follows `appShapes`. */
val MaterialTheme.cardShape: Shape
    @Composable @ReadOnlyComposable get() = shapes.large
```

- [ ] **Step 3: Provide the tokens from `AppTheme`**

In `Theme.kt` add the import `androidx.compose.runtime.CompositionLocalProvider` and replace the final `MaterialTheme(...)` call with:

```kotlin
    MaterialTheme(
        colorScheme = colors,
        typography = AppTypography,
        shapes = appShapes,
        content = {
            CompositionLocalProvider(
                LocalSpacing provides Spacing(),
                LocalElevations provides Elevations(),
            ) {
                Box(modifier.background(colors.background)) {
                    content()
                }
            }
        }
    )
```

- [ ] **Step 4: Compile**

Run: `./gradlew :core:ui:compileDebugKotlin --console=plain`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 5: Commit**

```bash
git add core/ui/src/main
git commit -m "feat(ui): add spacing/elevation tokens and card shape"
```

---

### Task 3: Base components

No UI test framework exists in the project (see design doc), so these are verified by compiling, detekt and the `@UiModePreviews` previews. They are intentionally unused until phase 1.

**Files (all create):**
- `core/ui/src/main/java/es/sebas1705/ui/components/AppCard.kt`
- `core/ui/src/main/java/es/sebas1705/ui/components/SectionHeader.kt`
- `core/ui/src/main/java/es/sebas1705/ui/components/StatusChip.kt`
- `core/ui/src/main/java/es/sebas1705/ui/components/PrimaryActionButton.kt`

- [ ] **Step 1: `AppCard`**

```kotlin
package es.sebas1705.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import es.sebas1705.common.utlis.UiModePreviews
import es.sebas1705.ui.theme.AppDimens
import es.sebas1705.ui.theme.AppTheme
import es.sebas1705.ui.theme.cardShape

/**
 * Standard card: consistent shape, elevation and padding, optional header (icon + title),
 * optional selected state and optional click.
 */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    icon: ImageVector? = null,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val spacing = AppDimens.spacing
    val colors = CardDefaults.cardColors(
        containerColor = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainer
        },
        contentColor = if (selected) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurface
        },
    )
    val elevation = CardDefaults.cardElevation(defaultElevation = AppDimens.elevation.low)
    val border = if (selected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    val body: @Composable ColumnScope.() -> Unit = {
        Column(
            modifier = Modifier.padding(spacing.lg),
            verticalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            if (title != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                ) {
                    if (icon != null) {
                        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(24.dp))
                    }
                    Text(text = title, style = MaterialTheme.typography.titleMedium)
                }
            }
            content()
        }
    }
    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier,
            shape = MaterialTheme.cardShape,
            colors = colors,
            elevation = elevation,
            border = border,
            content = body,
        )
    } else {
        Card(
            modifier = modifier,
            shape = MaterialTheme.cardShape,
            colors = colors,
            elevation = elevation,
            border = border,
            content = body,
        )
    }
}

@UiModePreviews
@Composable
private fun AppCardPreview() {
    AppTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AppCard(title = "Categories", icon = Icons.Outlined.Category) { Text("12 selected") }
            AppCard(title = "Selected", icon = Icons.Outlined.Category, selected = true, onClick = {}) {
                Text("Clickable and selected")
            }
        }
    }
}
```

- [ ] **Step 2: `SectionHeader`**

```kotlin
package es.sebas1705.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import es.sebas1705.common.utlis.UiModePreviews
import es.sebas1705.ui.theme.AppDimens
import es.sebas1705.ui.theme.AppTheme
import es.sebas1705.ui.theme.makeTitle

/** Section title (Finger Paint, primary color) with an optional subtitle. */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(AppDimens.spacing.xs),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.makeTitle(),
            color = MaterialTheme.colorScheme.primary,
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@UiModePreviews
@Composable
private fun SectionHeaderPreview() {
    AppTheme {
        SectionHeader(
            title = "Ranking",
            subtitle = "Who wins the most",
            modifier = Modifier.padding(16.dp),
        )
    }
}
```

- [ ] **Step 3: `StatusChip`**

```kotlin
package es.sebas1705.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import es.sebas1705.common.utlis.UiModePreviews
import es.sebas1705.ui.theme.AppDimens
import es.sebas1705.ui.theme.AppTheme

enum class StatusChipVariant { Ready, Warning, Error }

/**
 * Small read-only status pill. Colors come from the container/onContainer pairs
 * that `ContrastTest` guarantees to meet WCAG AA.
 */
@Composable
fun StatusChip(
    text: String,
    modifier: Modifier = Modifier,
    variant: StatusChipVariant = StatusChipVariant.Ready,
    icon: ImageVector? = null,
) {
    val scheme = MaterialTheme.colorScheme
    val (container, content) = when (variant) {
        StatusChipVariant.Ready -> scheme.secondaryContainer to scheme.onSecondaryContainer
        StatusChipVariant.Warning -> scheme.tertiaryContainer to scheme.onTertiaryContainer
        StatusChipVariant.Error -> scheme.errorContainer to scheme.onErrorContainer
    }
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = container,
        contentColor = content,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = AppDimens.spacing.md, vertical = AppDimens.spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppDimens.spacing.xs),
        ) {
            if (icon != null) {
                Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp))
            }
            Text(text = text, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@UiModePreviews
@Composable
private fun StatusChipPreview() {
    AppTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            StatusChip("4 players", icon = Icons.Outlined.CheckCircle)
            StatusChip("Select a category", variant = StatusChipVariant.Warning, icon = Icons.Outlined.Warning)
            StatusChip("Not enough players", variant = StatusChipVariant.Error)
        }
    }
}
```

- [ ] **Step 4: `PrimaryActionButton`**

```kotlin
package es.sebas1705.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import es.sebas1705.common.utlis.UiModePreviews
import es.sebas1705.core.resources.Sounds
import es.sebas1705.ui.sound.LocalSoundPlayer
import es.sebas1705.ui.theme.AppTheme

/** Full-width main action (e.g. "Start game"): 48 dp minimum height and the arcade click sound. */
@Composable
fun PrimaryActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val sound = LocalSoundPlayer.current
    FilledTonalButton(
        onClick = {
            sound(Sounds.CLK_ARCADE)
            onClick()
        },
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp),
    ) {
        Text(text)
    }
}

@UiModePreviews
@Composable
private fun PrimaryActionButtonPreview() {
    AppTheme {
        PrimaryActionButton(text = "Start game", onClick = {}, modifier = Modifier.padding(16.dp))
    }
}
```

- [ ] **Step 5: Compile and run detekt on the module**

Run: `./gradlew :core:ui:compileDebugKotlin :core:ui:detekt --console=plain`
Expected: BUILD SUCCESSFUL with no new detekt findings in the four new files. If `UiModePreviews` or `LocalSoundPlayer` does not resolve, check the import against `FaceDesign.kt` (`es.sebas1705.common.utlis.UiModePreviews`, `es.sebas1705.ui.sound.LocalSoundPlayer`) and fix the path.

- [ ] **Step 6: Commit**

```bash
git add core/ui/src/main
git commit -m "feat(ui): add AppCard, SectionHeader, StatusChip and PrimaryActionButton

Unused until phase 1 of the visual refresh. Each has a UiModePreviews preview."
```

---

### Task 4: One distinct icon per category

Today many categories share an icon or use an unrelated one (e.g. `MUSIC` → `TheaterComedy`, `BRANDS`/`ADULT_18_PLUS`/`OFFICE_AND_STATIONERY` → `Description`, `SPACE_AND_ASTRONOMY` → `FlightTakeoff`). All names below were checked to exist in `material-icons-extended` 1.7.8.

**Files:**
- Modify: `feature/home/face/categories/build.gradle.kts`
- Create: `feature/home/face/categories/src/test/kotlin/es/sebas1705/categories/models/CategoryIconsTest.kt`
- Modify: `feature/home/face/categories/src/main/java/es/sebas1705/categories/models/CategoryIcons.kt`

- [ ] **Step 1: Enable unit tests in the module**

In `feature/home/face/categories/build.gradle.kts` use:

```kotlin
plugins {
    id("buildlogic.android.feature")
    alias(libs.plugins.buildlogic.unit.test)
}

android {
    namespace = "es.sebas1705.feature.home.face.categories"
}

dependencies {
    api(projects.core.common)
    api(projects.core.ui)
    api(projects.core.designsystem)
    api(projects.domain.models)
    api(projects.domain.usescases.game)
    implementation(libs.lottie.compose)

    api(projects.feature.home.face.common)

    testImplementation(libs.junit.junit)
}
```

- [ ] **Step 2: Write the failing test**

`feature/home/face/categories/src/test/kotlin/es/sebas1705/categories/models/CategoryIconsTest.kt`:

```kotlin
package es.sebas1705.categories.models

import es.sebas1705.models.Categories
import org.junit.Assert.assertTrue
import org.junit.Test

class CategoryIconsTest {

    @Test
    fun `every category has its own icon`() {
        val shared = Categories.entries
            .groupBy { it.toCategoryIcon().name }
            .filterValues { it.size > 1 }

        assertTrue("Categories sharing an icon: $shared", shared.isEmpty())
    }
}
```

- [ ] **Step 3: Run it and confirm it fails**

Run: `./gradlew :feature:home:face:categories:testDebugUnitTest --tests "es.sebas1705.categories.models.CategoryIconsTest" --console=plain`
Expected: FAILED listing shared icons (e.g. `Outlined.Description`, `Outlined.Groups`, `Outlined.Computer`).
If it instead fails with "Method ... not mocked" (an Android stub being hit by `ImageVector`), add `@RunWith(RobolectricTestRunner::class)` and `@Config(sdk = [33])` to the test class exactly as in `data/analytics/src/test/.../LogEventDataSourceTest.kt`, then re-run and expect the assertion failure above.

- [ ] **Step 4: Replace the mapping**

Replace the whole content of `CategoryIcons.kt` with:

```kotlin
package es.sebas1705.categories.models

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Balance
import androidx.compose.material.icons.outlined.Biotech
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Celebration
import androidx.compose.material.icons.outlined.Chair
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material.icons.outlined.Computer
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.EmojiEmotions
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material.icons.outlined.Fastfood
import androidx.compose.material.icons.outlined.FlightTakeoff
import androidx.compose.material.icons.outlined.Gavel
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.HistoryEdu
import androidx.compose.material.icons.outlined.Kitchen
import androidx.compose.material.icons.outlined.Landscape
import androidx.compose.material.icons.outlined.LocationCity
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.Mood
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.NoAdultContent
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.RocketLaunch
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material.icons.outlined.Stars
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.Train
import androidx.compose.material.icons.outlined.Tv
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material.icons.outlined.Work
import androidx.compose.ui.graphics.vector.ImageVector
import es.sebas1705.models.Categories

/** One distinct icon per category; `CategoryIconsTest` fails if two categories share one. */
@Suppress("CyclomaticComplexMethod")
internal fun Categories.toCategoryIcon(): ImageVector = when (this) {
    Categories.ANIMALS -> Icons.Outlined.Pets
    Categories.FOOD_AND_DRINKS -> Icons.Outlined.Fastfood
    Categories.SPORTS -> Icons.Outlined.SportsSoccer
    Categories.MUSIC -> Icons.Outlined.MusicNote
    Categories.MOVIES_AND_SERIES -> Icons.Outlined.Movie
    Categories.ANIME -> Icons.Outlined.Tv
    Categories.VIDEO_GAMES -> Icons.Outlined.SportsEsports
    Categories.CELEBRITIES -> Icons.Outlined.Stars
    Categories.SOCIAL_MEDIA_AND_STREAMERS -> Icons.Outlined.Videocam
    Categories.INTERNET_AND_MEMES -> Icons.Outlined.EmojiEmotions
    Categories.BRANDS -> Icons.Outlined.Sell
    Categories.STORES_AND_RETAIL -> Icons.Outlined.Storefront
    Categories.APPS_AND_PLATFORMS -> Icons.Outlined.Apps
    Categories.CARS_AND_MOTORSPORT -> Icons.Outlined.DirectionsCar
    Categories.CITIES_AND_LANDMARKS -> Icons.Outlined.LocationCity
    Categories.ADULT_18_PLUS -> Icons.Outlined.NoAdultContent
    Categories.TECHNOLOGY -> Icons.Outlined.Computer
    Categories.SCIENCE_PHYSICS -> Icons.Outlined.Bolt
    Categories.SCIENCE_CHEMISTRY -> Icons.Outlined.Science
    Categories.SCIENCE_BIOLOGY -> Icons.Outlined.Biotech
    Categories.SPACE_AND_ASTRONOMY -> Icons.Outlined.RocketLaunch
    Categories.HISTORY -> Icons.Outlined.HistoryEdu
    Categories.GEOGRAPHY -> Icons.Outlined.Public
    Categories.TRANSPORT -> Icons.Outlined.Train
    Categories.PROFESSIONS -> Icons.Outlined.Work
    Categories.SCHOOL_AND_EDUCATION -> Icons.Outlined.School
    Categories.HEALTH_AND_MEDICINE -> Icons.Outlined.MedicalServices
    Categories.FASHION_AND_CLOTHING -> Icons.Outlined.Checkroom
    Categories.HOME_AND_FURNITURE -> Icons.Outlined.Chair
    Categories.NATURE_AND_WEATHER -> Icons.Outlined.Landscape
    Categories.HOLIDAYS_AND_CELEBRATIONS -> Icons.Outlined.Celebration
    Categories.MYTHOLOGY_AND_FANTASY -> Icons.Outlined.AutoAwesome
    Categories.BOOKS_AND_LITERATURE -> Icons.Outlined.Book
    Categories.ART_AND_DESIGN -> Icons.Outlined.Palette
    Categories.BUSINESS_AND_FINANCE -> Icons.Outlined.AccountBalance
    Categories.POLITICS_AND_LAW -> Icons.Outlined.Gavel
    Categories.CULTURE_AND_TRADITIONS -> Icons.Outlined.Groups
    Categories.TRAVEL_AND_TOURISM -> Icons.Outlined.FlightTakeoff
    Categories.SUPERHEROES_AND_COMICS -> Icons.Outlined.Shield
    Categories.BOARD_GAMES_AND_TABLETOP -> Icons.Outlined.Extension
    Categories.KITCHEN_AND_COOKWARE -> Icons.Outlined.Kitchen
    Categories.OFFICE_AND_STATIONERY -> Icons.Outlined.Description
    Categories.EMOTIONS_AND_FEELINGS -> Icons.Outlined.Mood
    Categories.VALUES_AND_ETHICS -> Icons.Outlined.Balance
}
```

- [ ] **Step 5: Run the test and confirm it passes**

Run: `./gradlew :feature:home:face:categories:testDebugUnitTest --tests "es.sebas1705.categories.models.CategoryIconsTest" --console=plain`
Expected: `every category has its own icon PASSED`. (`when` is exhaustive, so a missing category would fail compilation.)

- [ ] **Step 6: Commit**

```bash
git add feature/home/face/categories
git commit -m "feat(categories): give every category its own icon

Replaces shared/unrelated icons with a distinct, fitting icon per category
and adds CategoryIconsTest to keep them unique."
```

---

### Task 5: Docs and changelog

**Files:**
- Modify: `docs/design.md`
- Modify: `CHANGELOG.md`

- [ ] **Step 1: Make `docs/design.md` match the code**

Replace the "### Color Palette (Core UI)" subsection (through the end of its bullet list) with:

```markdown
### Color Palette (Core UI)

The theme uses Material 3 with three contrast levels (Low, Medium, High) in light and dark mode
(six schemes in `core/ui/.../theme/Color.kt` and `Theme.kt`). Low-contrast light values:

- **Primary:** Blue (`#1565C0`) — main action and interaction color.
- **Secondary:** Teal (`#00796B`) — confirmation, civilians, positive elements.
- **Tertiary:** Light blue (`#0277BD`) — secondary emphasis.
- **Error:** Red (`#BA1A1A`) — errors and impostor roles.
- **Background (light):** cool white (`#F7FAFF`).

`ContrastTest` (core/ui) fails if any key foreground/background pair of any scheme drops below WCAG AA (4.5:1).

### Design tokens and base components

- Spacing (`AppDimens.spacing`): xs 4, sm 8, md 12, lg 16, xl 24, xxl 32 dp.
- Elevation (`AppDimens.elevation`): none 0, low 1, medium 3, high 6 dp.
- Shapes: `appShapes` (4/8/12/16/24 dp); cards use `MaterialTheme.cardShape` (the `large` shape).
- Components in `core/ui/.../components`: `AppCard`, `SectionHeader`, `StatusChip`, `PrimaryActionButton`.
  New screens should use them instead of inline `dp` values and `RoundedCornerShape(...)`.
- Category icons: one distinct Material `Outlined` icon per category (`CategoryIcons.kt`, guarded by `CategoryIconsTest`).
```

In "## 4. Areas for Improvement" mark the two finished items and point to the plan:

```markdown
- ~~**Icon Consistency**~~ Done in the visual foundation (phase 0).
- **Visual Feedback:** Enhance transitions between game phases to feel more dynamic and alive.
- **Results Hierarchy:** Make the winners screen more visually impactful and celebratory.
- ~~**Accessibility (contrast)**~~ Guarded by `ContrastTest`; touch targets and font scaling are checked per phase.

Roadmap: `docs/designs/2026-10-18-visual-refresh-design.md`.
```

- [ ] **Step 2: Changelog**

Under `## [Unreleased]` in `CHANGELOG.md` add (create the `### Changed` heading if it does not exist):

```markdown
### Changed
- Medium-contrast light theme: darker secondary/tertiary containers so their text meets WCAG AA.
- Each word category now has its own, more fitting icon.
- Internal: spacing/elevation tokens and base UI components in `core/ui` for the visual refresh.
```

- [ ] **Step 3: Commit**

```bash
git add docs/design.md CHANGELOG.md
git commit -m "docs: update design doc for the visual foundation"
```

---

### Task 6: Final verification

- [ ] **Step 1: Build, detekt, tests for the touched modules**

Run:

```bash
./gradlew :app:compileDevDebugKotlin :core:ui:detekt :feature:home:face:categories:detekt :core:ui:testDebugUnitTest :feature:home:face:categories:testDebugUnitTest --console=plain
```

Expected: BUILD SUCCESSFUL; `ContrastTest` and `CategoryIconsTest` PASSED; no new detekt findings in files touched by this plan (pre-existing findings elsewhere are not part of this phase).

- [ ] **Step 2: Visual check (manual, in Android Studio or an emulator)**

Open the four component previews (`AppCardPreview`, `SectionHeaderPreview`, `StatusChipPreview`, `PrimaryActionButtonPreview`) in light and dark, and open the categories screen (Home → categories) to confirm the new icons look right and none is missing. Note anything off in the PR description; do not change designs here.

- [ ] **Step 3: Ask before publishing**

Do not push or open a PR without the owner's go-ahead. When approved: push `design/visual-refresh` and open a PR to `develop` titled "feat(ui): visual foundation (phase 0)", body listing what changed (contrast guard + 2 color fixes, tokens, 4 components, category icons, docs) and what was verified (Step 1) vs not (emulator run).

---

## Self-review

- **Spec coverage:** tokens → Task 2; base components (4) → Task 3; contrast audit → Task 1; icon set → Task 4; `docs/design.md` update → Task 5; acceptance (build + detekt + contrast test + docs) → Task 6. The spec's "`CardShape` alias to `large`" is `MaterialTheme.cardShape`. The spec's open point about `PrimaryActionButton` is kept as a component (it centralizes the click sound and 48 dp height).
- **Placeholders:** none; every step has the code or exact command.
- **Type consistency:** `AppDimens.spacing/elevation`, `LocalSpacing`, `LocalElevations`, `Spacing`, `Elevations`, `cardShape`, `StatusChipVariant` are defined in Tasks 2–3 before use; `toCategoryIcon()` keeps its signature.
- **Known risks:** `ImageVector.name` may need Robolectric in a plain JVM test (contingency in Task 4 Step 3); `UiModePreviews`/`LocalSoundPlayer` import paths are copied from `FaceDesign.kt` (contingency in Task 3 Step 5).
