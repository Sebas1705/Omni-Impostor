package es.sebas1705.home.profile.design

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import es.sebas1705.common.utlis.UiModePreviews
import es.sebas1705.home.profile.viewmodel.DeleteAccountStep
import es.sebas1705.ui.theme.AppDimens
import es.sebas1705.ui.theme.AppTheme
import es.sebas1705.core.resources.R as ResourceR

/** Confirmation, reauthentication and progress of the delete-account flow. */
@Composable
@Suppress("LongParameterList")
fun DeleteAccountDialog(
    step: DeleteAccountStep,
    errorMessage: String?,
    onDismiss: () -> Unit = {},
    onConfirm: () -> Unit = {},
    onPasswordConfirm: (String) -> Unit = {},
    onGoogleConfirm: () -> Unit = {}
) {
    when (step) {
        DeleteAccountStep.Hidden -> Unit
        DeleteAccountStep.InProgress -> DeleteAccountProgressDialog()
        DeleteAccountStep.Confirm -> DeleteAccountActionDialog(
            title = stringResource(ResourceR.string.core_resources_profile_delete_account_title),
            body = stringResource(ResourceR.string.core_resources_profile_delete_account_body),
            errorMessage = errorMessage,
            confirmText = stringResource(ResourceR.string.core_resources_profile_delete_account_confirm),
            onConfirm = onConfirm,
            onDismiss = onDismiss
        )
        DeleteAccountStep.ReauthGoogle -> DeleteAccountActionDialog(
            title = stringResource(ResourceR.string.core_resources_profile_delete_account_reauth_title),
            body = stringResource(ResourceR.string.core_resources_profile_delete_account_reauth_google_body),
            errorMessage = errorMessage,
            confirmText = stringResource(ResourceR.string.core_resources_profile_delete_account_reauth_google_action),
            onConfirm = onGoogleConfirm,
            onDismiss = onDismiss
        )
        DeleteAccountStep.ReauthPassword -> ReauthPasswordDialog(
            errorMessage = errorMessage,
            onConfirm = onPasswordConfirm,
            onDismiss = onDismiss
        )
    }
}

@Composable
@Suppress("LongParameterList")
private fun DeleteAccountActionDialog(
    title: String,
    body: String,
    errorMessage: String?,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmEnabled: Boolean = true,
    extraContent: @Composable () -> Unit = {}
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(AppDimens.spacing.sm)) {
                Text(body)
                extraContent()
                if (!errorMessage.isNullOrBlank()) {
                    Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = confirmEnabled,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(ResourceR.string.core_resources_profile_delete_account_cancel))
            }
        }
    )
}

@Composable
private fun ReauthPasswordDialog(
    errorMessage: String?,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var password by remember { mutableStateOf("") }
    DeleteAccountActionDialog(
        title = stringResource(ResourceR.string.core_resources_profile_delete_account_reauth_title),
        body = stringResource(ResourceR.string.core_resources_profile_delete_account_reauth_password_body),
        errorMessage = errorMessage,
        confirmText = stringResource(ResourceR.string.core_resources_profile_delete_account_confirm),
        onConfirm = { onConfirm(password) },
        onDismiss = onDismiss,
        confirmEnabled = password.isNotEmpty()
    ) {
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(stringResource(ResourceR.string.core_resources_profile_delete_account_password_hint)) },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun DeleteAccountProgressDialog() {
    AlertDialog(
        onDismissRequest = {},
        confirmButton = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
        text = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppDimens.spacing.md)
            ) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                Text(stringResource(ResourceR.string.core_resources_profile_delete_account_in_progress))
            }
        }
    )
}

@UiModePreviews
@Composable
private fun ConfirmPreview() {
    AppTheme {
        DeleteAccountDialog(step = DeleteAccountStep.Confirm, errorMessage = null)
    }
}

@UiModePreviews
@Composable
private fun ReauthPasswordPreview() {
    AppTheme {
        DeleteAccountDialog(step = DeleteAccountStep.ReauthPassword, errorMessage = "Wrong password")
    }
}
