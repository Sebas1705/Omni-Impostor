package es.sebas1705.common.extensions.primitives

import es.sebas1705.common.utlis.extensions.primitives.millisToFormatDate
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.TimeZone

class LongExtensionsTest {

    // millisToFormatDate uses the system zone; the expected strings below are Europe/Madrid times,
    // so pin it instead of depending on the machine (CI runners are UTC).
    private val previousZone = TimeZone.getDefault()

    @Before
    fun setUp() {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Madrid"))
    }

    @After
    fun tearDown() {
        TimeZone.setDefault(previousZone)
    }

    @Test fun `millisToFormatDate formats epoch millis correctly`() {
        val epochMillis = 0L
        val formattedDate = epochMillis.millisToFormatDate()
        assertEquals("01:00 - 01/01/1970", formattedDate)
    }

    @Test fun `millisToFormatDate formats current time correctly`() {
        val currentMillis = System.currentTimeMillis()
        val formattedDate = currentMillis.millisToFormatDate()
        val expectedDate = DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy")
            .withZone(ZoneId.systemDefault())
            .format(Instant.ofEpochMilli(currentMillis))
        assertEquals(expectedDate, formattedDate)
    }

    @Test fun `millisToFormatDate handles large epoch millis`() {
        val largeMillis = 32503680000000L // Year 3000
        val formattedDate = largeMillis.millisToFormatDate()
        assertEquals("01:00 - 01/01/3000", formattedDate)
    }

    @Test fun `millisToFormatDate handles negative epoch millis`() {
        val negativeMillis = -1L
        val formattedDate = negativeMillis.millisToFormatDate()
        assertEquals("00:59 - 01/01/1970", formattedDate)
    }
}