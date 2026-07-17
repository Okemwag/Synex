package com.synex.feature.portfolio

import com.synex.core.model.PortfolioSummary

data class PortfolioUiState(
    val isLoading: Boolean = true,
    val portfolio: PortfolioSummary? = null,
    val requiresDerivAccount: Boolean = false,
    val errorMessage: String? = null,
    val liveStatus: String = "connecting",
)
