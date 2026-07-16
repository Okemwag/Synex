package com.synex.feature.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.synex.core.data.SynexRepository
import com.synex.core.ui.customerMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AccountViewModel(private val repository: SynexRepository) : ViewModel() {
    private val _state = MutableStateFlow(AccountUiState())
    val state: StateFlow<AccountUiState> = _state.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _state.value = AccountUiState(isLoading = true)
            _state.value = runCatching { repository.accounts() }.fold(
                onSuccess = {
                    AccountUiState(
                        isLoading = false,
                        accounts = it,
                        selectedLoginId = repository.activeLoginId.value,
                    )
                },
                onFailure = { AccountUiState(isLoading = false, errorMessage = it.customerMessage("load your accounts")) },
            )
        }
    }

    fun selectAccount(loginId: String) {
        repository.selectAccount(loginId)
        _state.update { it.copy(selectedLoginId = loginId) }
    }

    companion object {
        fun factory(repository: SynexRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T = AccountViewModel(repository) as T
            }
    }
}
