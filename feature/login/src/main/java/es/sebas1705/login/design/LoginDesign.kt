package es.sebas1705.login.design

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import es.sebas1705.common.utlis.UiModePreviews
import es.sebas1705.core.resources.Sounds
import androidx.compose.foundation.background
import es.sebas1705.login.viewmodel.LoginMode
import es.sebas1705.ui.sound.LocalSoundPlayer
import es.sebas1705.ui.theme.AppTheme
import es.sebas1705.ui.theme.makeTitle
import es.sebas1705.core.resources.R as ResourceR

@Composable
fun LoginDesign(
    modifier: Modifier = Modifier,
    isCheckingSession: Boolean = false,
    loading: Boolean = false,
    errorMessage: String? = null,
    loginMode: LoginMode = LoginMode.Main,
    resetEmailSent: Boolean = false,
    resendCooldownSeconds: Int = 0,
    onGoogleSignIn: () -> Unit = {},
    onSignInWithEmail: (email: String, password: String) -> Unit = { _, _ -> },
    onSignUpWithEmail: (email: String, password: String) -> Unit = { _, _ -> },
    onSendPasswordReset: (email: String) -> Unit = {},
    onSignInAsGuest: () -> Unit = {},
    onResendVerificationEmail: () -> Unit = {},
    onCheckEmailVerified: () -> Unit = {},
    onCancelEmailVerification: () -> Unit = {},
    onChangeMode: (LoginMode) -> Unit = {},
    onDismissError: () -> Unit = {}
) {
    val sound = LocalSoundPlayer.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp)
            .padding(vertical = 40.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val isMain = loginMode == LoginMode.Main
        Image(
            painter = painterResource(ResourceR.drawable.core_resources_ic_app_logo),
            contentDescription = stringResource(ResourceR.string.core_resources_icon_content),
            modifier = Modifier.size(if (isMain) 160.dp else 88.dp)
        )
        if (isMain) {
            Text(
                text = stringResource(ResourceR.string.core_resources_app_name),
                style = MaterialTheme.typography.headlineLarge.makeTitle(),
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.Center
            )
        }

        if (isCheckingSession || loading) {
            CircularProgressIndicator(modifier = Modifier.size(56.dp), strokeWidth = 5.dp)
        } else {
            AnimatedContent(
                targetState = loginMode,
                transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(120)) },
                label = "login_panel"
            ) { mode ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        when (mode) {
                            LoginMode.Main -> MainPanel(
                                sound = sound,
                                onGoogleSignIn = onGoogleSignIn,
                                onSignInEmail = { onChangeMode(LoginMode.EmailLogin) },
                                onSignUpEmail = { onChangeMode(LoginMode.EmailRegister) },
                                onGuest = onSignInAsGuest,
                                errorMessage = errorMessage,
                                onDismissError = onDismissError
                            )
                            LoginMode.EmailLogin -> EmailLoginPanel(
                                sound = sound,
                                onSignIn = onSignInWithEmail,
                                onForgotPassword = { onChangeMode(LoginMode.ForgotPassword) },
                                onBack = { onChangeMode(LoginMode.Main) },
                                errorMessage = errorMessage,
                                onDismissError = onDismissError
                            )
                            LoginMode.EmailRegister -> EmailRegisterPanel(
                                sound = sound,
                                onSignUp = onSignUpWithEmail,
                                onBack = { onChangeMode(LoginMode.Main) },
                                errorMessage = errorMessage,
                                onDismissError = onDismissError
                            )
                            LoginMode.ForgotPassword -> ForgotPasswordPanel(
                                sound = sound,
                                resetEmailSent = resetEmailSent,
                                onSendReset = onSendPasswordReset,
                                onBack = { onChangeMode(LoginMode.EmailLogin) },
                                errorMessage = errorMessage,
                                onDismissError = onDismissError
                            )
                            LoginMode.EmailVerification -> EmailVerificationPanel(
                                sound = sound,
                                onResend = onResendVerificationEmail,
                                onCheckVerified = onCheckEmailVerified,
                                onCancel = onCancelEmailVerification,
                                errorMessage = errorMessage,
                                onDismissError = onDismissError,
                                resendCooldownSeconds = resendCooldownSeconds
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MainPanel(
    sound: (Sounds) -> Unit,
    onGoogleSignIn: () -> Unit,
    onSignInEmail: () -> Unit,
    onSignUpEmail: () -> Unit,
    onGuest: () -> Unit,
    errorMessage: String?,
    onDismissError: () -> Unit
) {
    Text(
        text = stringResource(ResourceR.string.core_resources_login_welcome_back),
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center
    )
    Text(
        text = stringResource(ResourceR.string.core_resources_login_google_prompt),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(4.dp))
    Button(
        onClick = { sound(Sounds.CLK_ARCADE); onGoogleSignIn() },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(stringResource(ResourceR.string.core_resources_login_continue_google))
    }
    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
    OutlinedButton(
        onClick = { sound(Sounds.CLK_CASUAL); onSignInEmail() },
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(Icons.Outlined.Email, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(stringResource(ResourceR.string.core_resources_login_sign_in_email))
    }
    OutlinedButton(
        onClick = { sound(Sounds.CLK_CASUAL); onSignUpEmail() },
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(Icons.Outlined.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(stringResource(ResourceR.string.core_resources_login_sign_up_email))
    }
    TextButton(
        onClick = { sound(Sounds.CLK_TAP); onGuest() },
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(Icons.Outlined.Person, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(stringResource(ResourceR.string.core_resources_login_continue_guest))
    }
    ErrorSection(errorMessage = errorMessage, sound = sound, onDismissError = onDismissError)
}

@Composable
private fun EmailLoginPanel(
    sound: (Sounds) -> Unit,
    onSignIn: (String, String) -> Unit,
    onForgotPassword: () -> Unit,
    onBack: () -> Unit,
    errorMessage: String?,
    onDismissError: () -> Unit
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var showPassword by rememberSaveable { mutableStateOf(false) }

    Text(
        text = stringResource(ResourceR.string.core_resources_login_sign_in_email),
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onSurface
    )
    Spacer(Modifier.height(4.dp))
    OutlinedTextField(
        value = email,
        onValueChange = { email = it },
        label = { Text(stringResource(ResourceR.string.core_resources_login_email_hint)) },
        leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
        modifier = Modifier.fillMaxWidth()
    )
    OutlinedTextField(
        value = password,
        onValueChange = { password = it },
        label = { Text(stringResource(ResourceR.string.core_resources_login_password_hint)) },
        singleLine = true,
        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
        trailingIcon = {
            IconButton(onClick = { showPassword = !showPassword }) {
                Icon(
                    imageVector = if (showPassword) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                    contentDescription = null
                )
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
    Button(
        onClick = { sound(Sounds.CLK_ARCADE); onSignIn(email, password) },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(stringResource(ResourceR.string.core_resources_login_sign_in))
    }
    TextButton(onClick = { sound(Sounds.CLK_TAP); onForgotPassword() }) {
        Text(stringResource(ResourceR.string.core_resources_login_forgot_password))
    }
    OutlinedButton(
        onClick = { sound(Sounds.CLK_TAP); onBack() },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(stringResource(ResourceR.string.core_resources_login_back_to_login))
    }
    ErrorSection(errorMessage = errorMessage, sound = sound, onDismissError = onDismissError)
}

@Composable
private fun EmailRegisterPanel(
    sound: (Sounds) -> Unit,
    onSignUp: (String, String) -> Unit,
    onBack: () -> Unit,
    errorMessage: String?,
    onDismissError: () -> Unit
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var showPassword by rememberSaveable { mutableStateOf(false) }

    Text(
        text = stringResource(ResourceR.string.core_resources_login_sign_up_email),
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onSurface
    )
    Spacer(Modifier.height(4.dp))
    OutlinedTextField(
        value = email,
        onValueChange = { email = it },
        label = { Text(stringResource(ResourceR.string.core_resources_login_email_hint)) },
        leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
        modifier = Modifier.fillMaxWidth()
    )
    OutlinedTextField(
        value = password,
        onValueChange = { password = it },
        label = { Text(stringResource(ResourceR.string.core_resources_login_password_hint)) },
        singleLine = true,
        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
        trailingIcon = {
            IconButton(onClick = { showPassword = !showPassword }) {
                Icon(
                    imageVector = if (showPassword) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                    contentDescription = null
                )
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
    Button(
        onClick = { sound(Sounds.CLK_ARCADE); onSignUp(email, password) },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(stringResource(ResourceR.string.core_resources_login_register))
    }
    OutlinedButton(
        onClick = { sound(Sounds.CLK_TAP); onBack() },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(stringResource(ResourceR.string.core_resources_login_back_to_login))
    }
    ErrorSection(errorMessage = errorMessage, sound = sound, onDismissError = onDismissError)
}

@Composable
private fun ForgotPasswordPanel(
    sound: (Sounds) -> Unit,
    resetEmailSent: Boolean,
    onSendReset: (String) -> Unit,
    onBack: () -> Unit,
    errorMessage: String?,
    onDismissError: () -> Unit
) {
    var email by rememberSaveable { mutableStateOf("") }

    Text(
        text = stringResource(ResourceR.string.core_resources_login_forgot_password),
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onSurface
    )
    Spacer(Modifier.height(4.dp))
    if (resetEmailSent) {
        Text(
            text = stringResource(ResourceR.string.core_resources_login_reset_sent),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
    } else {
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(stringResource(ResourceR.string.core_resources_login_email_hint)) },
            leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = { sound(Sounds.CLK_ARCADE); onSendReset(email) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(ResourceR.string.core_resources_login_send_reset))
        }
    }
    OutlinedButton(
        onClick = { sound(Sounds.CLK_TAP); onBack() },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(stringResource(ResourceR.string.core_resources_login_back_to_login))
    }
    ErrorSection(errorMessage = errorMessage, sound = sound, onDismissError = onDismissError)
}

@Composable
private fun EmailVerificationPanel(
    sound: (Sounds) -> Unit,
    onResend: () -> Unit,
    onCheckVerified: () -> Unit,
    onCancel: () -> Unit,
    errorMessage: String?,
    onDismissError: () -> Unit,
    resendCooldownSeconds: Int = 0,
) {
    val canResend = resendCooldownSeconds == 0

    Text(
        text = stringResource(ResourceR.string.core_resources_login_verify_title),
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onSurface
    )
    Text(
        text = stringResource(ResourceR.string.core_resources_login_verify_body),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(4.dp))
    Button(
        onClick = { sound(Sounds.CLK_ARCADE); onCheckVerified() },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(stringResource(ResourceR.string.core_resources_login_check_verified))
    }
    OutlinedButton(
        onClick = { if (canResend) { sound(Sounds.CLK_CASUAL); onResend() } },
        enabled = canResend,
        modifier = Modifier.fillMaxWidth()
    ) {
        if (canResend) {
            Text(stringResource(ResourceR.string.core_resources_login_resend_verification))
        } else {
            Text(stringResource(ResourceR.string.core_resources_login_resend_verification) + " ($resendCooldownSeconds s)")
        }
    }
    TextButton(
        onClick = { sound(Sounds.CLK_TAP); onCancel() },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = stringResource(ResourceR.string.core_resources_login_cancel_verification),
            color = MaterialTheme.colorScheme.error
        )
    }
    ErrorSection(errorMessage = errorMessage, sound = sound, onDismissError = onDismissError)
}

@Composable
private fun ErrorSection(errorMessage: String?, sound: (Sounds) -> Unit, onDismissError: () -> Unit) {
    if (!errorMessage.isNullOrBlank()) {
        Text(
            text = errorMessage,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        OutlinedButton(
            onClick = { sound(Sounds.CLK_TAP); onDismissError() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(ResourceR.string.core_resources_dismiss))
        }
    }
}

@UiModePreviews
@Composable
private fun Preview() {
    AppTheme {
        LoginDesign()
    }
}
