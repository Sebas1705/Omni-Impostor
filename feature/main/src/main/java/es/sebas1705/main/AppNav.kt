package es.sebas1705.main

import android.app.Activity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.os.LocaleListCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.rememberNavBackStack
import es.sebas1705.common.utlis.extensions.primitives.push
import es.sebas1705.common.utlis.extensions.primitives.pushAndFree
import es.sebas1705.main.design.AppNavDesign
import es.sebas1705.main.models.AppNavOverlayActions
import es.sebas1705.main.models.AppNavOverlayState
import es.sebas1705.main.viewmodel.MainIntent
import es.sebas1705.main.viewmodel.MainViewModel
import es.sebas1705.models.DarkThemePreference
import es.sebas1705.ui.adaptive.LocalForceCompactTables
import es.sebas1705.ui.sound.LocalSoundPlayer
import es.sebas1705.ui.theme.AppTheme
import java.util.Locale

/**
 * Navigation for the app.
 *
 * @since 0.1.0
 * @author Sebas1705 01/03/2025
 */
@Composable
@Suppress("LongMethod")
fun AppNav(
    modifier: Modifier = Modifier,
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val backStack = rememberNavBackStack(AppGraph.SplashScreen)
    val mainState by mainViewModel.uiState.collectAsStateWithLifecycle()
    val lastAppliedLanguageTag = rememberSaveable {
        mutableStateOf(AppCompatDelegate.getApplicationLocales().toLanguageTags())
    }
    val currentDestination = backStack.lastOrNull() as? AppGraph
    val showSettingsDialog = rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(null) {
        mainViewModel.eventHandler(MainIntent.ChargeData)
    }
    LaunchedEffect(mainState.splashFinished, currentDestination) {
        if (mainState.splashFinished && currentDestination is AppGraph.SplashScreen) {
            backStack.pushAndFree(
                if (mainState.isUserLogged) AppGraph.HomeScreen
                else AppGraph.LoginScreen
            )
        }
    }

    LaunchedEffect(mainState.appLanguage, mainState.splashFinished) {
        if (!mainState.splashFinished) return@LaunchedEffect

        val desiredTag = mainState.appLanguage.code
        if (lastAppliedLanguageTag.value.equals(desiredTag, ignoreCase = true)) {
            return@LaunchedEffect
        }

        Locale.setDefault(Locale.forLanguageTag(desiredTag))
        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(desiredTag)
        )
        lastAppliedLanguageTag.value = desiredTag
        // Force resource refresh for ComponentActivity hosts.
        (context as? Activity)?.recreate()
    }

    val systemInDark = isSystemInDarkTheme()
    val darkTheme = when (mainState.darkThemePreference) {
        DarkThemePreference.Light -> false
        DarkThemePreference.Dark -> true
        DarkThemePreference.System -> systemInDark
    }
    AppTheme(
        modifier = modifier,
        darkTheme = darkTheme,
        themeContrast = mainState.themeContrast
    ) {
        CompositionLocalProvider(
            LocalForceCompactTables provides mainState.forceCompactTables,
            LocalSoundPlayer provides { sound -> mainViewModel.playClick(sound, mainState.soundVolume) }
        ) {
            AppNavDesign(
                modifier = Modifier.fillMaxSize(),
                backStack = backStack,
                isGuestUser = mainState.isGuestUser,
                onGuestStatusChange = { isGuest ->
                    mainViewModel.eventHandler(MainIntent.SetGuestUser(isGuest))
                },
                overlayState = AppNavOverlayState(
                    currentDestination = currentDestination,
                    showSettingsDialog = showSettingsDialog.value
                ),
                overlayActions = AppNavOverlayActions(
                    onOpenSettings = { showSettingsDialog.value = true },
                    onDismissSettings = { showSettingsDialog.value = false },
                    onOpenCategories = {
                        showSettingsDialog.value = false
                        backStack.pushAndFree(AppGraph.HomeScreen)
                    },
                    onOpenDebugTools = {
                        showSettingsDialog.value = false
                        backStack.push(AppGraph.DebugToolsScreen)
                    }
                )
            )
        }
    }
}
