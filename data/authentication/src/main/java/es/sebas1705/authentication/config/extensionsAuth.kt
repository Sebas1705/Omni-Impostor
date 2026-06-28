package es.sebas1705.authentication.config

import android.content.Context
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption

private const val DEFAULT_WEB_CLIENT_ID_RES = "default_web_client_id"

private fun Context.resolveServerClientId(): String {
    val resourceId = resources.getIdentifier(
        DEFAULT_WEB_CLIENT_ID_RES,
        "string",
        packageName
    )
    return if (resourceId != 0) getString(resourceId)
    else SettingsAuth.FALLBACK_SERVER_CLIENT_ID
}

val Context.getCredentialRequestGoogle: GetCredentialRequest
    get() {
        val clientId = resolveServerClientId()
        return GetCredentialRequest.Builder()
            // One Tap: fast path if the device already has an authorized account
            .addCredentialOption(
                GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(SettingsAuth.FILTER_BY_AUTHORIZED_ACCOUNTS)
                    .setServerClientId(clientId)
                    .build()
            )
            // Full sign-in sheet: shown when One Tap has no eligible accounts
            .addCredentialOption(
                GetSignInWithGoogleOption.Builder(clientId).build()
            )
            .build()
    }
