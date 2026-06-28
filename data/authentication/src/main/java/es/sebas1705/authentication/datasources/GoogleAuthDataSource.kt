package es.sebas1705.authentication.datasources

import android.app.Activity
import android.content.Context
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import es.sebas1705.analytics.datasources.LogEventDataSource
import es.sebas1705.authentication.config.SettingsAuth
import es.sebas1705.authentication.config.getCredentialRequestGoogle
import es.sebas1705.common.managers.ClassLogData
import es.sebas1705.common.managers.TaskFlowManager
import es.sebas1705.common.responses.ErrorResponseType
import es.sebas1705.common.responses.ResponseState
import es.sebas1705.common.utlis.alias.FlowResponseNothing
import es.sebas1705.common.utlis.extensions.types.logE
import es.sebas1705.common.utlis.extensions.types.logI
import javax.inject.Inject

class GoogleAuthDataSource @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val logEventDataSource: LogEventDataSource,
    @param:ApplicationContext private val context: Context
) : ClassLogData() {

    private var credentialManager: CredentialManager? = null

    private val taskFlowManager = TaskFlowManager(
        this,
        logEventDataSource::logError,
        SettingsAuth.ERROR_GENERIC_MESSAGE_FAIL,
        SettingsAuth.ERROR_GENERIC_MESSAGE_EX
    )

    /**
     * Signs in with Google using the provided Activity context.
     *
     * An Activity context is required so that CredentialManager can present the
     * sign-in UI on Android 13 (API 33) and below.
     */
    suspend fun signWithGoogle(activity: Activity): FlowResponseNothing {
        logI("auth.google signIn start")
        credentialManager = CredentialManager.create(context)
        var credential: Credential? = null
        var error = ""
        try {
            credential = credentialManager!!.getCredential(
                activity,
                activity.getCredentialRequestGoogle
            ).credential
            logI("auth.google credential retrieved type=${credential::class.java.simpleName}")
        } catch (e: NoCredentialException) {
            logE("auth.google signIn failed reason=no_credentials", e)
            error = SettingsAuth.ERROR_NO_CREDENTIALS
        } catch (e: Exception) {
            logE("auth.google signIn failed reason=credential_request_error", e)
            error = e.message ?: SettingsAuth.ERROR_GENERIC_MESSAGE_FAIL
        }
        return taskFlowManager.taskFlowProducer(
            assertChecker = {
                when (credential) {
                    null -> error
                    is CustomCredential if credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                        -> null
                    else -> SettingsAuth.WRONG_CREDENTIALS
                }
            },
            taskAction = {
                logI("auth.google firebase signInWithCredential start")
                val googleIdTokenCredential =
                    GoogleIdTokenCredential.createFrom(credential!!.data)
                val authCredential =
                    GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
                firebaseAuth.signInWithCredential(authCredential)
            },
            onSuccessListener = {
                if (it.user != null) {
                    logI("auth.google signIn success uid=${maskUid(it.user!!.uid)}")
                    ResponseState.EmptySuccess
                }
                else taskFlowManager.createResponse(
                    ErrorResponseType.INTERNAL,
                    SettingsAuth.NOT_LOGGED_USER
                )
            }
        )
    }

    private fun maskUid(uid: String): String =
        if (uid.length <= 6) uid else "${uid.take(3)}...${uid.takeLast(3)}"
}
