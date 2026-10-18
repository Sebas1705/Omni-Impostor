package es.sebas1705.authentication.config

import android.content.Context
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ExtensionsAuthTest {

    @Test
    fun `getCredentialRequestGoogle builds request`() {
        val context: Context = RuntimeEnvironment.getApplication()

        val request: GetCredentialRequest = context.getCredentialRequestGoogle

        assertNotNull(request)
        assertEquals(2, request.credentialOptions.size)
        assertTrue(request.credentialOptions[1] is GetSignInWithGoogleOption)
        assertEquals(SettingsAuth.FILTER_BY_AUTHORIZED_ACCOUNTS, (request.credentialOptions[0] as GetGoogleIdOption).filterByAuthorizedAccounts)
    }
}
