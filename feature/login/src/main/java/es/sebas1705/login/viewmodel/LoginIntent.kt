package es.sebas1705.login.viewmodel

import android.app.Activity
import es.sebas1705.common.mvi.MVIBaseIntent

sealed interface LoginIntent : MVIBaseIntent {
    data object CheckSession : LoginIntent
    data class SignInWithGoogle(val activity: Activity) : LoginIntent
    data object SignInAsGuest : LoginIntent
    data class SignInWithEmail(val email: String, val password: String) : LoginIntent
    data class SignUpWithEmail(val email: String, val password: String) : LoginIntent
    data class SendPasswordReset(val email: String) : LoginIntent
    data object ResendVerificationEmail : LoginIntent
    data object CheckEmailVerified : LoginIntent
    data object CancelEmailVerification : LoginIntent
    data class ChangeMode(val mode: LoginMode) : LoginIntent
    data object ConsumeNavigation : LoginIntent
    data object ClearError : LoginIntent
}
