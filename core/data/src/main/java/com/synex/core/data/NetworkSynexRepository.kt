package com.synex.core.data

import com.synex.core.model.MarketQuote
import com.synex.core.model.OverviewSnapshot
import com.synex.core.model.PortfolioSummary
import com.synex.core.model.TradingAccount
import com.synex.core.model.OnboardingStatus
import com.synex.core.model.AccountBalanceUpdate
import com.synex.core.model.AccountConnectionUpdate
import com.synex.core.model.AccountPositionUpdate
import com.synex.core.model.AccountUpdate
import com.synex.core.model.AccountTransactionUpdate
import com.synex.core.network.SynexApiClient
import com.synex.core.data.mapper.toDomain
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.delay
import kotlinx.coroutines.CancellationException
import com.synex.core.network.SynexApiException
import com.synex.core.model.ActivityPage
import com.synex.core.model.ActivityRow
import com.synex.core.model.ContractOption
import com.synex.core.model.ContractUpdateEvent
import com.synex.core.model.PositionStatus
import com.synex.core.model.TradeProposal
import com.synex.core.model.TradeReceipt
import com.synex.core.model.TradeRequest
import com.synex.core.network.dto.BuyRequestDto
import com.synex.core.network.dto.ContractActionRequestDto
import com.synex.core.network.dto.ContractUpdateRequestDto
import com.synex.core.network.dto.ProposalRequestDto
import com.synex.core.network.dto.SellRequestDto
import com.synex.core.network.dto.PaymentAgentTransferRequest
import com.synex.core.network.dto.PaymentAgentWithdrawalRequest
import com.synex.core.network.dto.WithdrawalVerificationRequest
import com.synex.core.model.DerivWallet
import com.synex.core.model.FundingCapabilities
import com.synex.core.model.PaymentAgent
import com.synex.core.model.PaymentAgentDirectory
import com.synex.core.model.PaymentAgentSettings
import com.synex.core.model.PaymentOperation
import com.synex.core.model.WalletBalance
import com.synex.core.model.WalletTransaction
import com.synex.core.model.WalletTransactionPage
import com.synex.core.model.WithdrawalVerification
import com.synex.core.model.AutomationRun
import com.synex.core.model.AutomationStrategy
import com.synex.core.model.AutomationStrategyDraft
import com.synex.core.model.LegacyAccountSummary
import com.synex.core.network.dto.CreateAutomationStrategyRequest
import com.synex.core.network.dto.StartAutomationRequest
import java.net.URLDecoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

