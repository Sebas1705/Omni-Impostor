package es.sebas1705.services

/*
/**
 * Service to handle the Firebase Messaging Service
 *
 * @since 0.1.0
 * @author Sebas1705 01/03/2025
 */
class MyFirebaseMessagingService @Inject constructor() : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        sendRegistrationToken(token)
    }

    /**
     * Send registration token
     *
     * @param token [String]: token to send
     *
     * @since 0.1.0
     * @author Sebas1705 01/03/2025
     */
    private fun sendRegistrationToken(token: String) {
        // Never log the FCM registration token: it identifies the device/user.
    }

}
*/
