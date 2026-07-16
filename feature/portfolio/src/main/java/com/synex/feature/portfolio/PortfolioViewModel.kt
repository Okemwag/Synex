package com.synex.feature.portfolio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.synex.core.data.SynexRepository
import com.synex.core.data.DerivAccountRequiredException
import com.synex.core.ui.customerMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch

class PortfolioViewModel(private val repository: SynexRepository) : ViewModel() {
    private val _state = MutableStateFlow(PortfolioUiState())
    val state: StateFlow<PortfolioUiState> = _state.asStateFlow()

    init {
        refresh()
        viewModelScope.launch {
            repository.activeLoginId.drop(1).collect { refresh() }
        }
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
