package es.sebas1705.authentication.datasources

import android.app.Activity
import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import es.sebas1705.analytics.datasources.LogEventDataSource
import es.sebas1705.common.responses.ResponseState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verifyNoInteractions
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class GoogleAuthDataSourceFailureTest {

    private val firebaseAuth: FirebaseAuth = mock(FirebaseAuth::class.java)
    private val logEventDataSource: LogEventDataSource = mock(LogEventDataSource::class.java)
    private val context: Context = mock(Context::class.java)

    private val dataSource = GoogleAuthDataSource(
        firebaseAuth = firebaseAuth,
        logEventDataSource = logEventDataSource,
        context = context
    )

    @Test
    fun `signWithGoogle emits error when credential can't be obtained`() {
        runBlocking {
            val emission = dataSource.signWithGoogle(Robolectric.buildActivity(Activity::class.java).get()).first()
            assertTrue(emission !is ResponseState.EmptySuccess)
            verifyNoInteractions(firebaseAuth)
        }
    }
}