class NetworkSynexRepository(
    private val api: SynexApiClient,
) : SynexRepository {
    private val mutableActiveLoginId = MutableStateFlow<String?>(null)
    override val activeLoginId: StateFlow<String?> = mutableActiveLoginId.asStateFlow()

    override suspend fun overview(): OverviewSnapshot {
        val account = activeAccount()
        val markets = markets()
        val portfolio = portfolioFor(account)

        return OverviewSnapshot(
            account = account,
            portfolio = portfolio,
            watchlist = markets.take(5),
        )
    }

    override suspend fun markets(): List<MarketQuote> =
        api.symbols().data.map { it.toDomain() }

    override suspend fun candles(symbol: String) =
        api.candles(symbol).data.map { it.toDomain() }

    override suspend fun portfolio(): PortfolioSummary {
        return portfolioFor(activeAccount())
    }

    override fun accountUpdates(loginId: String): Flow<AccountUpdate> =
        api.accountStream(loginId).mapNotNull { event ->
            when (event.type) {
                "balance" -> event.balance?.let {
                    AccountBalanceUpdate(
                        loginId = event.loginId,
                        amount = it.amount,
                        currency = it.currency,
                    )
                }
                "position" -> event.position?.let {
                    AccountPositionUpdate(
                        loginId = event.loginId,
                        contractId = it.contractId,
                        contractType = it.contractType,
                        symbol = it.symbol,
                        status = it.status,
                        buyPrice = it.buyPrice,
                        currentSpot = it.currentSpot,
                        profit = it.profit,
                        profitPercentage = it.profitPercentage,
                        payout = it.payout,
                        currency = it.currency,
                        isExpired = it.isExpired,
                        isSold = it.isSold,
                    )
                }
                "transaction" -> event.transaction?.let {
                    AccountTransactionUpdate(
                        loginId = event.loginId,
                        transactionId = it.transactionId,
                        contractId = it.contractId,
                        action = it.action,
                        amount = it.amount,
                        balance = it.balance,
                        currency = it.currency,
                        epochSeconds = it.time,
                        symbol = it.symbol,
                    )
                }
                "status" -> AccountConnectionUpdate(event.loginId, event.status ?: "connecting")
                else -> null
            }
        }

    override suspend fun accounts(): List<TradingAccount> {
        val accounts = api.accounts().accounts.map { it.toDomain() }
        if (accounts.none { it.loginId == mutableActiveLoginId.value }) {
            mutableActiveLoginId.value = accounts.firstOrNull()?.loginId
        }
        return accounts
    }

    override suspend fun createOptionsAccount(accountType: String, realMoneyConfirmed: Boolean) {
        require(accountType == "demo" || accountType == "real")
        api.createOptionsAccount(accountType, realMoneyConfirmed)
    }

    override suspend fun resetDemoBalance(loginId: String) {
        api.resetDemoBalance(loginId)
    }

    override suspend fun derivConnectUrl(): String =
        api.derivConnectUrl().authorizeUrl.takeIf { it.startsWith("https://") }
            ?: throw IllegalStateException("The Deriv connection URL is unavailable.")

    override fun selectAccount(loginId: String) {
        mutableActiveLoginId.value = loginId
    }

    override suspend fun onboardingStatus(): OnboardingStatus = api.onboardingStatus().let {
        OnboardingStatus(it.riskAcknowledged, it.disclosureVersion)
    }

    override suspend fun acknowledgeRisk(disclosureVersion: String) {
        check(api.acknowledgeRisk(disclosureVersion).accepted)
    }

    override suspend fun contracts(symbol: String): List<ContractOption> =
        api.contracts(symbol).data.available.map {
            ContractOption(
                contractType = it.contractType,
                displayName = it.displayName.ifBlank { it.contractType },
                family = it.rules.family,
                requiredFields = it.rules.requiredFields.toSet(),
                optionalFields = it.rules.optionalFields.toSet(),
                minimumStake = it.minimumStake,
                maximumStake = it.maximumStake,
                minimumDuration = it.minimumDuration,
                maximumDuration = it.maximumDuration,
                multiplierChoices = it.multiplierChoices,
                growthRateChoices = it.growthRateChoices,
                cancellationChoices = it.cancellationChoices.map { choice -> choice.content },
            )
        }.distinctBy { it.contractType }

    override suspend fun proposal(request: TradeRequest): TradeProposal =
        api.proposal(request.toDto()).data.let {
            TradeProposal(it.id, it.askPrice, it.payout, it.longcode, it.expiresAt, it.maximumLoss)
        }

    override suspend fun buy(
        request: TradeRequest,
        proposal: TradeProposal,
        realMoneyConfirmed: Boolean,
        instructionKey: String,
    ): TradeReceipt {
        val buyRequest = BuyRequestDto(
            loginId = request.loginId,
            proposalId = proposal.id,
            maxPrice = proposal.askPrice,
            symbol = request.symbol,
            contractType = request.contractType,
            currency = request.currency,
            realMoneyConfirmed = realMoneyConfirmed,
        )
        var buyAccepted = false
        try {
            api.buy(buyRequest, instructionKey)
            buyAccepted = true
            return receipt(instructionKey)
        } catch (error: Exception) {
            if (error is CancellationException) throw error
            if (error is SynexApiException && !buyAccepted) throw error
            repeat(6) {
                delay(3_000)
                val status = runCatching { api.orderStatus(instructionKey) }.getOrNull()?.status
                if (status != null && status != "pending") return receipt(instructionKey)
            }
            throw TradeStatusPendingException()
        }
    }

    private suspend fun receipt(instructionKey: String): TradeReceipt {
        return api.receipt(instructionKey).data.let {
            TradeReceipt(
                orderReference = it.orderReference,
                loginId = it.loginId,
                isVirtual = it.isVirtual,
                status = it.status,
                symbol = it.symbol,
                contractType = it.contractType,
                purchasePrice = it.purchasePrice,
                maximumLoss = it.maximumLoss,
                potentialPayout = it.potentialPayout,
                currency = it.currency,
                contractId = it.contractId,
                transactionId = it.transactionId,
                longcode = it.longcode,
            )
        }
    }

    override suspend fun position(contractId: Long): PositionStatus {
        val loginId = requireActiveLoginId()
        return api.position(loginId, contractId).data.let {
            PositionStatus(
                contractId = it.contractId,
                contractType = it.contractType,
                symbol = it.symbol,
                longcode = it.longcode,
                status = it.status,
                buyPrice = it.buyPrice,
                currentSpot = it.currentSpot,
                profit = it.profit,
                profitPercentage = it.profitPercentage,
                currency = it.currency,
                isExpired = it.isExpired,
                isSold = it.isSold,
            )
        }
    }

    override suspend fun sell(contractId: Long) {
        val account = activeAccount()
        api.sell(SellRequestDto(account.loginId, contractId, currency = account.currency))
    }

    override suspend fun cancel(contractId: Long) {
        api.cancel(ContractActionRequestDto(requireActiveLoginId(), contractId))
    }

    override suspend fun updateContract(contractId: Long, stopLoss: Double?, takeProfit: Double?) {
        api.updateContract(ContractUpdateRequestDto(requireActiveLoginId(), contractId, stopLoss, takeProfit))
    }

    override suspend fun contractUpdateHistory(contractId: Long): List<ContractUpdateEvent> =
        api.contractUpdateHistory(requireActiveLoginId(), contractId).data.map { row ->
            ContractUpdateEvent(
                type = row.text("display_name", "order_type").ifBlank { "Limit order" },
                amount = row.text("display_order_amount", "order_amount").ifBlank { "—" },
                epochSeconds = row.long("order_date"),
            )
        }

    override suspend fun statement(
        offset: Int,
        limit: Int,
        dateFrom: Long?,
        dateTo: Long?,
        actionType: String?,
    ): ActivityPage = api.statement(requireActiveLoginId(), offset, limit, dateFrom, dateTo, actionType)
        .data.toDomain(isProfitTable = false)

    override suspend fun profitTable(
        offset: Int,
        limit: Int,
        dateFrom: String?,
        dateTo: String?,
        sort: String,
    ): ActivityPage = api.profitTable(requireActiveLoginId(), offset, limit, dateFrom, dateTo, sort)
        .data.toDomain(isProfitTable = true)

    override suspend fun fundingCapabilities(): FundingCapabilities = api.fundingCapabilities().let {
        FundingCapabilities(it.connected, it.paymentEnabled, it.reconnectRequired)
    }

    override suspend fun wallets(conversionCurrency: String): List<DerivWallet> = api.wallets(conversionCurrency).data.map { wallet ->
        DerivWallet(
            walletId = wallet.walletId,
            type = wallet.type,
            balances = wallet.balances.mapValues { (_, value) -> WalletBalance(value.balance, value.input, value.output) },
            approximateTotal = wallet.totalBalance?.approximateTotalBalance,
            totalCurrency = wallet.totalBalance?.convertedTo,
        )
    }

    override suspend fun walletTransactions(walletType: String, cursor: String?): WalletTransactionPage =
        api.walletTransactions(walletType, cursor).let { response ->
            WalletTransactionPage(
                transactions = response.data.transactions.map { transaction ->
                    WalletTransaction(
                        requestId = transaction.requestId,
                        transactionId = transaction.transactionId,
                        timestamp = transaction.timestamp,
                        category = transaction.category,
                        channel = transaction.channel,
                        status = transaction.metadata.transactionStatus,
                        amount = transaction.metadata.transactionNetAmount,
                        currency = transaction.metadata.transactionCurrency,
                    )
                },
                nextCursor = response.links.next.pageCursor(),
                previousCursor = response.links.prev.pageCursor(),
            )
        }

    override suspend fun paymentAgentDirectory(): PaymentAgentDirectory = api.paymentAgentStatistics().data.let {
        PaymentAgentDirectory(it.availableCountries, it.availableCurrencies)
    }

    override suspend fun paymentAgents(currency: String, country: String): List<PaymentAgent> =
        api.paymentAgents(currency, country).data.map { it.toDomain() }

    override suspend fun ownPaymentAgent(): PaymentAgent? =
        runCatching { api.paymentAgent("me").data.toDomain() }.getOrNull()?.takeIf { it.id > 0 }

    override suspend fun paymentAgentSettings(): PaymentAgentSettings = api.paymentAgentSettings().data.toDomain()

    override suspend fun updatePaymentAgentSettings(showRealName: Boolean): PaymentAgentSettings =
        api.updatePaymentAgentSettings(showRealName).data.toDomain()

    override suspend fun paymentAgentTransfer(
        toNickname: String,
        amount: String,
        currency: String,
        requestId: String,
        dryRun: Boolean,
    ): PaymentOperation = api.paymentAgentTransfer(
        PaymentAgentTransferRequest(toNickname, amount, currency, requestId, dryRun),
    ).data.let { PaymentOperation(requestId, it.status, it.transactionId) }

    override suspend fun paymentAgentTransferStatus(requestId: String): PaymentOperation =
        api.paymentAgentTransferStatus(requestId).data.let { PaymentOperation(requestId, it.status, it.transactionId) }

    override suspend fun requestWithdrawalCode(agentId: Long, amount: String, currency: String): WithdrawalVerification =
        api.requestWithdrawalCode(WithdrawalVerificationRequest(agentId, amount, currency)).data.let {
            WithdrawalVerification(it.message, it.expiresAt, it.nextRequestAt)
        }

    override suspend fun paymentAgentWithdrawal(
        agentId: Long,
        amount: String,
        currency: String,
        verificationCode: String,
        requestId: String,
    ): PaymentOperation = api.paymentAgentWithdrawal(
        PaymentAgentWithdrawalRequest(agentId, amount, currency, verificationCode, requestId),
    ).data.let { PaymentOperation(requestId, it.status, it.transactionId) }

    override suspend fun paymentAgentWithdrawalStatus(requestId: String): PaymentOperation =
        api.paymentAgentWithdrawalStatus(requestId).data.let { PaymentOperation(requestId, it.status, it.transactionId) }

    override suspend fun automationStrategies(): List<AutomationStrategy> =
        api.automationStrategies().strategies.map { it.toDomain() }

    override suspend fun createAutomationStrategy(draft: AutomationStrategyDraft): AutomationStrategy {
        val account = activeAccount()
        return api.createAutomationStrategy(
            CreateAutomationStrategyRequest(
                loginId = account.loginId,
                name = draft.name,
                symbol = draft.symbol,
                contractType = draft.contractType,
                currency = account.currency,
                amount = draft.amount,
                basis = "stake",
                duration = draft.duration,
                durationUnit = draft.durationUnit,
                intervalSeconds = draft.intervalSeconds,
                maxTrades = draft.maxTrades,
                maxLoss = draft.maxLoss,
                maxDurationMinutes = draft.maxDurationMinutes,
                maxConcurrentPositions = draft.maxConcurrentPositions,
            ),
        ).toDomain()
    }

    override suspend fun automationRuns(): List<AutomationRun> = api.automationRuns().runs.map { it.toDomain() }

    override suspend fun startAutomation(strategyId: String, realMoneyConfirmed: Boolean): AutomationRun =
        api.startAutomation(StartAutomationRequest(strategyId, realMoneyConfirmed)).toDomain()

    override suspend fun transitionAutomation(runId: String, action: String): AutomationRun =
        api.transitionAutomation(runId, action).toDomain()

    override suspend fun automationKillSwitchEnabled(): Boolean = api.automationSafety().killSwitchEnabled

    override suspend fun setAutomationKillSwitch(enabled: Boolean): Boolean = api.setAutomationKillSwitch(enabled).killSwitchEnabled

    override suspend fun accountNickname(): String =
        (api.accountNickname().data as? JsonObject)?.text("nickname").orEmpty()

    override suspend fun legacyAccountSummary(): LegacyAccountSummary {
        val statusData = api.legacyMigrationStatus().data as? JsonObject
        val accountData = runCatching { api.legacyAccounts().data as? JsonObject }.getOrNull()
        val loginIds = (accountData?.get("loginids") as? JsonObject)?.keys.orEmpty().filter { it.matches(Regex("^[A-Z]+[0-9]+$")) }.sorted()
        return LegacyAccountSummary(statusData?.text("status", "migration_status").orEmpty().ifBlank { "unknown" }, loginIds)
    }

    override suspend fun legacyStatement(
        loginId: String,
        offset: Int,
        limit: Int,
        dateFrom: Long?,
        dateTo: Long?,
        actionType: String?,
    ): ActivityPage {
        val data = api.legacyStatement(loginId, offset, limit, dateFrom, dateTo, actionType).data as? JsonObject ?: JsonObject(emptyMap())
        val statement = data["statement"] as? JsonObject ?: data
        val transactions = (statement["transactions"] as? JsonArray)?.mapNotNull { it as? JsonObject }.orEmpty()
        val count = statement.text("count").toIntOrNull() ?: transactions.size
        return com.synex.core.network.dto.ActivityDataDto(transactions, count).toDomain(isProfitTable = false)
    }

    override suspend fun derivSystemStatus(): String {
        return api.systemStatus().text("status").ifBlank { "unknown" }
    }

    private suspend fun activeAccount(): TradingAccount {
        val accounts = accounts()
        return accounts.firstOrNull { it.loginId == mutableActiveLoginId.value }
            ?: throw DerivAccountRequiredException()
    }

    private suspend fun requireActiveLoginId(): String = activeAccount().loginId

    private suspend fun portfolioFor(account: TradingAccount): PortfolioSummary {
        val positions = api.portfolio(account.loginId).data.contracts.map { it.toDomain() }
        val hasCompleteValuation = positions.all { it.currentValue != null && it.profitLoss != null }
        return PortfolioSummary(
            equity = if (hasCompleteValuation) {
                account.balance + positions.sumOf { it.currentValue ?: 0.0 }
            } else {
                null
            },
            availableCash = account.balance,
            profitLoss = if (hasCompleteValuation) {
                positions.sumOf { it.profitLoss ?: 0.0 }
            } else {
                null
            },
            currency = account.currency,
            positions = positions,
        )
    }
}

