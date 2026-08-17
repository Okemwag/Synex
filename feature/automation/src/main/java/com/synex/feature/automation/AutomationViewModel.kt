package com.synex.feature.automation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.synex.core.data.SynexRepository
import com.synex.core.model.AutomationStrategy
import com.synex.core.model.AutomationStrategyDraft
import com.synex.core.ui.customerMessage
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class AutomationViewModel(private val repository: SynexRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(AutomationUiState())
    val state: StateFlow<AutomationUiState> = mutableState.asStateFlow()

    init {
        refresh()
        viewModelScope.launch {
            while (isActive) {
                delay(5_000)
                runCatching { repository.automationRuns() }.getOrNull()?.let { runs -> mutableState.update { it.copy(runs = runs) } }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            mutableState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                val accounts = repository.accounts()
                val markets = repository.markets()
                val symbol = mutableState.value.symbol.ifBlank { markets.firstOrNull()?.symbol.orEmpty() }
                Loaded(
                    hasAccount = accounts.isNotEmpty(),
                    strategies = repository.automationStrategies(),
                    runs = repository.automationRuns(),
                    markets = markets,
                    contracts = if (symbol.isBlank()) emptyList() else repository.contracts(symbol),
                    symbol = symbol,
                    killSwitch = repository.automationKillSwitchEnabled(),
                )
            }.fold(
                onSuccess = { loaded -> mutableState.update { it.copy(isLoading = false, hasAccount = loaded.hasAccount, strategies = loaded.strategies, runs = loaded.runs, markets = loaded.markets, contracts = loaded.contracts, symbol = loaded.symbol, contractType = it.contractType.takeIf { type -> loaded.contracts.any { contract -> contract.contractType == type } } ?: loaded.contracts.firstOrNull()?.contractType.orEmpty(), killSwitchEnabled = loaded.killSwitch) } },
                onFailure = { error -> mutableState.update { it.copy(isLoading = false, errorMessage = error.customerMessage("load automation")) } },
            )
        }
    }

    fun setName(value: String) = mutableState.update { it.copy(name = value) }
    fun setAmount(value: String) = mutableState.update { it.copy(amount = value) }
    fun setDuration(value: String) = mutableState.update { it.copy(duration = value) }
    fun setDurationUnit(value: String) = mutableState.update { it.copy(durationUnit = value) }
    fun setInterval(value: String) = mutableState.update { it.copy(intervalSeconds = value) }
    fun setMaxTrades(value: String) = mutableState.update { it.copy(maxTrades = value) }
    fun setMaxLoss(value: String) = mutableState.update { it.copy(maxLoss = value) }
    fun setMaxDuration(value: String) = mutableState.update { it.copy(maxDurationMinutes = value) }
    fun setMaxConcurrent(value: String) = mutableState.update { it.copy(maxConcurrentPositions = value) }
    fun setContractType(value: String) = mutableState.update { it.copy(contractType = value) }

    fun setSymbol(value: String) {
        mutableState.update { it.copy(symbol = value, contractType = "", contracts = emptyList()) }
        viewModelScope.launch {
            runCatching { repository.contracts(value) }.fold(
                onSuccess = { contracts -> mutableState.update { it.copy(contracts = contracts, contractType = contracts.firstOrNull()?.contractType.orEmpty()) } },
                onFailure = { error -> mutableState.update { it.copy(errorMessage = error.customerMessage("load contracts")) } },
            )
        }
    }

    fun createStrategy() {
        val current = state.value
        val draft = runCatching {
            AutomationStrategyDraft(
                name = current.name.trim().also { require(it.isNotEmpty()) },
                symbol = current.symbol.also { require(it.isNotEmpty()) },
                contractType = current.contractType.also { require(it.isNotEmpty()) },
                amount = current.amount.toDouble().also { require(it > 0) },
                duration = current.duration.toInt().also { require(it > 0) },
                durationUnit = current.durationUnit,
                intervalSeconds = current.intervalSeconds.toInt().also { require(it >= 30) },
                maxTrades = current.maxTrades.toInt().also { require(it > 0) },
                maxLoss = current.maxLoss.toDouble().also { require(it > 0) },
                maxDurationMinutes = current.maxDurationMinutes.toInt().also { require(it > 0) },
                maxConcurrentPositions = current.maxConcurrentPositions.toInt().also { require(it > 0) },
            )
        }.getOrElse { return fail("Complete every strategy and safety field with a valid positive value. The interval must be at least 30 seconds.") }
        viewModelScope.launch {
            mutableState.update { it.copy(isBusy = true, errorMessage = null, message = null) }
            runCatching { repository.createAutomationStrategy(draft) }.fold(
                onSuccess = { strategy -> mutableState.update { it.copy(isBusy = false, strategies = listOf(strategy) + it.strategies, name = "", message = "Strategy created. Review its limits before starting it.") } },
                onFailure = { error -> mutableState.update { it.copy(isBusy = false, errorMessage = error.customerMessage("create the strategy")) } },
            )
        }
    }

    fun askToStart(strategy: AutomationStrategy) {
        if (strategy.isVirtual) start(strategy, false) else mutableState.update { it.copy(pendingRealStrategy = strategy) }
    }

    fun dismissRealConfirmation() = mutableState.update { it.copy(pendingRealStrategy = null) }
    fun confirmRealStart() { state.value.pendingRealStrategy?.let { start(it, true) }; dismissRealConfirmation() }

    private fun start(strategy: AutomationStrategy, realConfirmed: Boolean) {
        viewModelScope.launch {
            mutableState.update { it.copy(isBusy = true, errorMessage = null) }
            runCatching { repository.startAutomation(strategy.id, realConfirmed) }.fold(
                onSuccess = { run -> mutableState.update { it.copy(isBusy = false, runs = listOf(run) + it.runs, message = "Automation started.") } },
                onFailure = { error -> mutableState.update { it.copy(isBusy = false, errorMessage = error.customerMessage("start automation")) } },
            )
        }
    }

    fun transition(runId: String, action: String) {
        viewModelScope.launch {
            mutableState.update { it.copy(isBusy = true, errorMessage = null) }
            runCatching { repository.transitionAutomation(runId, action) }.fold(
                onSuccess = { updated -> mutableState.update { it.copy(isBusy = false, runs = it.runs.map { run -> if (run.id == updated.id) updated else run }) } },
                onFailure = { error -> mutableState.update { it.copy(isBusy = false, errorMessage = error.customerMessage("change automation state")) } },
            )
        }
    }

    fun askEmergencyStop() = mutableState.update { it.copy(confirmEmergencyStop = true) }
    fun dismissEmergencyStop() = mutableState.update { it.copy(confirmEmergencyStop = false) }
    fun toggleKillSwitch() {
        val enabled = !state.value.killSwitchEnabled
        mutableState.update { it.copy(confirmEmergencyStop = false) }
        viewModelScope.launch {
            mutableState.update { it.copy(isBusy = true, errorMessage = null) }
            runCatching { repository.setAutomationKillSwitch(enabled) }.fold(
                onSuccess = { value -> mutableState.update { it.copy(isBusy = false, killSwitchEnabled = value, message = if (value) "All active and paused runs were stopped." else "New runs may be started again.") }; refresh() },
                onFailure = { error -> mutableState.update { it.copy(isBusy = false, errorMessage = error.customerMessage("change the emergency stop")) } },
            )
        }
    }

    private fun fail(message: String) { mutableState.update { it.copy(errorMessage = message) } }

    companion object {
        fun factory(repository: SynexRepository): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = AutomationViewModel(repository) as T
        }
    }
}

private data class Loaded(
    val hasAccount: Boolean,
    val strategies: List<AutomationStrategy>,
    val runs: List<com.synex.core.model.AutomationRun>,
    val markets: List<com.synex.core.model.MarketQuote>,
    val contracts: List<com.synex.core.model.ContractOption>,
    val symbol: String,
    val killSwitch: Boolean,
)
