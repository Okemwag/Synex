package com.synex.feature.markets

import com.synex.core.model.MarketQuote

data class MarketsUiState(
    val isLoading: Boolean = true,
    val markets: List<MarketQuote> = emptyList(),
    val query: String = "",
    val category: String = "All",
    val errorMessage: String? = null,
) {
    val categories get() = listOf("All") + markets.map(MarketQuote::market).distinct()

    val visibleMarkets get() = markets.filter { market ->
        (category == "All" || market.market == category) &&
            (query.isBlank() || market.displayName.contains(query, true) || market.symbol.contains(query, true))
    }
}
