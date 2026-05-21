package es.sebas1705.authentication

import es.sebas1705.common.utlis.alias.FlowResponseNothing
import es.sebas1705.repositories.interfaces.IAuthenticationRepository
import javax.inject.Inject

class SendVerificationEmailUseCase @Inject constructor(
    private val authenticationRepository: IAuthenticationRepository
) {
    operator fun invoke(): FlowResponseNothing = authenticationRepository.sendVerificationEmail()
}
