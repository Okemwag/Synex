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
    val price: Double?,
    val changePercent: Double?,
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
    val currentValue: Double?,
    val profitLoss: Double?,
    val currency: String,
    val purchaseEpochSeconds: Long,
)

data class PortfolioSummary(
    val equity: Double?,
    val availableCash: Double,
    val profitLoss: Double?,
    val currency: String,
    val positions: List<Position>,
)

data class OverviewSnapshot(
    val account: TradingAccount,
    val portfolio: PortfolioSummary,
    val watchlist: List<MarketQuote>,
)

data class ContractOption(
    val contractType: String,
    val displayName: String,
    val family: String,
    val requiredFields: Set<String>,
    val optionalFields: Set<String>,
    val minimumStake: Double?,
    val maximumStake: Double?,
    val minimumDuration: String,
    val maximumDuration: String,
    val multiplierChoices: List<Double>,
    val growthRateChoices: List<Double>,
    val cancellationChoices: List<String>,
)

data class TradeRequest(
    val loginId: String,
    val contractType: String,
    val symbol: String,
    val amount: Double,
    val basis: String,
    val currency: String,
    val duration: Int? = null,
    val durationUnit: String? = null,
    val dateExpiry: Long? = null,
    val barrier: String? = null,
    val barrier2: String? = null,
    val multiplier: Double? = null,
    val growthRate: Double? = null,
    val cancellation: String? = null,
    val stopLoss: Double? = null,
    val takeProfit: Double? = null,
    val payoutPerPoint: Double? = null,
    val selectedTick: Int? = null,
)

data class TradeProposal(
    val id: String,
    val askPrice: Double,
    val payout: Double?,
    val longcode: String,
    val expiresAt: String,
    val maximumLoss: Double,
)

data class TradeReceipt(
    val orderReference: String,
    val loginId: String,
    val isVirtual: Boolean,
    val status: String,
    val symbol: String,
    val contractType: String,
    val purchasePrice: Double,
    val maximumLoss: Double,
    val potentialPayout: Double,
    val currency: String,
    val contractId: Long?,
    val transactionId: Long?,
    val longcode: String,
)

data class PositionStatus(
    val contractId: Long,
    val contractType: String,
    val symbol: String,
    val longcode: String,
    val status: String,
    val buyPrice: Double,
    val currentSpot: Double,
    val profit: Double,
    val profitPercentage: Double,
    val currency: String,
    val isExpired: Boolean,
    val isSold: Boolean,
)

data class ContractUpdateEvent(
    val type: String,
    val amount: String,
    val epochSeconds: Long,
)

data class ActivityPage(
    val rows: List<ActivityRow>,
    val count: Int,
)

data class ActivityRow(
    val transactionId: Long,
    val contractId: Long?,
    val type: String,
    val description: String,
    val amount: Double?,
    val balanceAfter: Double?,
    val buyPrice: Double?,
    val sellPrice: Double?,
    val profit: Double?,
    val currency: String,
    val epochSeconds: Long,
    val details: List<Pair<String, String>>,
)

sealed interface AccountUpdate

data class AccountBalanceUpdate(
    val loginId: String,
    val amount: Double,
    val currency: String,
) : AccountUpdate

data class AccountPositionUpdate(
    val loginId: String,
    val contractId: Long,
    val contractType: String,
    val symbol: String,
    val status: String,
    val buyPrice: Double,
    val currentSpot: Double,
    val profit: Double,
    val profitPercentage: Double,
    val payout: Double,
    val currency: String,
    val isExpired: Boolean,
    val isSold: Boolean,
) : AccountUpdate

data class AccountConnectionUpdate(
    val loginId: String,
    val status: String,
) : AccountUpdate

data class AccountTransactionUpdate(
    val loginId: String,
    val transactionId: Long,
    val contractId: Long?,
    val action: String,
    val amount: Double,
    val balance: Double,
    val currency: String,
    val epochSeconds: Long,
    val symbol: String,
) : AccountUpdate
