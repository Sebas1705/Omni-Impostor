package es.sebas1705.main.design

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import es.sebas1705.main.components.AppNavigationContent
import es.sebas1705.main.models.AppNavOverlayActions
import es.sebas1705.main.models.AppNavOverlayState
import es.sebas1705.settings.AppSettingsDialog

@Composable
internal fun AppNavDesign(
    backStack: NavBackStack<NavKey>,
    overlayState: AppNavOverlayState,
    overlayActions: AppNavOverlayActions,
    isGuestUser: Boolean = false,
    onGuestStatusChange: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier,
) = Box(
    modifier = modifier
        .fillMaxSize()
) {
    AppNavigationContent(
        backStack = backStack,
        isGuestUser = isGuestUser,
        onGuestStatusChange = onGuestStatusChange,
        onOpenSettings = overlayActions.onOpenSettings
    )

    if (overlayState.showSettingsDialog) {
        AppSettingsDialog(
            onDismiss = overlayActions.onDismissSettings,
            onOpenCategories = overlayActions.onOpenCategories,
            onOpenDebugTools = overlayActions.onOpenDebugTools,
        )
    }
}


