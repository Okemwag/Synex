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

interface SynexRepository {
    val activeLoginId: StateFlow<String?>

    suspend fun overview(): OverviewSnapshot
    suspend fun markets(): List<MarketQuote>
    suspend fun candles(symbol: String): List<Candle>
    suspend fun portfolio(): PortfolioSummary
    fun accountUpdates(loginId: String): Flow<AccountUpdate>
    suspend fun accounts(): List<TradingAccount>
    suspend fun derivConnectUrl(): String
    suspend fun onboardingStatus(): OnboardingStatus
    suspend fun acknowledgeRisk(disclosureVersion: String)
    fun selectAccount(loginId: String)
}

class DerivAccountRequiredException : IllegalStateException(
    "No Deriv trading account is linked to this Synex profile.",
)
