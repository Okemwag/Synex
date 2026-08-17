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
        loadAccounts(afterDerivAuthorization = false)
    }

    fun connectDeriv() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isConnecting = true,
                    connectionMessage = null,
                    errorMessage = null,
                )
            }
            runCatching { repository.derivConnectUrl() }.fold(
                onSuccess = { url ->
                    _state.update { it.copy(isConnecting = false, connectionUrl = url) }
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(
                            isConnecting = false,
                            connectionMessage = error.customerMessage("start the Deriv connection"),
                        )
                    }
                },
            )
        }
    }

    fun onBrowserOpened() {
        _state.update {
            it.copy(
                connectionUrl = null,
                waitingForConnection = true,
                connectionMessage = "Finish connecting in Deriv, then return to Synex. If the final browser page does not load, return here anyway.",
            )
        }
    }

    fun onBrowserLaunchFailed() {
        _state.update {
            it.copy(
                connectionUrl = null,
                waitingForConnection = false,
                connectionMessage = "We couldn't open Deriv. Check that a browser is installed and try again.",
            )
        }
    }

    fun onAppResumed() {
        if (_state.value.waitingForConnection) {
            loadAccounts(afterDerivAuthorization = true)
        }
    }

    fun selectAccount(loginId: String) {
        repository.selectAccount(loginId)
        _state.update { it.copy(selectedLoginId = loginId) }
    }

    fun createOptionsAccount(accountType: String, realMoneyConfirmed: Boolean) {
        viewModelScope.launch {
            _state.update { it.copy(isManagingAccount = true, errorMessage = null, connectionMessage = null) }
            runCatching { repository.createOptionsAccount(accountType, realMoneyConfirmed) }.fold(
                onSuccess = {
                    _state.update { it.copy(isManagingAccount = false, connectionMessage = if (accountType == "demo") "Practice Options account created." else "Real Options account created.") }
                    loadAccounts(afterDerivAuthorization = false)
                },
                onFailure = { error ->
                    _state.update { it.copy(isManagingAccount = false, connectionMessage = error.customerMessage("create the Options account")) }
                },
            )
        }
    }

    fun resetDemoBalance(loginId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isManagingAccount = true, errorMessage = null, connectionMessage = null) }
            runCatching { repository.resetDemoBalance(loginId) }.fold(
                onSuccess = {
                    _state.update { it.copy(isManagingAccount = false, connectionMessage = "$loginId's practice balance was reset by Deriv.") }
                    loadAccounts(afterDerivAuthorization = false)
                },
                onFailure = { error ->
                    _state.update { it.copy(isManagingAccount = false, connectionMessage = error.customerMessage("reset the practice balance")) }
                },
            )
        }
    }

    private fun loadAccounts(afterDerivAuthorization: Boolean) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { repository.accounts() }.fold(
                onSuccess = { accounts ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            accounts = accounts,
                            selectedLoginId = repository.activeLoginId.value,
                            waitingForConnection = afterDerivAuthorization && accounts.isEmpty(),
                            connectionMessage = when {
                                !afterDerivAuthorization -> it.connectionMessage
                                accounts.isNotEmpty() -> "Deriv account connected successfully."
                                else -> "No linked account was found yet. Finish the Deriv authorization, then return and try again."
                            },
                        )
                    }
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.customerMessage("load your accounts"),
                        )
                    }
                },
            )
        }
    }

    companion object {
        fun factory(repository: SynexRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T = AccountViewModel(repository) as T
            }
    }
}
