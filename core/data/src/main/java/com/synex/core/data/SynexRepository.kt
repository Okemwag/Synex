package com.synex.core.data

import com.synex.core.model.MarketQuote
import com.synex.core.model.OverviewSnapshot
import com.synex.core.model.PortfolioSummary
import com.synex.core.model.TradingAccount
import com.synex.core.model.OnboardingStatus
import kotlinx.coroutines.flow.StateFlow

interface SynexRepository {
    val activeLoginId: StateFlow<String?>

    suspend fun overview(): OverviewSnapshot
    suspend fun markets(): List<MarketQuote>
    suspend fun portfolio(): PortfolioSummary
    suspend fun accounts(): List<TradingAccount>
    suspend fun onboardingStatus(): OnboardingStatus
    suspend fun acknowledgeRisk(disclosureVersion: String)
    fun selectAccount(loginId: String)
}
