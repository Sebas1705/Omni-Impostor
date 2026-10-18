package es.sebas1705.common.extensions.primitives

import es.sebas1705.common.utlis.extensions.primitives.percentageFormat
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.Locale

class FloatExtensionsTest {

    private val originalLocale = Locale.getDefault()

    // The formats use the device locale (comma in Spanish); the expectations are written for a dot.
    @Before fun useUsLocale() = Locale.setDefault(Locale.US)

    @After fun restoreLocale() = Locale.setDefault(originalLocale)

    @Test
    fun `percentageFormat formats positive float correctly`() {
        val value = 0.1234f
        val formatted = value.percentageFormat()
        assertEquals("12.34%", formatted)
    }

    @Test
    fun `percentageFormat formats negative float correctly`() {
        val value = -0.5678f
        val formatted = value.percentageFormat()
        assertEquals("-56.78%", formatted)
    }

    @Test
    fun `percentageFormat formats zero correctly`() {
        val value = 0.0f
        val formatted = value.percentageFormat()
        assertEquals("0.00%", formatted)
    }

    @Test
    fun `percentageFormat handles large float values`() {
        val value = 12345.6789f
        val formatted = value.percentageFormat()
        assertEquals("1234567.88%", formatted)
    }

    @Test
    fun `percentageFormat handles small float values`() {
        val value = 0.00001234f
        val formatted = value.percentageFormat()
        assertEquals("0.00%", formatted)
    }
}