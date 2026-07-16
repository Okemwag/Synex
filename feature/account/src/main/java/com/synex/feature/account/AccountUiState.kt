package com.synex.feature.account

import com.synex.core.model.TradingAccount

data class AccountUiState(
    val isLoading: Boolean = true,
    val accounts: List<TradingAccount> = emptyList(),
    val selectedLoginId: String? = null,
    val errorMessage: String? = null,
)
