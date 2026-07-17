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
import com.synex.core.network.SynexApiClient
import com.synex.core.data.mapper.toDomain
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull

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

    private suspend fun activeAccount(): TradingAccount {
        val accounts = accounts()
        return accounts.firstOrNull { it.loginId == mutableActiveLoginId.value }
            ?: throw DerivAccountRequiredException()
    }

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
