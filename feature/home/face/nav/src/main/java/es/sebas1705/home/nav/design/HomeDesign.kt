package es.sebas1705.home.nav.design

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import es.sebas1705.categories.CategoriesFullScreenDialog
import es.sebas1705.common.utlis.extensions.primitives.pushAndFree
import es.sebas1705.common.utlis.extensions.types.logD
import es.sebas1705.common.utlis.extensions.types.logI
import es.sebas1705.core.resources.Sounds
import androidx.compose.ui.res.stringResource
import es.sebas1705.ui.sound.LocalSoundPlayer
import es.sebas1705.game.UserFullScreenDialog
import es.sebas1705.home.nav.HomeGraph
import es.sebas1705.home.nav.models.homeTabs
import es.sebas1705.home.nav.viewmodel.FaceIntent
import es.sebas1705.home.nav.viewmodel.FaceViewModel
import es.sebas1705.home.profile.ProfileScreen
import es.sebas1705.home.ranking.RankingScreen
import es.sebas1705.mode.ModeFullScreenDialog
import kotlinx.collections.immutable.toImmutableList

private const val HOME_NAV_ANIMATION_DURATION_MS = 240
private const val HOME_DESIGN_LOG_TAG = "HomeDesignFlow"

private fun homeDesignLogD(message: String) = HOME_DESIGN_LOG_TAG.logD(message)
private fun homeDesignLogI(message: String) = HOME_DESIGN_LOG_TAG.logI(message)

private fun homeForwardTransition() = ContentTransform(
    targetContentEnter = fadeIn(
        animationSpec = tween(
            durationMillis = HOME_NAV_ANIMATION_DURATION_MS,
            easing = FastOutSlowInEasing
        )
    ) + slideInHorizontally(
        initialOffsetX = { fullWidth -> fullWidth / 5 },
        animationSpec = tween(
            durationMillis = HOME_NAV_ANIMATION_DURATION_MS,
            easing = FastOutSlowInEasing
        )
    ),
    initialContentExit = fadeOut(
        animationSpec = tween(durationMillis = HOME_NAV_ANIMATION_DURATION_MS / 2)
    ) + slideOutHorizontally(
        targetOffsetX = { fullWidth -> -(fullWidth / 12) },
        animationSpec = tween(durationMillis = HOME_NAV_ANIMATION_DURATION_MS)
    )
)

private fun homeBackwardTransition() = ContentTransform(
    targetContentEnter = fadeIn(
        animationSpec = tween(
            durationMillis = HOME_NAV_ANIMATION_DURATION_MS,
            easing = FastOutSlowInEasing
        )
    ) + slideInHorizontally(
        initialOffsetX = { fullWidth -> -(fullWidth / 5) },
        animationSpec = tween(
            durationMillis = HOME_NAV_ANIMATION_DURATION_MS,
            easing = FastOutSlowInEasing
        )
    ),
    initialContentExit = fadeOut(
        animationSpec = tween(durationMillis = HOME_NAV_ANIMATION_DURATION_MS / 2)
    ) + slideOutHorizontally(
        targetOffsetX = { fullWidth -> fullWidth / 12 },
        animationSpec = tween(durationMillis = HOME_NAV_ANIMATION_DURATION_MS)
    )
)

