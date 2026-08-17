package com.synex.core.network

internal object ApiRoutes {
    const val ACCOUNTS = "v1/accounts"
    const val OPTIONS_ACCOUNTS = "v1/accounts/options"
    const val SYMBOLS = "v1/markets/symbols"
    const val CANDLES = "v1/markets/candles"
    const val PORTFOLIO = "v1/portfolio"
    const val ACCOUNT_STREAM_TICKET = "v1/streams/account-ticket"
    const val ACCOUNT_STREAM = "v1/streams/account"
    const val DERIV_CONNECT_URL = "v1/auth/deriv/connect-url"
    const val ONBOARDING_STATUS = "v1/onboarding/status"
    const val RISK_ACKNOWLEDGEMENT = "v1/onboarding/risk-acknowledgement"
    const val CONTRACTS = "v1/markets/contracts"
    const val PROPOSAL = "v1/trading/proposal"
    const val BUY = "v1/trading/buy"
    const val RECEIPT = "v1/trading/receipt"
    const val ORDER_STATUS = "v1/trading/order-status"
    const val POSITION = "v1/positions/status"
    const val SELL = "v1/trading/sell"
    const val CANCEL = "v1/trading/cancel"
    const val CONTRACT_UPDATE = "v1/trading/update"
    const val CONTRACT_UPDATE_HISTORY = "v1/trading/update-history"
    const val STATEMENT = "v1/statement"
    const val PROFIT_TABLE = "v1/profit-table"
    const val FUNDING_CAPABILITIES = "v1/funding/capabilities"
    const val WALLETS = "v1/wallets"
    const val PAYMENT_AGENT_STATISTICS = "v1/payment-agents/statistics"
    const val PAYMENT_AGENTS = "v1/payment-agents"
    const val PAYMENT_AGENT_SETTINGS = "v1/payment-agents/client-settings"
    const val PAYMENT_AGENT_TRANSFERS = "v1/payment-agents/transfers"
    const val PAYMENT_AGENT_WITHDRAWAL_CODE = "v1/payment-agents/withdrawals/verification-code"
    const val PAYMENT_AGENT_WITHDRAWALS = "v1/payment-agents/withdrawals"
}

internal object ApiDefaults {
    const val CANDLE_GRANULARITY_SECONDS = 86_400
    const val CANDLE_COUNT = 30
}
