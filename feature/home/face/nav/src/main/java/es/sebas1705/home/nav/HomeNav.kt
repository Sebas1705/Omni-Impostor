package es.sebas1705.home.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.rememberNavBackStack
import es.sebas1705.common.utlis.extensions.types.logD
import es.sebas1705.home.nav.design.HomeDesign

private const val HOME_NAV_LOG_TAG = "HomeNavFlow"

private fun homeNavLogD(message: String) = HOME_NAV_LOG_TAG.logD(message)

@Composable
@Suppress("LongMethod")
fun HomeNav(
    modifier: Modifier = Modifier,
    isGuestUser: Boolean = false,
    onSignOut: () -> Unit = {},
    onDebugNav: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onOpenOfflineGame: (
        players: List<String>,
        categories: List<String>,
        modeName: String,
        impostors: Int,
        showImpostorsInResult: Boolean,
        discussionTimerSeconds: Int,
        impostorsKnowEachOther: Boolean,
        showNumOfImpostors: Boolean
    ) -> Unit = { _, _, _, _, _, _, _, _ -> },
    onOpenOnlineGame: (
        categories: List<String>,
        modeName: String,
        impostors: Int,
        networkMode: String,
    ) -> Unit = { _, _, _, _ -> },
) {
    val backStack = rememberNavBackStack(HomeGraph.FaceScreen)

    LaunchedEffect(backStack.lastOrNull()) {
        homeNavLogD("destination changed current=${backStack.lastOrNull()} size=${backStack.size}")
    }

    HomeDesign(
        modifier = modifier,
        backStack = backStack,
        isGuestUser = isGuestUser,
        onSignOut = onSignOut,
        onDebugNav = onDebugNav,
        onOpenSettings = onOpenSettings,
        onOpenOfflineGame = onOpenOfflineGame,
        onOpenOnlineGame = onOpenOnlineGame,
    )
}

