package com.synex.feature.trade

import com.synex.core.model.ContractOption
import com.synex.core.model.MarketQuote
import com.synex.core.model.TradeProposal
import com.synex.core.model.TradeReceipt
import com.synex.core.model.TradingAccount

data class TradeForm(
    val symbol: String = "",
    val contractType: String = "",
    val amount: String = "10",
    val basis: String = "stake",
    val expiryMode: String = "duration",
    val duration: String = "5",
    val durationUnit: String = "m",
    val dateExpiry: String = "",
    val barrier: String = "",
    val barrier2: String = "",
    val multiplier: String = "",
    val growthRate: String = "",
    val cancellation: String = "",
    val stopLoss: String = "",
    val takeProfit: String = "",
    val payoutPerPoint: String = "",
    val selectedTick: String = "",
)

data class TradeUiState(
    val isLoading: Boolean = true,
    val isBusy: Boolean = false,
    val accounts: List<TradingAccount> = emptyList(),
    val activeAccount: TradingAccount? = null,
    val markets: List<MarketQuote> = emptyList(),
    val contracts: List<ContractOption> = emptyList(),
    val form: TradeForm = TradeForm(),
    val proposal: TradeProposal? = null,
    val quoteSecondsRemaining: Int = 0,
    val receipt: TradeReceipt? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val purchaseLocked: Boolean = false,
) {
    val selectedContract: ContractOption?
        get() = contracts.firstOrNull { it.contractType == form.contractType }
}
