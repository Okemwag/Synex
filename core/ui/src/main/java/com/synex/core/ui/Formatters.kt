package com.synex.core.ui

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale
import kotlin.math.absoluteValue

fun formatMoney(value: Double, currencyCode: String): String {
    val code = currencyCode.trim().uppercase().ifBlank { "USD" }
    val currency = runCatching { Currency.getInstance(code) }.getOrNull()
    if (currency != null) {
        return NumberFormat.getCurrencyInstance(Locale.US).apply { this.currency = currency }.format(value)
    }
    val decimals = if (value.absoluteValue in 0.0..<1.0) 8 else 2
    val formatted = NumberFormat.getNumberInstance(Locale.US).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = decimals
    }.format(value)
    return "$formatted $code"
}
