package com.synex.feature.overview

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

class OverviewViewModel(
    private val repository: SynexRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(OverviewUiState())
    val state: StateFlow<OverviewUiState> = _state.asStateFlow()

    init {
        refresh()
        viewModelScope.launch {
            repository.activeLoginId.drop(1).collect { refresh() }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            _state.value = runCatching { repository.overview() }
                .fold(
                    onSuccess = { OverviewUiState(isLoading = false, snapshot = it) },
                    onFailure = {
                        OverviewUiState(
                            isLoading = false,
                            requiresDerivAccount = it is DerivAccountRequiredException,
                            errorMessage = it.customerMessage("load your account overview"),
                        )
                    },
                )
        }
    }

    companion object {
        fun factory(repository: SynexRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    OverviewViewModel(repository) as T
            }
    }
}
