package es.sebas1705.main.components

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import es.sebas1705.common.utlis.extensions.primitives.pop
import es.sebas1705.common.utlis.extensions.primitives.push
import es.sebas1705.common.utlis.extensions.primitives.pushAndFree
import es.sebas1705.common.utlis.extensions.types.logD
import es.sebas1705.common.utlis.extensions.types.logI
import es.sebas1705.debug.DebugToolsScreen
import es.sebas1705.home.nav.HomeNav
import es.sebas1705.login.LoginScreen
import es.sebas1705.main.AppGraph
import es.sebas1705.models.Categories
import es.sebas1705.models.Modes
import es.sebas1705.offlinegame.OfflineGameScreen
import es.sebas1705.onlinegame.OnlineGameScreen
import es.sebas1705.splash.SplashScreen
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableSet

private const val NAV_ANIMATION_DURATION_MS = 280
private const val APP_NAV_LOG_TAG = "AppNavFlow"

private fun appNavLogD(message: String) = APP_NAV_LOG_TAG.logD(message)
private fun appNavLogI(message: String) = APP_NAV_LOG_TAG.logI(message)

private fun forwardTransition() = ContentTransform(
    targetContentEnter = fadeIn(
        animationSpec = tween(
            durationMillis = NAV_ANIMATION_DURATION_MS,
            easing = FastOutSlowInEasing
        )
    ) + slideInHorizontally(
        initialOffsetX = { fullWidth -> fullWidth / 4 },
        animationSpec = tween(
            durationMillis = NAV_ANIMATION_DURATION_MS,
            easing = FastOutSlowInEasing
        )
    ),
    initialContentExit = fadeOut(
        animationSpec = tween(durationMillis = NAV_ANIMATION_DURATION_MS / 2)
    ) + slideOutHorizontally(
        targetOffsetX = { fullWidth -> -(fullWidth / 10) },
        animationSpec = tween(durationMillis = NAV_ANIMATION_DURATION_MS)
    )
)

private fun backwardTransition() = ContentTransform(
    targetContentEnter = fadeIn(
        animationSpec = tween(
            durationMillis = NAV_ANIMATION_DURATION_MS,
            easing = FastOutSlowInEasing
        )
    ) + slideInHorizontally(
        initialOffsetX = { fullWidth -> -(fullWidth / 4) },
        animationSpec = tween(
            durationMillis = NAV_ANIMATION_DURATION_MS,
            easing = FastOutSlowInEasing
        )
    ),
    initialContentExit = fadeOut(
        animationSpec = tween(durationMillis = NAV_ANIMATION_DURATION_MS / 2)
    ) + slideOutHorizontally(
        targetOffsetX = { fullWidth -> fullWidth / 10 },
        animationSpec = tween(durationMillis = NAV_ANIMATION_DURATION_MS)
    )
)

