package es.sebas1705.authentication.datasources

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import es.sebas1705.analytics.datasources.LogEventDataSource
import es.sebas1705.authentication.helpers.ImmediateSuccessTask
import es.sebas1705.common.responses.ResponseState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class UserAuthDataSourceTest {

    private val firebaseAuth: FirebaseAuth = mock(FirebaseAuth::class.java)
    private val logEventDataSource: LogEventDataSource = mock(LogEventDataSource::class.java)

    private val dataSource = UserAuthDataSource(
        firebaseAuth = firebaseAuth,
        logEventDataSource = logEventDataSource
    )

    @Test
    fun `signOut returns true when currentUser is null`() {
        `when`(firebaseAuth.currentUser).thenReturn(null)

        val result = dataSource.signOut()

        assertTrue(result)
        verify(firebaseAuth).signOut()
    }

    @Test
    fun `signOut returns false when currentUser is not null`() {
        val user = mock(FirebaseUser::class.java)
        `when`(firebaseAuth.currentUser).thenReturn(user)

        val result = dataSource.signOut()

        assertFalse(result)
        verify(firebaseAuth).signOut()
    }

    @Test
    fun `isUserLogged returns true when currentUser not null`() {
        val user = mock(FirebaseUser::class.java)
        `when`(user.uid).thenReturn("uid-123456")
        `when`(firebaseAuth.currentUser).thenReturn(user)

        assertTrue(dataSource.isUserLogged())
    }

    @Test
    fun `getCurrentUser returns firebase currentUser`() {
        val user = mock(FirebaseUser::class.java)
        `when`(firebaseAuth.currentUser).thenReturn(user)

        assertSame(user, dataSource.getCurrentUser())
    }

    @Test
    fun `getCurrentUser returns null when firebase currentUser is null`() {
        `when`(firebaseAuth.currentUser).thenReturn(null)

        assertNull(dataSource.getCurrentUser())
    }

    @Test
    fun `sendForgotPassword emits EmptySuccess when task succeeds`() {
        runBlocking {
            val email = "user@example.com"

            `when`(firebaseAuth.sendPasswordResetEmail(email))
                .thenReturn(ImmediateSuccessTask(null))

            val emission = dataSource.sendForgotPassword(email).first { it !is ResponseState.Loading }

            assertTrue(emission is ResponseState.EmptySuccess)
            verify(firebaseAuth).sendPasswordResetEmail(email)
        }
    }
}