private fun com.synex.core.network.dto.PaymentAgentDto.toDomain() = PaymentAgent(
    id = id,
    name = name.orEmpty(),
    nickname = nickname.orEmpty(),
    information = information.orEmpty(),
    paymentMethods = paymentMethods.orEmpty(),
    withdrawalCommission = withdrawalCommission,
    withdrawalMinimum = withdrawalMinimum,
    withdrawalMaximum = withdrawalMaximum,
)

private fun com.synex.core.network.dto.PaymentAgentSettingsDto.toDomain() = PaymentAgentSettings(
    depositEnabled = depositEnabled,
    withdrawEnabled = withdrawEnabled,
    showRealName = showRealName,
)

private fun com.synex.core.network.dto.AutomationStrategyDto.toDomain() = AutomationStrategy(
    id = id,
    loginId = loginId,
    isVirtual = isVirtual,
    name = name,
    symbol = symbol,
    contractType = contractType,
    currency = currency,
    amount = amount,
    intervalSeconds = intervalSeconds,
    maxTrades = maxTrades,
    maxLoss = maxLoss,
    maxDurationMinutes = maxDurationMinutes,
    maxConcurrentPositions = maxConcurrentPositions,
)

private fun com.synex.core.network.dto.AutomationRunDto.toDomain() = AutomationRun(
    id = id,
    strategyId = strategyId,
    strategyName = strategyName,
    loginId = loginId,
    status = status,
    isVirtual = isVirtual,
    tradeCount = tradeCount,
    successfulTrades = successfulTrades,
    failedTrades = failedTrades,
    committedLoss = committedLoss,
    settledTrades = settledTrades,
    realizedProfit = realizedProfit,
    startedAt = startedAt,
    nextExecutionAt = nextExecutionAt,
    lastError = lastError,
)

