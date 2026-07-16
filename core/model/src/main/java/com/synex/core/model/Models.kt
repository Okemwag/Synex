package com.synex.core.model

data class TradingAccount(
    val loginId: String,
    val currency: String,
    val balance: Double,
    val isVirtual: Boolean,
)

data class MarketQuote(
    val symbol: String,
    val displayName: String,
    val market: String,
    val price: Double,
    val changePercent: Double,
    val isOpen: Boolean = true,
)

data class Candle(
    val epochSeconds: Long,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
)

data class Position(
    val contractId: Long,
    val symbol: String,
    val displayName: String,
    val direction: PositionDirection,
    val buyPrice: Double,
    val currentValue: Double,
    val profitLoss: Double,
    val currency: String,
    val purchaseEpochSeconds: Long,
)

data class PortfolioSummary(
    val equity: Double,
    val availableCash: Double,
    val profitLoss: Double,
    val currency: String,
    val positions: List<Position>,
)

data class OverviewSnapshot(
    val account: TradingAccount,
    val portfolio: PortfolioSummary,
    val watchlist: List<MarketQuote>,
)
