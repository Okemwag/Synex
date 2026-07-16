package com.synex.core.network

internal object ApiRoutes {
    const val ACCOUNTS = "v1/accounts"
    const val SYMBOLS = "v1/markets/symbols"
    const val CANDLES = "v1/markets/candles"
    const val PORTFOLIO = "v1/portfolio"
}

internal object ApiDefaults {
    const val CANDLE_GRANULARITY_SECONDS = 86_400
    const val CANDLE_COUNT = 30
}
