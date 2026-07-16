package com.synex.feature.account

import com.synex.core.model.TradingAccount

data class AccountUiState(
    val isLoading: Boolean = true,
    val accounts: List<TradingAccount> = emptyList(),
    val errorMessage: String? = null,
)
