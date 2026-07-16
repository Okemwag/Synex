package com.synex.feature.auth

data class AuthUiState(
    val configured: Boolean,
    val authenticated: Boolean,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)
