package com.synex.core.ui

import java.io.IOException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class FormattersTest {
    @Test
    fun formatsIsoCurrency() {
        assertEquals("$1,234.50", formatMoney(1234.5, "USD"))
    }

    @Test
    fun formatsCryptoWithoutCurrencyException() {
        assertEquals("0.00123456 BTC", formatMoney(0.00123456, "BTC"))
    }

    @Test
    fun customerErrorsDoNotExposeRawMessages() {
        val result = IllegalStateException("sensitive provider detail")
            .customerMessage("load your portfolio")

        assertFalse(result.contains("sensitive"))
        assertEquals("We couldn't load your portfolio. Please try again.", result)
    }

    @Test
    fun connectionErrorsAreActionable() {
        assertEquals(
            "Check your connection and try again.",
            IOException("socket detail").customerMessage("load data"),
        )
    }
}
