package es.sebas1705.authentication.datasources

import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import es.sebas1705.analytics.datasources.LogEventDataSource
import es.sebas1705.authentication.helpers.ImmediateSuccessTask
import es.sebas1705.common.responses.ResponseState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class EmailAuthDataSourceTest {

    private val firebaseAuth: FirebaseAuth = mock(FirebaseAuth::class.java)
    private val logEventDataSource: LogEventDataSource = mock(LogEventDataSource::class.java)

    private val dataSource = EmailAuthDataSource(
        firebaseAuth = firebaseAuth,
        logEventDataSource = logEventDataSource
    )

    @Test
    fun `signInWithEmail emits EmptySuccess when auth result has user`() {
        runBlocking {
            val email = "user@example.com"
            val password = "pass123"

            val authResult = mock(AuthResult::class.java)
            val user = mock(FirebaseUser::class.java)

            `when`(authResult.user).thenReturn(user)
            `when`(firebaseAuth.signInWithEmailAndPassword(email, password))
                .thenReturn(ImmediateSuccessTask(authResult))

            val emission = dataSource.signInWithEmail(email, password).first { it !is ResponseState.Loading }

            assertTrue(emission is ResponseState.EmptySuccess)
            verify(firebaseAuth).signInWithEmailAndPassword(email, password)
        }
    }

    @Test
    fun `signInWithEmail emits error when auth result has null user`() {
        runBlocking {
            val email = "user@example.com"
            val password = "pass123"

            val authResult = mock(AuthResult::class.java)
            `when`(authResult.user).thenReturn(null)

            `when`(firebaseAuth.signInWithEmailAndPassword(email, password))
                .thenReturn(ImmediateSuccessTask(authResult))

            val emission = dataSource.signInWithEmail(email, password).first { it !is ResponseState.Loading }

            assertTrue(emission is ResponseState.Error)
            verify(firebaseAuth).signInWithEmailAndPassword(email, password)
        }
    }

    @Test
    fun `signUpWithEmail emits EmptySuccess without sending verification when auth result has user (viewmodel sends it)`() {
        runBlocking {
            val email = "user@example.com"
            val password = "pass123"

            val authResult = mock(AuthResult::class.java)
            val user = mock(FirebaseUser::class.java)

            `when`(authResult.user).thenReturn(user)
            `when`(firebaseAuth.createUserWithEmailAndPassword(email, password))
                .thenReturn(ImmediateSuccessTask(authResult))

            val emission = dataSource.signUpWithEmail(email, password).first { it !is ResponseState.Loading }

            assertTrue(emission is ResponseState.EmptySuccess)
            verify(firebaseAuth).createUserWithEmailAndPassword(email, password)
            verify(user, never()).sendEmailVerification()
        }
    }

    @Test
    fun `signUpWithEmail emits error when auth result has null user`() {
        runBlocking {
            val email = "user@example.com"
            val password = "pass123"

            val authResult = mock(AuthResult::class.java)
            `when`(authResult.user).thenReturn(null)

            `when`(firebaseAuth.createUserWithEmailAndPassword(email, password))
                .thenReturn(ImmediateSuccessTask(authResult))

            val emission = dataSource.signUpWithEmail(email, password).first { it !is ResponseState.Loading }

            assertTrue(emission is ResponseState.Error)
            verify(firebaseAuth).createUserWithEmailAndPassword(email, password)
        }
    }
}
