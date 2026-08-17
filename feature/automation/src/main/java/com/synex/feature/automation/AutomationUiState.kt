package com.synex.feature.automation

import com.synex.core.model.AutomationRun
import com.synex.core.model.AutomationStrategy
import com.synex.core.model.ContractOption
import com.synex.core.model.MarketQuote

data class AutomationUiState(
    val isLoading: Boolean = true,
    val isBusy: Boolean = false,
    val hasAccount: Boolean = false,
    val killSwitchEnabled: Boolean = false,
    val strategies: List<AutomationStrategy> = emptyList(),
    val runs: List<AutomationRun> = emptyList(),
    val markets: List<MarketQuote> = emptyList(),
    val contracts: List<ContractOption> = emptyList(),
    val name: String = "",
    val symbol: String = "",
    val contractType: String = "",
    val amount: String = "10",
    val duration: String = "5",
    val durationUnit: String = "m",
    val intervalSeconds: String = "60",
    val maxTrades: String = "10",
    val maxLoss: String = "100",
    val maxDurationMinutes: String = "60",
    val maxConcurrentPositions: String = "1",
    val pendingRealStrategy: AutomationStrategy? = null,
    val confirmEmergencyStop: Boolean = false,
    val message: String? = null,
    val errorMessage: String? = null,
)
