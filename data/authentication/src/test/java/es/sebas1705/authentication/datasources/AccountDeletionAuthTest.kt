package es.sebas1705.authentication.datasources

import android.app.Activity
import android.content.Context
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserInfo
import es.sebas1705.analytics.datasources.LogEventDataSource
import es.sebas1705.authentication.config.AccountProvider
import es.sebas1705.authentication.helpers.ImmediateFailureTask
import es.sebas1705.authentication.helpers.ImmediateSuccessTask
import es.sebas1705.common.responses.ErrorResponseType
import es.sebas1705.common.responses.ResponseState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class AccountDeletionAuthTest {

    private val firebaseAuth: FirebaseAuth = mock(FirebaseAuth::class.java)
    private val logEventDataSource: LogEventDataSource = mock(LogEventDataSource::class.java)
    private val user: FirebaseUser = mock(FirebaseUser::class.java)

    private val userSource = UserAuthDataSource(firebaseAuth, logEventDataSource)
    private val emailSource = EmailAuthDataSource(firebaseAuth, logEventDataSource)
    private val googleSource = GoogleAuthDataSource(
        firebaseAuth = firebaseAuth,
        logEventDataSource = logEventDataSource,
        context = mock(Context::class.java)
    )

    private fun <T> kotlinx.coroutines.flow.Flow<ResponseState<T>>.result() =
        runBlocking { first { it !is ResponseState.Loading } }

    private fun signedIn() {
        `when`(firebaseAuth.currentUser).thenReturn(user)
    }

    // region email verification

    @Test
    fun `refreshEmailVerified reloads before reading the flag`() {
        signedIn()
        var reloaded = false
        `when`(user.isEmailVerified).thenAnswer { reloaded }
        doAnswer {
            reloaded = true
            ImmediateSuccessTask<Void>(null)
        }.`when`(user).reload()

        val emission = userSource.refreshEmailVerified().result()

        assertEquals(ResponseState.Success(true), emission)
        verify(user).reload()
    }

    @Test
    fun `refreshEmailVerified emits false while the server still says unverified`() {
        signedIn()
        `when`(user.reload()).thenReturn(ImmediateSuccessTask<Void>(null))
        `when`(user.isEmailVerified).thenReturn(false)

        assertEquals(ResponseState.Success(false), userSource.refreshEmailVerified().result())
    }

    @Test
    fun `refreshEmailVerified fails instead of claiming unverified when reload fails`() {
        signedIn()
        `when`(user.reload()).thenReturn(ImmediateFailureTask<Void>(Exception("network")))

        assertTrue(userSource.refreshEmailVerified().result() is ResponseState.Error)
    }

    @Test
    fun `refreshEmailVerified fails without a session`() {
        `when`(firebaseAuth.currentUser).thenReturn(null)

        assertTrue(userSource.refreshEmailVerified().result() is ResponseState.Error)
    }

    // endregion

    // region deletion

    @Test
    fun `deleteCurrentUser succeeds`() {
        signedIn()
        `when`(user.delete()).thenReturn(ImmediateSuccessTask<Void>(null))

        assertTrue(userSource.deleteCurrentUser().result() is ResponseState.EmptySuccess)
    }

    @Test
    fun `deleteCurrentUser flags a recent login requirement as unauthorized`() {
        signedIn()
        `when`(user.delete()).thenReturn(
            ImmediateFailureTask<Void>(FirebaseAuthRecentLoginRequiredException("code", "too old"))
        )

        val emission = userSource.deleteCurrentUser().result() as ResponseState.Error

        assertEquals(ErrorResponseType.UNAUTHORIZED, emission.type)
    }

    @Test
    fun `deleteCurrentUser keeps other failures as bad requests`() {
        signedIn()
        `when`(user.delete()).thenReturn(ImmediateFailureTask<Void>(Exception("network")))

        val emission = userSource.deleteCurrentUser().result() as ResponseState.Error

        assertEquals(ErrorResponseType.BAD_REQUEST, emission.type)
    }

    // endregion

    // region account provider

    private fun providers(vararg ids: String) {
        signedIn()
        val infos = ids.map { id -> mock(UserInfo::class.java).also { `when`(it.providerId).thenReturn(id) } }
        `when`(user.providerData).thenReturn(infos)
    }

    @Test
    fun `password provider`() {
        providers("firebase", "password")
        assertEquals(AccountProvider.Password, userSource.getAccountProvider())
    }

    @Test
    fun `google provider`() {
        providers("firebase", "google.com")
        assertEquals(AccountProvider.Google, userSource.getAccountProvider())
    }

    @Test
    fun `linked accounts can use the password`() {
        providers("firebase", "google.com", "password")
        assertEquals(AccountProvider.Password, userSource.getAccountProvider())
    }

    @Test
    fun `anonymous users have nothing to confirm`() {
        providers("firebase")
        assertEquals(AccountProvider.Other, userSource.getAccountProvider())
    }

    @Test
    fun `no session has nothing to confirm`() {
        `when`(firebaseAuth.currentUser).thenReturn(null)
        assertEquals(AccountProvider.Other, userSource.getAccountProvider())
    }

    // endregion

    // region reauthentication

    private fun passwordUser() {
        signedIn()
        `when`(user.email).thenReturn("user@example.com")
    }

    @Test
    fun `reauthenticate confirms the password`() {
        passwordUser()
        `when`(user.reauthenticate(any(AuthCredential::class.java)))
            .thenReturn(ImmediateSuccessTask<Void>(null))

        assertTrue(emailSource.reauthenticate("secret1").result() is ResponseState.EmptySuccess)
    }

    @Test
    fun `a wrong password is forbidden`() {
        passwordUser()
        `when`(user.reauthenticate(any(AuthCredential::class.java))).thenReturn(
            ImmediateFailureTask<Void>(FirebaseAuthInvalidCredentialsException("code", "wrong"))
        )

        val emission = emailSource.reauthenticate("nope").result() as ResponseState.Error

        assertEquals(ErrorResponseType.FORBIDDEN, emission.type)
    }

    @Test
    fun `a mismatched user is forbidden`() {
        passwordUser()
        `when`(user.reauthenticate(any(AuthCredential::class.java))).thenReturn(
            ImmediateFailureTask<Void>(FirebaseAuthInvalidUserException("code", "mismatch"))
        )

        val emission = emailSource.reauthenticate("secret1").result() as ResponseState.Error

        assertEquals(ErrorResponseType.FORBIDDEN, emission.type)
    }

    @Test
    fun `a network failure while reauthenticating is not a wrong password`() {
        passwordUser()
        `when`(user.reauthenticate(any(AuthCredential::class.java)))
            .thenReturn(ImmediateFailureTask<Void>(Exception("network")))

        val emission = emailSource.reauthenticate("secret1").result() as ResponseState.Error

        assertEquals(ErrorResponseType.BAD_REQUEST, emission.type)
    }

    @Test
    fun `reauthenticate fails without a session`() {
        `when`(firebaseAuth.currentUser).thenReturn(null)

        assertTrue(emailSource.reauthenticate("secret1").result() is ResponseState.Error)
    }

    @Test
    fun `Google reauthentication without credentials never reaches Firebase`() {
        signedIn()

        val emission = runBlocking {
            googleSource.reauthenticateWithGoogle(Robolectric.buildActivity(Activity::class.java).get()).first {
                it !is ResponseState.Loading
            }
        }

        assertTrue(emission is ResponseState.Error)
        verify(user, never()).reauthenticateAndRetrieveData(any(AuthCredential::class.java))
    }

    // endregion
}
