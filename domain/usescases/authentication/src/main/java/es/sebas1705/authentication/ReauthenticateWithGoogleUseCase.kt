package es.sebas1705.authentication

import android.app.Activity
import es.sebas1705.common.utlis.alias.FlowResponseNothing
import es.sebas1705.repositories.interfaces.IAuthenticationRepository
import javax.inject.Inject

class ReauthenticateWithGoogleUseCase @Inject constructor(
    private val authenticationRepository: IAuthenticationRepository
) {
    suspend operator fun invoke(activity: Activity): FlowResponseNothing =
        authenticationRepository.reauthenticateWithGoogle(activity)
}
