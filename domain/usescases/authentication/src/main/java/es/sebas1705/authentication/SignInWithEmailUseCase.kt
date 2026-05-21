package es.sebas1705.authentication

import es.sebas1705.common.utlis.alias.FlowResponseNothing
import es.sebas1705.repositories.interfaces.IAuthenticationRepository
import javax.inject.Inject

class SignInWithEmailUseCase @Inject constructor(
    private val authenticationRepository: IAuthenticationRepository
) {
    operator fun invoke(email: String, password: String): FlowResponseNothing =
        authenticationRepository.signInWithEmail(email, password)
}
