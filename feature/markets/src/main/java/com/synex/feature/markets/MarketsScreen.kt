package com.synex.feature.markets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.synex.core.data.SynexRepository
import com.synex.core.ui.ErrorState
import com.synex.core.ui.LoadingState
import com.synex.core.ui.MarketRow
import com.synex.core.ui.PageHeading
import com.synex.core.ui.SynexMuted
import com.synex.core.ui.SynexPaper

@Composable
fun MarketsRoute(
    repository: SynexRepository,
    modifier: Modifier = Modifier,
    viewModel: MarketsViewModel = viewModel(factory = MarketsViewModel.factory(repository)),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    MarketsScreen(
        state,
        viewModel::setQuery,
        viewModel::setCategory,
        viewModel::selectMarket,
        viewModel::closeMarketHistory,
        viewModel::refresh,
        modifier,
    )
}

@Composable
fun MarketsScreen(
    state: MarketsUiState,
    onQueryChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onMarketSelected: (com.synex.core.model.MarketQuote) -> Unit,
    onHistoryClosed: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().background(SynexPaper).padding(top = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        PageHeading("Follow global markets", "Markets", Modifier.padding(horizontal = 20.dp))
        MarketSearch(state.query, onQueryChange)
        CategoryFilters(state.categories, state.category, onCategoryChange)
        when {
            state.isLoading -> LoadingState(Modifier.padding(horizontal = 20.dp))
            state.errorMessage != null -> ErrorState(state.errorMessage, onRetry, Modifier.padding(horizontal = 20.dp))
            else -> MarketList(state, onMarketSelected, onHistoryClosed)
        }
    }
}

@Composable
private fun MarketList(
    state: MarketsUiState,
    onMarketSelected: (com.synex.core.model.MarketQuote) -> Unit,
    onHistoryClosed: () -> Unit,
) {
    LazyColumn(contentPadding = PaddingValues(horizontal = 20.dp, vertical = 2.dp)) {
        item {
            Text(
                "${state.visibleMarkets.size} instruments",
                style = MaterialTheme.typography.labelMedium,
                color = SynexMuted,
                modifier = Modifier.padding(vertical = 8.dp),
            )
        }
        state.selectedMarket?.let { market ->
            item(key = "history-${market.symbol}") {
                MarketHistoryCard(
                    market = market,
                    candles = state.candles,
                    isLoading = state.isHistoryLoading,
                    errorMessage = state.historyErrorMessage,
                    onClose = onHistoryClosed,
                    modifier = Modifier.padding(bottom = 12.dp),
                )
            }
        }
        items(state.visibleMarkets, key = { it.symbol }) { market ->
            MarketRow(market, onClick = { onMarketSelected(market) })
        }
    }
}
