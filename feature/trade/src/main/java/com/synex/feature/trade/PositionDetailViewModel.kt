package com.synex.feature.trade

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.synex.core.data.SynexRepository
import com.synex.core.model.AccountPositionUpdate
import com.synex.core.model.ContractUpdateEvent
import com.synex.core.model.PositionStatus
import com.synex.core.ui.customerMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PositionDetailUiState(
    val isLoading: Boolean = true,
    val isBusy: Boolean = false,
    val position: PositionStatus? = null,
    val history: List<ContractUpdateEvent> = emptyList(),
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val closed: Boolean = false,
)

class PositionDetailViewModel(
    private val repository: SynexRepository,
    private val contractId: Long,
) : ViewModel() {
    private val mutableState = MutableStateFlow(PositionDetailUiState())
    val state: StateFlow<PositionDetailUiState> = mutableState.asStateFlow()

    init {
        refresh()
        viewModelScope.launch {
            repository.activeLoginId.filterNotNull().collectLatest { loginId ->
                repository.accountUpdates(loginId).collect { update ->
                    if (update is AccountPositionUpdate && update.contractId == contractId) {
                        mutableState.update { current ->
                            val existing = current.position
                            current.copy(
                                position = PositionStatus(
                                    contractId = update.contractId,
                                    contractType = update.contractType,
                                    symbol = update.symbol,
                                    longcode = existing?.longcode.orEmpty(),
                                    status = update.status,
                                    buyPrice = update.buyPrice,
                                    currentSpot = update.currentSpot,
                                    profit = update.profit,
                                    profitPercentage = update.profitPercentage,
                                    currency = update.currency,
                                    isExpired = update.isExpired,
                                    isSold = update.isSold,
                                ),
                                closed = update.isExpired || update.isSold,
                            )
                        }
                    }
                }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            mutableState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { repository.position(contractId) to repository.contractUpdateHistory(contractId) }
                .onSuccess { (position, history) ->
                    mutableState.update {
                        it.copy(
                            isLoading = false,
                            position = position,
                            history = history,
                            closed = position.isExpired || position.isSold,
                        )
                    }
                }
                .onFailure { error -> mutableState.update { it.copy(isLoading = false, errorMessage = error.customerMessage("load this position")) } }
        }
    }

    fun sell() = act("close this trade") { repository.sell(contractId) }
    fun cancel() = act("cancel this trade") { repository.cancel(contractId) }

    fun updateLimits(stopLossRaw: String, takeProfitRaw: String) {
        val stopLoss = stopLossRaw.toDoubleOrNull()
        val takeProfit = takeProfitRaw.toDoubleOrNull()
        if (stopLoss == null && takeProfit == null) {
            mutableState.update { it.copy(errorMessage = "Enter a stop-loss or take-profit amount.") }
            return
        }
        if ((stopLoss != null && stopLoss < 0) || (takeProfit != null && takeProfit < 0)) {
            mutableState.update { it.copy(errorMessage = "Protection amounts cannot be negative.") }
            return
        }
        viewModelScope.launch {
            mutableState.update { it.copy(isBusy = true, errorMessage = null, successMessage = null) }
            runCatching { repository.updateContract(contractId, stopLoss, takeProfit) }
                .onSuccess {
                    val history = runCatching { repository.contractUpdateHistory(contractId) }.getOrDefault(emptyList())
                    mutableState.update { it.copy(isBusy = false, history = history, successMessage = "Trade protection updated.") }
                }
                .onFailure { error -> mutableState.update { it.copy(isBusy = false, errorMessage = error.customerMessage("update trade protection")) } }
        }
    }

    private fun act(label: String, block: suspend () -> Unit) {
        viewModelScope.launch {
            mutableState.update { it.copy(isBusy = true, errorMessage = null, successMessage = null) }
            runCatching { block() }
                .onSuccess { mutableState.update { it.copy(isBusy = false, closed = true, successMessage = "Trade instruction accepted by Deriv.") } }
                .onFailure { error -> mutableState.update { it.copy(isBusy = false, errorMessage = error.customerMessage(label)) } }
        }
    }

    companion object {
        fun factory(repository: SynexRepository, contractId: Long): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = PositionDetailViewModel(repository, contractId) as T
        }
    }
}
