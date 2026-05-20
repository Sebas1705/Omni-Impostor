package es.sebas1705.repositories.repos

import es.sebas1705.authentication.datasources.EmailAuthDataSource
import es.sebas1705.authentication.datasources.GoogleAuthDataSource
import es.sebas1705.authentication.datasources.UserAuthDataSource
import es.sebas1705.common.utlis.alias.FlowResponseNothing
import es.sebas1705.datastore.datasources.SettingsPreferencesDataSource
import es.sebas1705.repositories.interfaces.IAuthenticationRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Authentication repository implementation
 *
 * @property emailAuthDataSource [EmailAuthDataSource]: Data source for email authentication
 * @property googleAuthDataSource [GoogleAuthDataSource]: Data source for Google authentication
 * @property userAuthDataSource [UserAuthDataSource]: Data source for user authentication
 *
 * @since 0.1.0
 * @author Sebas1705 01/03/2025
 */
class AuthenticationRepository @Inject constructor(
    private val emailAuthDataSource: EmailAuthDataSource,
    private val googleAuthDataSource: GoogleAuthDataSource,
    private val userAuthDataSource: UserAuthDataSource,
    private val settingsPreferencesDataSource: SettingsPreferencesDataSource,
) : IAuthenticationRepository {

    //Tasks:
    override fun signUpWithEmail(
        email: String,
        password: String,
    ): FlowResponseNothing = emailAuthDataSource.signUpWithEmail(email, password)

    override fun signInWithEmail(
        email: String,
        password: String,
    ): FlowResponseNothing = emailAuthDataSource.signInWithEmail(email, password)

    override fun sendForgotPassword(
        email: String
    ): FlowResponseNothing = userAuthDataSource.sendForgotPassword(email)

    override suspend fun signWithGoogle(): FlowResponseNothing =
        googleAuthDataSource.signWithGoogle()

    override fun signInAnonymously(): FlowResponseNothing = userAuthDataSource.signInAnonymously()

    override fun sendVerificationEmail(): FlowResponseNothing = userAuthDataSource.sendVerificationEmail()

    //Functions:
    override fun signOut(): Boolean = userAuthDataSource.signOut()
    override fun isUserLogged(): Boolean = userAuthDataSource.isUserLogged()
    override fun isEmailVerified(): Boolean = userAuthDataSource.isEmailVerified()
    override fun isAnonymous(): Boolean = userAuthDataSource.isAnonymous()

    override fun deleteCurrentUser(): FlowResponseNothing = userAuthDataSource.deleteCurrentUser()

    override suspend fun isSessionExpected(): Boolean =
        settingsPreferencesDataSource.getAuthSessionExpected().first()

    override suspend fun setSessionExpected(value: Boolean) {
        settingsPreferencesDataSource.saveAuthSessionExpected(value)
    }
}