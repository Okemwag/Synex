package com.synex.core.ui

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

fun formatMoney(value: Double, currencyCode: String): String = NumberFormat
    .getCurrencyInstance(Locale.US)
    .apply { currency = Currency.getInstance(currencyCode) }
    .format(value)
