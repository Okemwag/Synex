package com.synex.feature.markets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.synex.core.data.SynexRepository
import com.synex.core.model.MarketQuote
import com.synex.core.ui.customerMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MarketsViewModel(private val repository: SynexRepository) : ViewModel() {
    private val _state = MutableStateFlow(MarketsUiState())
    val state: StateFlow<MarketsUiState> = _state.asStateFlow()

    init { refresh() }

    fun setQuery(query: String) = _state.update { it.copy(query = query) }

    fun setCategory(category: String) = _state.update { it.copy(category = category) }

    fun selectMarket(market: MarketQuote) {
        _state.update {
            it.copy(
                selectedMarket = market,
                candles = emptyList(),
                isHistoryLoading = true,
                historyErrorMessage = null,
            )
        }
        viewModelScope.launch {
            runCatching { repository.candles(market.symbol) }
                .onSuccess { candles ->
                    _state.update { current ->
                        if (current.selectedMarket?.symbol != market.symbol) current else current.copy(
                            candles = candles.sortedBy { it.epochSeconds },
                            isHistoryLoading = false,
                        )
                    }
                }
                .onFailure { error ->
                    _state.update { current ->
                        if (current.selectedMarket?.symbol != market.symbol) current else current.copy(
                            isHistoryLoading = false,
                            historyErrorMessage = error.customerMessage("load market history"),
                        )
                    }
                }
        }
    }

    fun closeMarketHistory() {
        _state.update {
            it.copy(
                selectedMarket = null,
                candles = emptyList(),
                isHistoryLoading = false,
                historyErrorMessage = null,
            )
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { repository.markets() }
                .onSuccess { markets -> _state.update { it.copy(isLoading = false, markets = markets) } }
                .onFailure { error ->
                    _state.update {
                        it.copy(isLoading = false, errorMessage = error.customerMessage("load markets"))
                    }
                }
        }
    }

    companion object {
        fun factory(repository: SynexRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T = MarketsViewModel(repository) as T
            }
    }
}
