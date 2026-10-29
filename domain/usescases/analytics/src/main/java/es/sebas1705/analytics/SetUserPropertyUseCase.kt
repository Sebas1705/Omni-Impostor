package es.sebas1705.analytics

import es.sebas1705.analytics.config.UserProperty
import es.sebas1705.common.utlis.extensions.types.logI
import es.sebas1705.repositories.interfaces.IAnalyticsRepository
import javax.inject.Inject

/**
 * Use case to set a user property
 *
 * @param analyticsRepository [analyticsRepository]: repository to set the user property
 *
 * @see analyticsRepository
 *
 * @since 0.1.0
 * @author Sebas1705 01/03/2025
 */
class SetUserPropertyUseCase @Inject constructor(
    private val analyticsRepository: IAnalyticsRepository
) {
    operator fun invoke(userProperty: UserProperty, value: String) {
        logI("UserProperty set: ${userProperty.tag}")
        analyticsRepository.setUserProperty(userProperty, value)
    }
}