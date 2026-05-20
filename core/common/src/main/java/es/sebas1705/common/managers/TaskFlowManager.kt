package es.sebas1705.common.managers

import com.google.android.gms.tasks.Task
import es.sebas1705.common.responses.ErrorResponseType
import es.sebas1705.common.responses.ResponseState
import es.sebas1705.common.utlis.alias.FlowResponse
import es.sebas1705.common.utlis.extensions.types.logE
import es.sebas1705.common.utlis.extensions.types.logI
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.callbackFlow

/**
 * Class that will manage the task flow
 *
 * @property classLogData [ClassLogData]: Class log data
 * @property loggerAction (ClassLogData, String) -> Unit: Logger action
 * @property genericFailMessage [String]: Generic fail message
 * @property genericExMessage [String]: Generic exception message
 *
 * @since 0.1.0
 * @author Sebas1705 01/03/2025
 */
class TaskFlowManager(
    private val classLogData: ClassLogData,
    private val loggerAction: (ClassLogData, String) -> Unit,
    private val genericFailMessage: String,
    private val genericExMessage: String
) {

    /**
     * Function that will produce a task flow
     *
     * @param assertChecker () -> String?: Function to check if the operation is valid
     * @param taskAction () -> Task<Q>: Function to get the data
     * @param onSuccessListener (Q) -> ResponseState<T>: Function to handle the onCompleteListener
     *
     * @return [FlowResponse]<[T]>: Flow with the response of the operation
     *
     * @since 0.1.0
     * @author Sebas1705 01/03/2025
     */
    fun <T, Q> taskFlowProducer(
        assertChecker: suspend () -> String? = { null },
        taskAction: suspend () -> Task<Q>,
        onSuccessListener: (Q) -> ResponseState<T>
    ): FlowResponse<T> = callbackFlow {
        logI("Starting task flow producer")
        val sender = this@callbackFlow::trySendBlocking
        val closer = this@callbackFlow::close
        try {
            sender(ResponseState.Loading)
            assertChecker()?.let {
                logE("Error in task flow producer: $it")
                sender(createResponse(ErrorResponseType.BAD_REQUEST, it))
                closer(null)
            }
                ?: taskAction()
                    .addOnSuccessListener {
                        sender(onSuccessListener(it))
                        closer(null)
                    }.addOnFailureListener {
                        sender(
                            ResponseState.Error(
                                classLogData,
                                ErrorResponseType.BAD_REQUEST,
                                it.message ?: genericFailMessage,
                                loggerAction
                            )
                        )
                        closer(null)
                    }
        } catch (e: Exception) {
            logE("Error in task flow producer: ${e.message}")
            sender(
                ResponseState.Error(
                    classLogData,
                    ErrorResponseType.INTERNAL,
                    e.message ?: genericExMessage,
                    loggerAction
                )
            )
            closer(null)
        }
        awaitClose { }
    }

    /**
     * Function that will create a response
     *
     * @param responseType [ErrorResponseType]: Error response type
     * @param message [String]: Message
     *
     * @return [ResponseState]<T>: Response state
     *
     * @since 0.1.0
     * @author Sebas1705 01/03/2025
     */
    fun createResponse(
        responseType: ErrorResponseType, message: String
    ) = ResponseState.Error(
        classLogData, responseType, message, loggerAction
    )

}