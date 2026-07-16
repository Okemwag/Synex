package com.synex.feature.account

import com.synex.core.model.TradingAccount

data class AccountUiState(
    val isLoading: Boolean = true,
    val isConnecting: Boolean = false,
    val accounts: List<TradingAccount> = emptyList(),
    val selectedLoginId: String? = null,
    val connectionUrl: String? = null,
    val waitingForConnection: Boolean = false,
    val connectionMessage: String? = null,
    val errorMessage: String? = null,
)
