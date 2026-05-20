package es.sebas1705.login.viewmodel

import es.sebas1705.common.mvi.MVIBaseState

data class LoginState(
    val isCheckingSession: Boolean = false,
    val navigateToHome: Boolean = false,
    val navigateAsGuest: Boolean = false,
    val errorMessage: String? = null,
    val loginMode: LoginMode = LoginMode.Main,
    val isWaitingEmailVerification: Boolean = false,
    val resetEmailSent: Boolean = false,
    val resendCooldownSeconds: Int = 0,
) : MVIBaseState
