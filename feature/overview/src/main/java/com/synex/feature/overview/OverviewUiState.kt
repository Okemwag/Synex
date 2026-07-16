package com.synex.feature.overview

import com.synex.core.model.OverviewSnapshot

data class OverviewUiState(
    val isLoading: Boolean = true,
    val snapshot: OverviewSnapshot? = null,
    val errorMessage: String? = null,
)