@Composable
@Suppress("LongMethod")
internal fun AppNavigationContent(
    backStack: NavBackStack<NavKey>,
    isGuestUser: Boolean,
    onGuestStatusChange: (Boolean) -> Unit,
    onOpenSettings: () -> Unit,
) {
    LaunchedEffect(backStack.lastOrNull()) {
        appNavLogD("destination changed current=${backStack.lastOrNull()} size=${backStack.size}")
    }

    NavDisplay(
        modifier = Modifier.fillMaxSize(),
        backStack = backStack,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        transitionSpec = { forwardTransition() },
        popTransitionSpec = { backwardTransition() },
        entryProvider = entryProvider {
            entry<AppGraph.SplashScreen> {
                appNavLogI("render SplashScreen")
                SplashScreen()
            }
            entry<AppGraph.LoginScreen> {
                appNavLogI("render LoginScreen")
                LoginScreen(
                    onLoginSuccess = { isGuest ->
                        appNavLogI("event login_success isGuest=$isGuest -> HomeScreen")
                        onGuestStatusChange(isGuest)
                        backStack.pushAndFree(AppGraph.HomeScreen)
                    }
                )
            }
            entry<AppGraph.HomeScreen> {
                appNavLogI("render HomeScreen")
                HomeNav(
                    modifier = Modifier.fillMaxSize(),
                    isGuestUser = isGuestUser,
                    onSignOut = {
                        appNavLogI("event sign_out -> LoginScreen")
                        backStack.pushAndFree(AppGraph.LoginScreen)
                    },
                    onDebugNav = {
                        appNavLogD("event open_debug_tools")
                        backStack.push(AppGraph.DebugToolsScreen)
                    },
                    onOpenOfflineGame = { players, categories, modeName, impostors, showImpostorsInResult, discussionTimerSeconds, impostorsKnowEachOther, showNumOfImpostors ->
                        appNavLogI(
                            "event open_offline_game players=${players.size} categories=${categories.size} " +
                                "mode=$modeName impostors=$impostors showImpostorsInResult=$showImpostorsInResult " +
                                "timer=$discussionTimerSeconds impostorsKnow=$impostorsKnowEachOther"
                        )
                        backStack.push(
                            AppGraph.OfflineGameScreen(
                                players = players,
                                categories = categories,
                                modeName = modeName,
                                impostors = impostors,
                                showImpostorsInResult = showImpostorsInResult,
                                discussionTimerSeconds = discussionTimerSeconds,
                                impostorsKnowEachOther = impostorsKnowEachOther,
                                showNumOfImpostors = showNumOfImpostors
                            )
                        )
                    },
                    onOpenOnlineGame = { categories, modeName, impostors, networkMode ->
                        appNavLogI(
                            "event open_online_game categories=${categories.size} " +
                                "mode=$modeName impostors=$impostors networkMode=$networkMode"
                        )
                        backStack.push(
                            AppGraph.OnlineGameScreen(
                                categories = categories,
                                modeName = modeName,
                                impostors = impostors,
                                networkMode = networkMode,
                            )
                        )
                    },
                    onOpenSettings = onOpenSettings
                )
            }
            entry<AppGraph.OnlineGameScreen> { route ->
                appNavLogI(
                    "render OnlineGameScreen categories=${route.categories.size} " +
                        "mode=${route.modeName} impostors=${route.impostors} network=${route.networkMode}"
                )
                OnlineGameScreen(
                    modifier = Modifier.fillMaxSize(),
                    categories = route.categories
                        .mapNotNull { name -> Categories.entries.firstOrNull { it.name == name } }
                        .toImmutableSet(),
                    modeName = route.modeName,
                    impostors = route.impostors,
                    networkMode = route.networkMode,
                    onBack = {
                        appNavLogI("event online_back")
                        backStack.pop()
                    }
                )
            }
            entry<AppGraph.OfflineGameScreen> { route ->
                appNavLogI(
                    "render OfflineGameScreen routePlayers=${route.players.size} routeCategories=${route.categories.size} " +
                        "mode=${route.modeName} impostors=${route.impostors}"
                )
                OfflineGameScreen(
                    modifier = Modifier.fillMaxSize(),
                    players = route.players.toImmutableList(),
                    categories = route.categories
                        .mapNotNull { name -> Categories.entries.firstOrNull { it.name == name } }
                        .toImmutableSet(),
                    mode = Modes.entries.firstOrNull { it.name == route.modeName } ?: Modes.Classic,
                    impostors = route.impostors,
                    showImpostorsInResult = route.showImpostorsInResult,
                    discussionTimerSeconds = route.discussionTimerSeconds,
                    impostorsKnowEachOther = route.impostorsKnowEachOther,
                    showNumOfImpostors = route.showNumOfImpostors,
                    onBack = {
                        appNavLogI("event offline_back")
                        backStack.pop()
                    }
                )
            }
            entry<AppGraph.DebugToolsScreen> {
                appNavLogI("render DebugToolsScreen")
                DebugToolsScreen(
                    onBack = {
                        appNavLogD("event debug_tools_back")
                        backStack.pop()
                    }
                )
            }
        }
    )
}


