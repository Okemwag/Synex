package com.synex.core.ui

import java.io.IOException
import kotlinx.coroutines.CancellationException
import com.synex.core.network.SynexApiException

fun Throwable.customerMessage(action: String): String {
    if (this is CancellationException) throw this
    return when {
        message.orEmpty().contains("No Deriv trading account", ignoreCase = true) ->
            "Connect a Deriv trading account to continue."
        this is IOException -> "Check your connection and try again."
        this is SynexApiException && message.isNotBlank() -> message
        else -> "We couldn't $action. Please try again."
    }
}
