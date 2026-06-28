package es.sebas1705.repositories.interfaces

import android.app.Activity
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

    fun isEmailVerified(): Boolean

    fun isAnonymous(): Boolean

    fun deleteCurrentUser(): FlowResponseNothing

    suspend fun isSessionExpected(): Boolean

    suspend fun setSessionExpected(value: Boolean)
}
