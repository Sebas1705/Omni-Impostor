package es.sebas1705.authentication

import es.sebas1705.common.utlis.alias.FlowResponseNothing
import es.sebas1705.repositories.interfaces.IAuthenticationRepository
import javax.inject.Inject

/**
 * Permanently deletes the signed-in account from Firebase Authentication.
 *
 * That is everything the account owns: Firestore only holds anonymous `clue_ratings` counters with no
 * user id, which the rules do not let clients delete. If a per-user collection is ever added, its
 * documents must be deleted here, before the account, while the user can still pass the rules.
 */
class DeleteCurrentUserUseCase @Inject constructor(
    private val authenticationRepository: IAuthenticationRepository
) {
    operator fun invoke(): FlowResponseNothing = authenticationRepository.deleteCurrentUser()
}
