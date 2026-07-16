package com.synex.feature.portfolio

import com.synex.core.model.PortfolioSummary

data class PortfolioUiState(
    val isLoading: Boolean = true,
    val portfolio: PortfolioSummary? = null,
    val errorMessage: String? = null,
)
