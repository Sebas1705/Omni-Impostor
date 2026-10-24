package es.sebas1705.login

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import es.sebas1705.login.design.LoginDesign
import es.sebas1705.login.viewmodel.LoginIntent
import es.sebas1705.login.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    onLoginSuccess: (isGuest: Boolean) -> Unit = {},
    loginViewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by loginViewModel.uiState.collectAsStateWithLifecycle()
    val loading by loginViewModel.loading.collectAsStateWithLifecycle()
    val activity = requireNotNull(LocalActivity.current) { "LoginScreen must be hosted in an Activity" }

    LaunchedEffect(null) {
        loginViewModel.eventHandler(LoginIntent.CheckSession)
    }

    LaunchedEffect(uiState.navigateToHome) {
        if (uiState.navigateToHome) {
            onLoginSuccess(uiState.navigateAsGuest)
            loginViewModel.eventHandler(LoginIntent.ConsumeNavigation)
        }
    }

    LoginDesign(
        modifier = modifier,
        isCheckingSession = uiState.isCheckingSession,
        loading = loading,
        errorMessage = uiState.errorMessage,
        loginMode = uiState.loginMode,
        resetEmailSent = uiState.resetEmailSent,
        resendCooldownSeconds = uiState.resendCooldownSeconds,
        onGoogleSignIn = { loginViewModel.eventHandler(LoginIntent.SignInWithGoogle(activity)) },
        onSignInWithEmail = { email, password ->
            loginViewModel.eventHandler(LoginIntent.SignInWithEmail(email, password))
        },
        onSignUpWithEmail = { email, password ->
            loginViewModel.eventHandler(LoginIntent.SignUpWithEmail(email, password))
        },
        onSendPasswordReset = { email ->
            loginViewModel.eventHandler(LoginIntent.SendPasswordReset(email))
        },
        onSignInAsGuest = { loginViewModel.eventHandler(LoginIntent.SignInAsGuest) },
        onResendVerificationEmail = { loginViewModel.eventHandler(LoginIntent.ResendVerificationEmail) },
        onCheckEmailVerified = { loginViewModel.eventHandler(LoginIntent.CheckEmailVerified) },
        onCancelEmailVerification = { loginViewModel.eventHandler(LoginIntent.CancelEmailVerification) },
        onChangeMode = { mode -> loginViewModel.eventHandler(LoginIntent.ChangeMode(mode)) },
        onDismissError = { loginViewModel.eventHandler(LoginIntent.ClearError) }
    )
}
