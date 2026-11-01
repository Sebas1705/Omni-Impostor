package es.sebas1705.authentication.config

import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.GoogleAuthProvider
import es.sebas1705.common.responses.ErrorResponseType

/**
 * How the current user proves who they are when Firebase asks for a recent sign-in.
 *
 * @property Password email and password
 * @property Google Google account
 * @property Other anonymous users or no session: there is nothing to ask the user for
 */
enum class AccountProvider {
    Password,
    Google,
    Other
}

/** Resolves the provider from the user's Firebase provider ids; either works when both are linked. */
internal fun accountProviderOf(providerIds: List<String>): AccountProvider = when {
    EmailAuthProvider.PROVIDER_ID in providerIds -> AccountProvider.Password
    GoogleAuthProvider.PROVIDER_ID in providerIds -> AccountProvider.Google
    else -> AccountProvider.Other
}

/** A rejected password or a different Google account is the user's to fix, unlike any other failure. */
internal fun reauthFailureType(exception: Exception): ErrorResponseType = when (exception) {
    is FirebaseAuthInvalidCredentialsException,
    is FirebaseAuthInvalidUserException -> ErrorResponseType.FORBIDDEN
    else -> ErrorResponseType.BAD_REQUEST
}
