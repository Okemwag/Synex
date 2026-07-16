package com.synex.core.network

internal object ApiRoutes {
    const val ACCOUNTS = "v1/accounts"
    const val SYMBOLS = "v1/markets/symbols"
    const val CANDLES = "v1/markets/candles"
    const val PORTFOLIO = "v1/portfolio"
    const val DERIV_CONNECT_URL = "v1/auth/deriv/connect-url"
    const val ONBOARDING_STATUS = "v1/onboarding/status"
    const val RISK_ACKNOWLEDGEMENT = "v1/onboarding/risk-acknowledgement"
}

internal object ApiDefaults {
    const val CANDLE_GRANULARITY_SECONDS = 86_400
    const val CANDLE_COUNT = 30
}
