package com.synex.core.ui

import java.io.IOException
import kotlinx.coroutines.CancellationException

fun Throwable.customerMessage(action: String): String {
    if (this is CancellationException) throw this
    return when {
        message.orEmpty().contains("No Deriv trading account", ignoreCase = true) ->
            "Connect a Deriv trading account to continue."
        this is IOException -> "Check your connection and try again."
        else -> "We couldn't $action. Please try again."
    }
}
