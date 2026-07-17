package com.synex.feature.portfolio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.synex.core.data.SynexRepository
import com.synex.core.data.DerivAccountRequiredException
import com.synex.core.model.AccountBalanceUpdate
import com.synex.core.model.AccountConnectionUpdate
import com.synex.core.model.AccountPositionUpdate
import com.synex.core.model.AccountUpdate
import com.synex.core.model.PortfolioSummary
import com.synex.core.model.Position
import com.synex.core.model.PositionDirection
import com.synex.core.ui.customerMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PortfolioViewModel(private val repository: SynexRepository) : ViewModel() {
    private val _state = MutableStateFlow(PortfolioUiState())
    val state: StateFlow<PortfolioUiState> = _state.asStateFlow()

    init {
        refresh()
        viewModelScope.launch {
            repository.activeLoginId.drop(1).collect { refresh() }
        }
        viewModelScope.launch {
            repository.activeLoginId
                .filterNotNull()
                .distinctUntilChanged()
                .collectLatest { loginId ->
                    repository.accountUpdates(loginId).collect(::applyAccountUpdate)
                }
        }
    }

    private fun applyAccountUpdate(update: AccountUpdate) {
        _state.update { current ->
            val portfolio = current.portfolio
            when (update) {
                is AccountConnectionUpdate -> current.copy(liveStatus = update.status)
                is AccountBalanceUpdate -> if (portfolio == null) current else current.copy(
                    portfolio = portfolio.revalue(availableCash = update.amount),
                    liveStatus = "connected",
                )
                is AccountPositionUpdate -> if (portfolio == null) current else {
                    val positions = if (update.isExpired || update.isSold) {
                        portfolio.positions.filterNot { it.contractId == update.contractId }
                    } else {
                        val livePosition = update.toPosition()
                        if (portfolio.positions.any { it.contractId == update.contractId }) {
                            portfolio.positions.map { existing ->
                                if (existing.contractId == update.contractId) {
                                    livePosition.copy(displayName = existing.displayName.ifBlank { livePosition.displayName })
                                } else {
                                    existing
                                }
                            }
                        } else {
                            portfolio.positions + livePosition
                        }
                    }
                    current.copy(
                        portfolio = portfolio.copy(positions = positions).revalue(),
                        liveStatus = "connected",
                    )
                }
            }
        }
    }

    private fun AccountPositionUpdate.toPosition() = Position(
        contractId = contractId,
        symbol = symbol,
        displayName = symbol,
        direction = if (contractType.contains("PUT", ignoreCase = true) || contractType.contains("DOWN", ignoreCase = true)) {
            PositionDirection.DOWN
        } else {
            PositionDirection.UP
        },
        buyPrice = buyPrice,
        currentValue = buyPrice + profit,
        profitLoss = profit,
        currency = currency,
        purchaseEpochSeconds = 0,
    )

    private fun PortfolioSummary.revalue(availableCash: Double = this.availableCash): PortfolioSummary {
        val completelyValued = positions.all { it.currentValue != null && it.profitLoss != null }
        return copy(
            availableCash = availableCash,
            equity = if (completelyValued) availableCash + positions.sumOf { it.currentValue ?: 0.0 } else null,
            profitLoss = if (completelyValued) positions.sumOf { it.profitLoss ?: 0.0 } else null,
        )
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = PortfolioUiState(isLoading = true)
            _state.value = runCatching { repository.portfolio() }.fold(
                onSuccess = { PortfolioUiState(isLoading = false, portfolio = it) },
                onFailure = {
                    PortfolioUiState(
                        isLoading = false,
                        requiresDerivAccount = it is DerivAccountRequiredException,
                        errorMessage = it.customerMessage("load your portfolio"),
                    )
                },
            )
        }
    }

    companion object {
        fun factory(repository: SynexRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T = PortfolioViewModel(repository) as T
            }
    }
}
