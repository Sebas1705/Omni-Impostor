package es.sebas1705.repositories.interfaces

import android.app.Activity
import es.sebas1705.authentication.config.AccountProvider
import es.sebas1705.common.utlis.alias.FlowResponse
import es.sebas1705.common.utlis.alias.FlowResponseNothing

interface IAuthenticationRepository {

    fun signUpWithEmail(email: String, password: String): FlowResponseNothing

    fun signInWithEmail(email: String, password: String): FlowResponseNothing

    /**
     * Sign in with Google.
     * @param activity The calling Activity; required by CredentialManager on Android <=13.
     */
    suspend fun signWithGoogle(activity: Activity): FlowResponseNothing

    fun sendForgotPassword(email: String): FlowResponseNothing

    fun signOut(): Boolean

    fun isUserLogged(): Boolean

    fun signInAnonymously(): FlowResponseNothing

    fun sendVerificationEmail(): FlowResponseNothing

    /** Cached value: see [refreshEmailVerified] to know the current state. */
    fun isEmailVerified(): Boolean

    /** Reloads the user from the server and emits whether the email is verified now. */
    fun refreshEmailVerified(): FlowResponse<Boolean>

    fun isAnonymous(): Boolean

    fun getAccountProvider(): AccountProvider

    fun reauthenticateWithPassword(password: String): FlowResponseNothing

    /**
     * @param activity The calling Activity; required by CredentialManager on Android <=13.
     */
    suspend fun reauthenticateWithGoogle(activity: Activity): FlowResponseNothing

    fun deleteCurrentUser(): FlowResponseNothing

    suspend fun isSessionExpected(): Boolean

    suspend fun setSessionExpected(value: Boolean)
}
