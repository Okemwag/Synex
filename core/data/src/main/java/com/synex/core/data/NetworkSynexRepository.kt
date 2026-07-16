package com.synex.core.data

import com.synex.core.model.MarketQuote
import com.synex.core.model.OverviewSnapshot
import com.synex.core.model.PortfolioSummary
import com.synex.core.model.Position
import com.synex.core.model.TradingAccount
import com.synex.core.model.OnboardingStatus
import com.synex.core.network.SynexApiClient
import com.synex.core.data.mapper.toDomain
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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

    override suspend fun portfolio(): PortfolioSummary {
        return portfolioFor(activeAccount())
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
        val openValue = positions.sumOf(Position::currentValue)
        return PortfolioSummary(
            equity = account.balance + openValue,
            availableCash = account.balance,
            profitLoss = positions.sumOf(Position::profitLoss),
            currency = account.currency,
            positions = positions,
        )
    }
}