private fun String?.pageCursor(): String? {
    if (this.isNullOrBlank()) return null
    val encoded = substringAfter("page_cursor=", "").substringBefore('&')
    return encoded.takeIf(String::isNotBlank)?.let { URLDecoder.decode(it, "UTF-8") }
}

private fun TradeRequest.toDto() = ProposalRequestDto(
    loginId = loginId,
    contractType = contractType,
    symbol = symbol,
    amount = amount,
    basis = basis,
    currency = currency,
    duration = duration,
    durationUnit = durationUnit,
    dateExpiry = dateExpiry,
    barrier = barrier,
    barrier2 = barrier2,
    multiplier = multiplier,
    growthRate = growthRate,
    cancellation = cancellation,
    stopLoss = stopLoss,
    takeProfit = takeProfit,
    payoutPerPoint = payoutPerPoint,
    selectedTick = selectedTick,
)

private fun com.synex.core.network.dto.ActivityDataDto.toDomain(isProfitTable: Boolean): ActivityPage {
    val rows = transactions.map { row ->
        val buyPrice = row.doubleOrNull("buy_price")
        val sellPrice = row.doubleOrNull("sell_price")
        ActivityRow(
            transactionId = row.long("transaction_id"),
            contractId = row.longOrNull("contract_id"),
            type = row.text("action_type", "transaction_type", "contract_type", "shortcode").ifBlank {
                if (isProfitTable) "Trade" else "Transaction"
            },
            description = row.text("longcode", "description", "underlying_symbol"),
            amount = row.doubleOrNull("amount"),
            balanceAfter = row.doubleOrNull("balance_after"),
            buyPrice = buyPrice,
            sellPrice = sellPrice,
            profit = row.doubleOrNull("profit") ?: if (buyPrice != null && sellPrice != null) sellPrice - buyPrice else null,
            currency = row.text("currency").ifBlank { "USD" },
            epochSeconds = row.long("sell_time", "transaction_time", "purchase_time"),
            details = row.entries.sortedBy { it.key }.map { (key, value) ->
                key.replace('_', ' ').replaceFirstChar(Char::uppercase) to
                    ((value as? JsonPrimitive)?.contentOrNull ?: value.toString())
            },
        )
    }
    return ActivityPage(rows, count.takeIf { it > 0 } ?: rows.size)
}

private fun JsonObject.text(vararg keys: String): String =
    keys.firstNotNullOfOrNull { key -> get(key)?.jsonPrimitive?.contentOrNull } ?: ""

private fun JsonObject.doubleOrNull(key: String): Double? = text(key).toDoubleOrNull()

private fun JsonObject.longOrNull(key: String): Long? = text(key).toDoubleOrNull()?.toLong()

private fun JsonObject.long(vararg keys: String): Long =
    keys.firstNotNullOfOrNull { key -> longOrNull(key) } ?: 0L
