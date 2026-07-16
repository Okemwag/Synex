package com.synex.feature.markets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.synex.core.data.SynexRepository
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

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { repository.markets() }
                .onSuccess { markets -> _state.update { it.copy(isLoading = false, markets = markets) } }
                .onFailure { error ->
                    _state.update {
                        it.copy(isLoading = false, errorMessage = error.message ?: "Unable to load markets.")
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
