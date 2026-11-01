package es.sebas1705.authentication

import es.sebas1705.authentication.config.AccountProvider
import es.sebas1705.repositories.interfaces.IAuthenticationRepository
import javax.inject.Inject

class GetAccountProviderUseCase @Inject constructor(
    private val authenticationRepository: IAuthenticationRepository
) {
    operator fun invoke(): AccountProvider = authenticationRepository.getAccountProvider()
}
