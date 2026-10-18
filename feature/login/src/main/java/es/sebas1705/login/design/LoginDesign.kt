package es.sebas1705.login.design

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
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
import androidx.compose.ui.graphics.vector.ImageVector
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
import es.sebas1705.login.viewmodel.LoginMode
import es.sebas1705.ui.components.AppCard
import es.sebas1705.ui.components.PrimaryActionButton
import es.sebas1705.ui.components.StatusChip
import es.sebas1705.ui.components.StatusChipVariant
import es.sebas1705.ui.sound.LocalSoundPlayer
import es.sebas1705.ui.theme.AppDimens
import es.sebas1705.ui.theme.AppTheme
import es.sebas1705.ui.theme.makeTitle
import es.sebas1705.core.resources.R as ResourceR

/** Panel width stops growing on tablets so the form stays readable. */
private val PanelMaxWidth = 480.dp
private val MainLogoSize = 160.dp
private val FormLogoSize = 88.dp
private val ButtonIconSize = 18.dp
private val MinTouchTarget = 48.dp

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
    val spacing = AppDimens.spacing

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = spacing.xl, vertical = spacing.xxl),
        verticalArrangement = Arrangement.spacedBy(spacing.xl, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val isMain = loginMode == LoginMode.Main
        Image(
            painter = painterResource(ResourceR.drawable.core_resources_ic_app_logo),
            contentDescription = stringResource(ResourceR.string.core_resources_icon_content),
            modifier = Modifier.size(if (isMain) MainLogoSize else FormLogoSize)
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
                AppCard(modifier = Modifier.widthIn(max = PanelMaxWidth).fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(spacing.sm),
                        verticalArrangement = Arrangement.spacedBy(spacing.md),
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

/** Panel title, centered, with an optional explanatory subtitle. */
@Composable
private fun PanelHeader(title: String, subtitle: String? = null) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppDimens.spacing.xs),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.makeTitle(),
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = MinTouchTarget)
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(ButtonIconSize))
            Spacer(Modifier.size(AppDimens.spacing.sm))
        }
        Text(text)
    }
}

