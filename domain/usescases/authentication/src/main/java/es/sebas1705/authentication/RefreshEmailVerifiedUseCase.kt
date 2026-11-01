package es.sebas1705.authentication

import es.sebas1705.common.utlis.alias.FlowResponse
import es.sebas1705.repositories.interfaces.IAuthenticationRepository
import javax.inject.Inject

/**
 * Asks the server whether the email is verified, unlike [IsEmailVerifiedUseCase] that reads the cache.
 */
class RefreshEmailVerifiedUseCase @Inject constructor(
    private val authenticationRepository: IAuthenticationRepository
) {
    operator fun invoke(): FlowResponse<Boolean> = authenticationRepository.refreshEmailVerified()
}
