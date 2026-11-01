package es.sebas1705.repositories.repos

import android.app.Activity
import es.sebas1705.authentication.config.AccountProvider
import es.sebas1705.authentication.datasources.EmailAuthDataSource
import es.sebas1705.authentication.datasources.GoogleAuthDataSource
import es.sebas1705.authentication.datasources.UserAuthDataSource
import es.sebas1705.common.utlis.alias.FlowResponse
import es.sebas1705.common.utlis.alias.FlowResponseNothing
import es.sebas1705.datastore.datasources.SettingsPreferencesDataSource
import es.sebas1705.repositories.interfaces.IAuthenticationRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class AuthenticationRepository @Inject constructor(
    private val emailAuthDataSource: EmailAuthDataSource,
    private val googleAuthDataSource: GoogleAuthDataSource,
    private val userAuthDataSource: UserAuthDataSource,
    private val settingsPreferencesDataSource: SettingsPreferencesDataSource,
) : IAuthenticationRepository {

    override fun signUpWithEmail(email: String, password: String): FlowResponseNothing =
        emailAuthDataSource.signUpWithEmail(email, password)

    override fun signInWithEmail(email: String, password: String): FlowResponseNothing =
        emailAuthDataSource.signInWithEmail(email, password)

    override fun sendForgotPassword(email: String): FlowResponseNothing =
        userAuthDataSource.sendForgotPassword(email)

    override suspend fun signWithGoogle(activity: Activity): FlowResponseNothing =
        googleAuthDataSource.signWithGoogle(activity)

    override fun signInAnonymously(): FlowResponseNothing = userAuthDataSource.signInAnonymously()

    override fun sendVerificationEmail(): FlowResponseNothing = userAuthDataSource.sendVerificationEmail()

    override fun signOut(): Boolean = userAuthDataSource.signOut()
    override fun isUserLogged(): Boolean = userAuthDataSource.isUserLogged()
    override fun isEmailVerified(): Boolean = userAuthDataSource.isEmailVerified()
    override fun refreshEmailVerified(): FlowResponse<Boolean> = userAuthDataSource.refreshEmailVerified()
    override fun isAnonymous(): Boolean = userAuthDataSource.isAnonymous()
    override fun getAccountProvider(): AccountProvider = userAuthDataSource.getAccountProvider()

    override fun reauthenticateWithPassword(password: String): FlowResponseNothing =
        emailAuthDataSource.reauthenticate(password)

    override suspend fun reauthenticateWithGoogle(activity: Activity): FlowResponseNothing =
        googleAuthDataSource.reauthenticateWithGoogle(activity)

    override fun deleteCurrentUser(): FlowResponseNothing = userAuthDataSource.deleteCurrentUser()

    override suspend fun isSessionExpected(): Boolean =
        settingsPreferencesDataSource.getAuthSessionExpected().first()

    override suspend fun setSessionExpected(value: Boolean) {
        settingsPreferencesDataSource.saveAuthSessionExpected(value)
    }
}
