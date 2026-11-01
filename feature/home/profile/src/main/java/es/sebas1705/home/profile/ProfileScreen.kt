package es.sebas1705.home.profile

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import es.sebas1705.home.profile.design.DeleteAccountDialog
import es.sebas1705.home.profile.design.ProfileDesign
import es.sebas1705.home.profile.viewmodel.ProfileIntent
import es.sebas1705.home.profile.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    onSignOut: () -> Unit = {},
    onDebugNav: () -> Unit = {},
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by profileViewModel.uiState.collectAsStateWithLifecycle()
    val loading by profileViewModel.loading.collectAsStateWithLifecycle()
    val activity = requireNotNull(LocalActivity.current) { "ProfileScreen must be hosted in an Activity" }

    LaunchedEffect(null) {
        profileViewModel.eventHandler(ProfileIntent.Load)
    }

    LaunchedEffect(uiState.navigateToLogin, onSignOut) {
        if (uiState.navigateToLogin) {
            onSignOut()
            profileViewModel.eventHandler(ProfileIntent.ConsumeSignOutNavigation)
        }
    }

    ProfileDesign(
        modifier = modifier,
        selectedTab = uiState.selectedTab,
        isLoadingOfflineRecords = loading,
        offlineRecordRows = uiState.offlineRecordRows,
        offlineRecordSort = uiState.offlineRecordSort,
        rolePreference = uiState.rolePreference,
        favoriteCategory = uiState.favoriteCategory,
        matchesPlayed = uiState.matchesPlayed,
        currentStreak = uiState.currentStreak,
        bestStreak = uiState.bestStreak,
        errorMessage = uiState.errorMessage,
        onSelectTab = { tab -> profileViewModel.eventHandler(ProfileIntent.SelectTab(tab)) },
        onToggleOfflineRecordSort = { column ->
            profileViewModel.eventHandler(ProfileIntent.ToggleOfflineRecordSort(column))
        },
        onSignOut = { profileViewModel.eventHandler(ProfileIntent.SignOut) },
        canDeleteAccount = uiState.canDeleteAccount,
        onDeleteAccount = { profileViewModel.eventHandler(ProfileIntent.RequestDeleteAccount) },
        onDebugNav = onDebugNav
    )

    DeleteAccountDialog(
        step = uiState.deleteAccountStep,
        errorMessage = uiState.deleteAccountError,
        onDismiss = { profileViewModel.eventHandler(ProfileIntent.DismissDeleteAccount) },
        onConfirm = { profileViewModel.eventHandler(ProfileIntent.ConfirmDeleteAccount) },
        onPasswordConfirm = { password ->
            profileViewModel.eventHandler(ProfileIntent.ReauthenticateWithPassword(password))
        },
        onGoogleConfirm = {
            profileViewModel.eventHandler(ProfileIntent.ReauthenticateWithGoogle(activity))
        }
    )
}