@Composable
@Suppress("LongMethod")
fun HomeDesign(
    modifier: Modifier = Modifier,
    backStack: NavBackStack<NavKey> = NavBackStack(HomeGraph.FaceScreen),
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
    faceViewModel: FaceViewModel = hiltViewModel()
) {
    val faceState by faceViewModel.uiState.collectAsStateWithLifecycle()
    val faceLoading by faceViewModel.loading.collectAsStateWithLifecycle()
    val showCategoriesDialog = rememberSaveable { mutableStateOf(false) }
    val showUsersDialog = rememberSaveable { mutableStateOf(false) }
    val showModeDialog = rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(null) {
        homeDesignLogI("load face state")
        faceViewModel.eventHandler(FaceIntent.Load)
    }

    LaunchedEffect(backStack.lastOrNull()) {
        homeDesignLogD("tab destination current=${backStack.lastOrNull()} size=${backStack.size}")
    }

    LaunchedEffect(showCategoriesDialog.value, showUsersDialog.value, showModeDialog.value) {
        homeDesignLogD(
            "dialogs categories=${showCategoriesDialog.value} users=${showUsersDialog.value} " +
                    "mode=${showModeDialog.value}"
        )
    }

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(),
        bottomBar = {
            val sound = LocalSoundPlayer.current
            NavigationBar {
                homeTabs.forEach { tab ->
                    val tabLabel = stringResource(tab.labelRes)
                    val isSelected = backStack.lastOrNull() == tab.key
                    val scale by animateFloatAsState(
                        targetValue = if (isSelected) 1.15f else 1f,
                        animationSpec = spring(dampingRatio = 0.55f, stiffness = 420f),
                        label = "tab_scale"
                    )
                    val iconColor by animateColorAsState(
                        targetValue = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        label = "tab_color"
                    )
                    val labelColor by animateColorAsState(
                        targetValue = if (isSelected) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        label = "tab_label_color"
                    )
                    val indicatorColor by animateColorAsState(
                        targetValue = if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.75f)
                        } else {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0f)
                        },
                        label = "tab_indicator_color"
                    )

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            if (backStack.lastOrNull() != tab.key) {
                                homeDesignLogD("tab click from=${backStack.lastOrNull()} to=${tab.key}")
                                sound(Sounds.CLK_TAP)
                                backStack.pushAndFree(tab.key)
                            }
                        },
                        icon = {
                            val iconModifier = Modifier.graphicsLayer(
                                scaleX = scale,
                                scaleY = scale
                            )

                            if (tab.key == HomeGraph.RankingScreen) {
                                BadgedBox(
                                    badge = {
                                        if (!isSelected) {
                                            Badge(
                                                containerColor = MaterialTheme.colorScheme.tertiary,
                                                contentColor = MaterialTheme.colorScheme.onTertiary
                                            )
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = tab.icon,
                                        contentDescription = tabLabel,
                                        modifier = iconModifier,
                                        tint = iconColor
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tabLabel,
                                    modifier = iconModifier,
                                    tint = iconColor
                                )
                            }
                        },
                        label = { Text(tabLabel, color = labelColor) },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = indicatorColor
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        NavDisplay(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            backStack = backStack,
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator()
            ),
            transitionSpec = { homeForwardTransition() },
            popTransitionSpec = { homeBackwardTransition() },
            entryProvider = entryProvider {
                entry<HomeGraph.FaceScreen> {
                    FaceScreen(
                        faceState = faceState,
                        isLoading = faceLoading,
                        onOpenUser = {
                            homeDesignLogD("open users dialog")
                            showUsersDialog.value = true
                        },
                        onOpenCategories = {
                            homeDesignLogD("open categories dialog")
                            showCategoriesDialog.value = true
                        },
                        onOpenMode = {
                            homeDesignLogD("open mode dialog")
                            showModeDialog.value = true
                        },
                        onStartOfflineGame = {
                            homeDesignLogI(
                                "start offline game users=${faceState.users.size} " +
                                        "selectedCategories=${faceState.categoriesStates.count { it.value }} " +
                                        "mode=${faceState.mode} impostors=${faceState.impostors} " +
                                        "showImpostorsInResult=${faceState.showImpostorsInResult} " +
                                        "timer=${faceState.discussionTimerSeconds} " +
                                        "impostorsKnow=${faceState.impostorsKnowEachOther}"
                            )
                            onOpenOfflineGame(
                                faceState.users.toList(),
                                faceState.categoriesStates
                                    .filter { it.value }
                                    .keys
                                    .map { it.name },
                                faceState.mode.name,
                                faceState.impostors,
                                faceState.showImpostorsInResult,
                                faceState.discussionTimerSeconds,
                                faceState.impostorsKnowEachOther,
                                faceState.showNumOfImpostors
                            )
                        },
                        onOpenSettings = onOpenSettings
                    )
                }
                entry<HomeGraph.RankingScreen> {
                    RankingScreen()
                }
                entry<HomeGraph.ProfileScreen> {
                    ProfileScreen(
                        onSignOut = onSignOut,
                        onDebugNav = onDebugNav
                    )
                }
            }
        )

        if (showCategoriesDialog.value)
            CategoriesFullScreenDialog(
                categoriesStates = faceState.categoriesStates,
                onDismiss = {
                    homeDesignLogD("dismiss categories dialog")
                    showCategoriesDialog.value = false
                }
            )

        if (showUsersDialog.value)
            UserFullScreenDialog(
                users = faceState.users,
                onDismiss = {
                    homeDesignLogD("dismiss users dialog")
                    showUsersDialog.value = false
                }
            )

        if (showModeDialog.value)
            ModeFullScreenDialog(
                faceState = faceState,
                onDismiss = {
                    homeDesignLogD("dismiss mode dialog")
                    showModeDialog.value = false
                }
            )
    }

}


