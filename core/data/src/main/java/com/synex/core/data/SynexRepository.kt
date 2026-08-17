package com.synex.core.data

import com.synex.core.model.MarketQuote
import com.synex.core.model.Candle
import com.synex.core.model.AccountUpdate
import kotlinx.coroutines.flow.Flow
import com.synex.core.model.OverviewSnapshot
import com.synex.core.model.PortfolioSummary
import com.synex.core.model.TradingAccount
import com.synex.core.model.OnboardingStatus
import kotlinx.coroutines.flow.StateFlow
import com.synex.core.model.ActivityPage
import com.synex.core.model.ContractOption
import com.synex.core.model.ContractUpdateEvent
import com.synex.core.model.PositionStatus
import com.synex.core.model.TradeProposal
import com.synex.core.model.TradeReceipt
import com.synex.core.model.TradeRequest
import com.synex.core.model.DerivWallet
import com.synex.core.model.FundingCapabilities
import com.synex.core.model.PaymentAgent
import com.synex.core.model.PaymentAgentDirectory
import com.synex.core.model.PaymentAgentSettings
import com.synex.core.model.PaymentOperation
import com.synex.core.model.WalletTransactionPage
import com.synex.core.model.WithdrawalVerification
import com.synex.core.model.AutomationRun
import com.synex.core.model.AutomationStrategy
import com.synex.core.model.AutomationStrategyDraft
import com.synex.core.model.LegacyAccountSummary

interface SynexRepository {
    val activeLoginId: StateFlow<String?>

    suspend fun overview(): OverviewSnapshot
    suspend fun markets(): List<MarketQuote>
    suspend fun candles(symbol: String): List<Candle>
    suspend fun portfolio(): PortfolioSummary
    fun accountUpdates(loginId: String): Flow<AccountUpdate>
    suspend fun accounts(): List<TradingAccount>
    suspend fun createOptionsAccount(accountType: String, realMoneyConfirmed: Boolean): Unit = unsupportedFunding()
    suspend fun resetDemoBalance(loginId: String): Unit = unsupportedFunding()
    suspend fun derivConnectUrl(): String
    suspend fun onboardingStatus(): OnboardingStatus
    suspend fun acknowledgeRisk(disclosureVersion: String)
    fun selectAccount(loginId: String)
    suspend fun contracts(symbol: String): List<ContractOption>
    suspend fun proposal(request: TradeRequest): TradeProposal
    suspend fun buy(request: TradeRequest, proposal: TradeProposal, realMoneyConfirmed: Boolean, instructionKey: String): TradeReceipt
    suspend fun position(contractId: Long): PositionStatus
    suspend fun sell(contractId: Long)
    suspend fun cancel(contractId: Long)
    suspend fun updateContract(contractId: Long, stopLoss: Double?, takeProfit: Double?)
    suspend fun contractUpdateHistory(contractId: Long): List<ContractUpdateEvent>
    suspend fun statement(offset: Int, limit: Int, dateFrom: Long?, dateTo: Long?, actionType: String?): ActivityPage
    suspend fun profitTable(offset: Int, limit: Int, dateFrom: String?, dateTo: String?, sort: String): ActivityPage
    suspend fun fundingCapabilities(): FundingCapabilities = unsupportedFunding()
    suspend fun wallets(conversionCurrency: String): List<DerivWallet> = unsupportedFunding()
    suspend fun walletTransactions(walletType: String, cursor: String? = null): WalletTransactionPage = unsupportedFunding()
    suspend fun paymentAgentDirectory(): PaymentAgentDirectory = unsupportedFunding()
    suspend fun paymentAgents(currency: String, country: String = ""): List<PaymentAgent> = unsupportedFunding()
    suspend fun ownPaymentAgent(): PaymentAgent? = unsupportedFunding()
    suspend fun paymentAgentSettings(): PaymentAgentSettings = unsupportedFunding()
    suspend fun updatePaymentAgentSettings(showRealName: Boolean): PaymentAgentSettings = unsupportedFunding()
    suspend fun paymentAgentTransfer(toNickname: String, amount: String, currency: String, requestId: String, dryRun: Boolean): PaymentOperation = unsupportedFunding()
    suspend fun paymentAgentTransferStatus(requestId: String): PaymentOperation = unsupportedFunding()
    suspend fun requestWithdrawalCode(agentId: Long, amount: String, currency: String): WithdrawalVerification = unsupportedFunding()
    suspend fun paymentAgentWithdrawal(agentId: Long, amount: String, currency: String, verificationCode: String, requestId: String): PaymentOperation = unsupportedFunding()
    suspend fun paymentAgentWithdrawalStatus(requestId: String): PaymentOperation = unsupportedFunding()
    suspend fun automationStrategies(): List<AutomationStrategy> = unsupportedFunding()
    suspend fun createAutomationStrategy(draft: AutomationStrategyDraft): AutomationStrategy = unsupportedFunding()
    suspend fun automationRuns(): List<AutomationRun> = unsupportedFunding()
    suspend fun startAutomation(strategyId: String, realMoneyConfirmed: Boolean): AutomationRun = unsupportedFunding()
    suspend fun transitionAutomation(runId: String, action: String): AutomationRun = unsupportedFunding()
    suspend fun automationKillSwitchEnabled(): Boolean = unsupportedFunding()
    suspend fun setAutomationKillSwitch(enabled: Boolean): Boolean = unsupportedFunding()
    suspend fun accountNickname(): String = unsupportedFunding()
    suspend fun legacyAccountSummary(): LegacyAccountSummary = unsupportedFunding()
    suspend fun legacyStatement(loginId: String, offset: Int, limit: Int, dateFrom: Long?, dateTo: Long?, actionType: String?): ActivityPage = unsupportedFunding()
    suspend fun derivSystemStatus(): String = unsupportedFunding()
}

private fun <T> unsupportedFunding(): T = throw UnsupportedOperationException("This repository does not implement account and funding operations.")

class DerivAccountRequiredException : IllegalStateException(
    "No Deriv trading account is linked to this Synex profile.",
)

class TradeStatusPendingException : IllegalStateException(
    "This order is still being checked with Deriv. Do not place another trade on this account until it is resolved.",
)
