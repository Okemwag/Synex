package com.synex.core.model

data class TradingAccount(
    val loginId: String,
    val currency: String,
    val balance: Double,
    val isVirtual: Boolean,
    val status: String = "",
    val accountType: String = "",
    val accountGroup: String = "",
    val jurisdiction: String = "",
    val readyForTrading: Boolean = true,
    val readinessMissing: List<String> = emptyList(),
    val paymentScope: Boolean = false,
)

data class FundingCapabilities(
    val connected: Boolean,
    val paymentEnabled: Boolean,
    val reconnectRequired: Boolean,
)

data class WalletBalance(val balance: String, val input: String, val output: String)

data class DerivWallet(
    val walletId: String,
    val type: String,
    val balances: Map<String, WalletBalance>,
    val approximateTotal: String?,
    val totalCurrency: String?,
)

data class WalletTransaction(
    val requestId: String,
    val transactionId: Long,
    val timestamp: String,
    val category: String,
    val channel: String,
    val status: String,
    val amount: String,
    val currency: String,
)

data class WalletTransactionPage(
    val transactions: List<WalletTransaction>,
    val nextCursor: String?,
    val previousCursor: String?,
)

data class PaymentAgent(
    val id: Long,
    val name: String,
    val nickname: String,
    val information: String,
    val paymentMethods: List<String>,
    val withdrawalCommission: Double?,
    val withdrawalMinimum: String?,
    val withdrawalMaximum: String?,
)

data class PaymentAgentDirectory(
    val countries: List<String>,
    val currencies: List<String>,
)

data class PaymentAgentSettings(
    val depositEnabled: Boolean,
    val withdrawEnabled: Boolean,
    val showRealName: Boolean,
)

data class PaymentOperation(val requestId: String, val status: String, val transactionId: Long?)

data class WithdrawalVerification(val message: String, val expiresAt: Long, val nextRequestAt: Long)

data class AutomationStrategy(
    val id: String,
    val loginId: String,
    val isVirtual: Boolean,
    val name: String,
    val symbol: String,
    val contractType: String,
    val currency: String,
    val amount: Double,
    val intervalSeconds: Int,
    val maxTrades: Int,
    val maxLoss: Double,
    val maxDurationMinutes: Int,
    val maxConcurrentPositions: Int,
)

data class AutomationRun(
    val id: String,
    val strategyId: String,
    val strategyName: String,
    val loginId: String,
    val status: String,
    val isVirtual: Boolean,
    val tradeCount: Int,
    val successfulTrades: Int,
    val failedTrades: Int,
    val committedLoss: Double,
    val settledTrades: Int,
    val realizedProfit: Double,
    val startedAt: String,
    val nextExecutionAt: String,
    val lastError: String,
)

data class AutomationStrategyDraft(
    val name: String,
    val symbol: String,
    val contractType: String,
    val amount: Double,
    val duration: Int,
    val durationUnit: String,
    val intervalSeconds: Int,
    val maxTrades: Int,
    val maxLoss: Double,
    val maxDurationMinutes: Int,
    val maxConcurrentPositions: Int,
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
