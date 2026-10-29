package es.sebas1705.analytics

import android.os.Bundle
import es.sebas1705.analytics.config.EventLog
import es.sebas1705.common.utlis.extensions.types.logI
import es.sebas1705.repositories.interfaces.IAnalyticsRepository
import javax.inject.Inject

/**
 * Use case to log an event
 *
 * @param analyticsRepository [analyticsRepository]: repository to log the event
 *
 * @see analyticsRepository
 *
 * @since 0.1.0
 * @author Sebas1705 01/03/2025
 */
class LogEventUseCase @Inject constructor(
    private val analyticsRepository: IAnalyticsRepository
) {
    operator fun invoke(event: EventLog, bundle: Bundle) {
        logI("Event: ${event.tag}, bundleKeys: ${bundle.size()}")
        analyticsRepository.logEvent(event, bundle)
    }
}