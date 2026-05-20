package es.sebas1705.authentication.datasources

import androidx.credentials.CredentialManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import es.sebas1705.analytics.datasources.LogEventDataSource
import es.sebas1705.authentication.config.SettingsAuth
import es.sebas1705.common.managers.ClassLogData
import es.sebas1705.common.managers.TaskFlowManager
import es.sebas1705.common.responses.ResponseState
import es.sebas1705.common.responses.ErrorResponseType
import es.sebas1705.common.utlis.alias.FlowResponseNothing
import es.sebas1705.common.utlis.extensions.types.logI
import javax.inject.Inject

/**
 * Authentication data source for user authentication operations.
 *
 * @property credentialManager [CredentialManager]: credential manager to get the google credential
 * @property firebaseAuth [FirebaseAuth]: firebase authentication instance
 * @property logEventDataSource [LogEventDataSource]: data source for logging events
 *
 * @since 0.1.0
 * @author Sebas1705 01/03/2025
 */
class UserAuthDataSource @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val logEventDataSource: LogEventDataSource
) : ClassLogData() {

    //Properties:
    private var credentialManager: CredentialManager? = null

    //Managers:
    private val taskFlowManager = TaskFlowManager(
        this,
        logEventDataSource::logError,
        SettingsAuth.ERROR_GENERIC_MESSAGE_FAIL,
        SettingsAuth.ERROR_GENERIC_MESSAGE_EX
    )

    //Tasks:

    fun sendForgotPassword(
        email: String
    ): FlowResponseNothing = taskFlowManager.taskFlowProducer(
        taskAction = { firebaseAuth.sendPasswordResetEmail(email) },
        onSuccessListener = { ResponseState.EmptySuccess }
    )

    //Functions:
    fun signOut(): Boolean {
        logI("auth.signOut start hasCurrentUser=${firebaseAuth.currentUser != null}")
        firebaseAuth.signOut()
        val isSignedOut = firebaseAuth.currentUser == null
        logI("auth.signOut done success=$isSignedOut")
        return isSignedOut
    }

    fun isUserLogged(): Boolean {
        val currentUser = firebaseAuth.currentUser
        logI(
            "auth.session check currentUser=${if (currentUser == null) "null" else maskUid(currentUser.uid)}"
        )
        return currentUser != null
    }

    fun getCurrentUser(): FirebaseUser? = firebaseAuth.currentUser

    fun isAnonymous(): Boolean = firebaseAuth.currentUser?.isAnonymous ?: false

    fun isEmailVerified(): Boolean = firebaseAuth.currentUser?.isEmailVerified ?: false

    fun signInAnonymously(): FlowResponseNothing = taskFlowManager.taskFlowProducer(
        taskAction = { firebaseAuth.signInAnonymously() },
        onSuccessListener = {
            if (it.user != null) ResponseState.EmptySuccess
            else taskFlowManager.createResponse(ErrorResponseType.INTERNAL, SettingsAuth.NOT_LOGGED_USER)
        }
    )

    fun sendVerificationEmail(): FlowResponseNothing = taskFlowManager.taskFlowProducer(
        taskAction = {
            firebaseAuth.currentUser?.sendEmailVerification()
                ?: throw Exception(SettingsAuth.NOT_LOGGED_USER)
        },
        onSuccessListener = { ResponseState.EmptySuccess }
    )

    fun deleteCurrentUser(): FlowResponseNothing = taskFlowManager.taskFlowProducer(
        taskAction = {
            firebaseAuth.currentUser?.delete()
                ?: throw Exception(SettingsAuth.NOT_LOGGED_USER)
        },
        onSuccessListener = { ResponseState.EmptySuccess }
    )

    private fun maskUid(uid: String): String =
        if (uid.length <= 6) uid else "${uid.take(3)}...${uid.takeLast(3)}"
}