package es.sebas1705.common.extensions.types

import android.util.Log
import es.sebas1705.common.utlis.extensions.types.logD
import es.sebas1705.common.utlis.extensions.types.logE
import es.sebas1705.common.utlis.extensions.types.logI
import es.sebas1705.common.utlis.extensions.types.logV
import es.sebas1705.common.utlis.extensions.types.logW
import es.sebas1705.common.utlis.extensions.types.logWTF
import org.junit.Test
import org.mockito.Mockito.mockStatic

/**
 * Logging is gated to the dev variant (process name contains ".dev"), which a JVM unit test is not,
 * so every level must stay silent here. The messages are also decorated, so the raw text is never logged as is.
 */
class AnyExtensionsTest {

    private fun assertSilent(log: Any.() -> Unit) {
        mockStatic(Log::class.java).use { logMock ->
            Any().log()
            logMock.verifyNoInteractions()
        }
    }

    @Test fun `logI does not log outside the dev variant`() = assertSilent { logI("Info message") }

    @Test fun `logE does not log outside the dev variant`() = assertSilent { logE("Error message") }

    @Test fun `logD does not log outside the dev variant`() = assertSilent { logD("Debug message") }

    @Test fun `logV does not log outside the dev variant`() = assertSilent { logV("Verbose message") }

    @Test fun `logW does not log outside the dev variant`() = assertSilent { logW("Warn message") }

    @Test fun `logWTF does not log outside the dev variant`() = assertSilent { logWTF("WTF message") }
}