@Composable
private fun EmailField(
    value: String,
    onValueChange: (String) -> Unit,
    imeAction: ImeAction
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(stringResource(ResourceR.string.core_resources_login_email_hint)) },
        leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = imeAction),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit
) {
    var showPassword by rememberSaveable { mutableStateOf(false) }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(stringResource(ResourceR.string.core_resources_login_password_hint)) },
        singleLine = true,
        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
        trailingIcon = {
            IconButton(onClick = { showPassword = !showPassword }) {
                Icon(
                    imageVector = if (showPassword) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                    contentDescription = stringResource(
                        if (showPassword) {
                            ResourceR.string.core_resources_phase4_hide_password
                        } else {
                            ResourceR.string.core_resources_phase4_show_password
                        }
                    )
                )
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
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
    PanelHeader(
        title = stringResource(ResourceR.string.core_resources_login_welcome_back),
        subtitle = stringResource(ResourceR.string.core_resources_login_google_prompt)
    )
    PrimaryActionButton(
        text = stringResource(ResourceR.string.core_resources_login_continue_google),
        onClick = onGoogleSignIn
    )
    HorizontalDivider(modifier = Modifier.padding(vertical = AppDimens.spacing.xs))
    SecondaryButton(
        text = stringResource(ResourceR.string.core_resources_login_sign_in_email),
        icon = Icons.Outlined.Email,
        onClick = { sound(Sounds.CLK_CASUAL); onSignInEmail() }
    )
    SecondaryButton(
        text = stringResource(ResourceR.string.core_resources_login_sign_up_email),
        icon = Icons.Outlined.PersonAdd,
        onClick = { sound(Sounds.CLK_CASUAL); onSignUpEmail() }
    )
    TextButton(
        onClick = { sound(Sounds.CLK_TAP); onGuest() },
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = MinTouchTarget)
    ) {
        Icon(Icons.Outlined.Person, contentDescription = null, modifier = Modifier.size(ButtonIconSize))
        Spacer(Modifier.size(AppDimens.spacing.sm))
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

    PanelHeader(title = stringResource(ResourceR.string.core_resources_login_sign_in_email))
    EmailField(value = email, onValueChange = { email = it }, imeAction = ImeAction.Next)
    PasswordField(value = password, onValueChange = { password = it })
    PrimaryActionButton(
        text = stringResource(ResourceR.string.core_resources_login_sign_in),
        onClick = { onSignIn(email, password) }
    )
    TextButton(
        onClick = { sound(Sounds.CLK_TAP); onForgotPassword() },
        modifier = Modifier.heightIn(min = MinTouchTarget)
    ) {
        Text(stringResource(ResourceR.string.core_resources_login_forgot_password))
    }
    SecondaryButton(
        text = stringResource(ResourceR.string.core_resources_login_back_to_login),
        onClick = { sound(Sounds.CLK_TAP); onBack() }
    )
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

    PanelHeader(title = stringResource(ResourceR.string.core_resources_login_sign_up_email))
    EmailField(value = email, onValueChange = { email = it }, imeAction = ImeAction.Next)
    PasswordField(value = password, onValueChange = { password = it })
    PrimaryActionButton(
        text = stringResource(ResourceR.string.core_resources_login_register),
        onClick = { onSignUp(email, password) }
    )
    SecondaryButton(
        text = stringResource(ResourceR.string.core_resources_login_back_to_login),
        onClick = { sound(Sounds.CLK_TAP); onBack() }
    )
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

    PanelHeader(title = stringResource(ResourceR.string.core_resources_login_forgot_password))
    if (resetEmailSent) {
        StatusChip(
            text = stringResource(ResourceR.string.core_resources_login_reset_sent),
            modifier = Modifier.fillMaxWidth(),
            variant = StatusChipVariant.Ready
        )
    } else {
        EmailField(value = email, onValueChange = { email = it }, imeAction = ImeAction.Done)
        PrimaryActionButton(
            text = stringResource(ResourceR.string.core_resources_login_send_reset),
            onClick = { onSendReset(email) }
        )
    }
    SecondaryButton(
        text = stringResource(ResourceR.string.core_resources_login_back_to_login),
        onClick = { sound(Sounds.CLK_TAP); onBack() }
    )
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

    PanelHeader(
        title = stringResource(ResourceR.string.core_resources_login_verify_title),
        subtitle = stringResource(ResourceR.string.core_resources_login_verify_body)
    )
    PrimaryActionButton(
        text = stringResource(ResourceR.string.core_resources_login_check_verified),
        onClick = onCheckVerified
    )
    SecondaryButton(
        text = if (canResend) {
            stringResource(ResourceR.string.core_resources_login_resend_verification)
        } else {
            stringResource(ResourceR.string.core_resources_login_resend_verification) + " ($resendCooldownSeconds s)"
        },
        enabled = canResend,
        onClick = { if (canResend) { sound(Sounds.CLK_CASUAL); onResend() } }
    )
    TextButton(
        onClick = { sound(Sounds.CLK_TAP); onCancel() },
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = MinTouchTarget)
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
        StatusChip(
            text = errorMessage,
            modifier = Modifier.fillMaxWidth(),
            variant = StatusChipVariant.Error,
            icon = Icons.Outlined.ErrorOutline
        )
        SecondaryButton(
            text = stringResource(ResourceR.string.core_resources_dismiss),
            onClick = { sound(Sounds.CLK_TAP); onDismissError() }
        )
    }
}

@UiModePreviews
@Composable
private fun Preview() {
    AppTheme {
        LoginDesign()
    }
}

@UiModePreviews
@Composable
private fun EmailLoginPreview() {
    AppTheme {
        LoginDesign(loginMode = LoginMode.EmailLogin, errorMessage = "Wrong email or password")
    }
}
