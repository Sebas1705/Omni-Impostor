package es.sebas1705.authentication

import es.sebas1705.repositories.interfaces.IAuthenticationRepository
import javax.inject.Inject

class IsEmailVerifiedUseCase @Inject constructor(
    private val authenticationRepository: IAuthenticationRepository
) {
    operator fun invoke(): Boolean = authenticationRepository.isEmailVerified()
}
