package es.sebas1705.home.profile.viewmodel

import android.app.Activity
import es.sebas1705.common.mvi.MVIBaseIntent

sealed interface ProfileIntent : MVIBaseIntent {
    data object Load : ProfileIntent
    data object SignOut : ProfileIntent
    data object ConsumeSignOutNavigation : ProfileIntent
    data object RequestDeleteAccount : ProfileIntent
    data object DismissDeleteAccount : ProfileIntent
    data object ConfirmDeleteAccount : ProfileIntent
    data class ReauthenticateWithPassword(val password: String) : ProfileIntent
    data class ReauthenticateWithGoogle(val activity: Activity) : ProfileIntent
    data class SelectTab(val tab: ProfileTab) : ProfileIntent
    data class ToggleOfflineRecordSort(val column: ProfileOfflineRecordSortColumn) : ProfileIntent
}

