package com.synex.feature.overview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.synex.core.data.SynexRepository
import com.synex.core.model.OverviewSnapshot
import com.synex.core.ui.ErrorState
import com.synex.core.ui.LoadingState
import com.synex.core.ui.MarketRow
import com.synex.core.ui.PageHeading
import com.synex.core.ui.SectionHeading
import com.synex.core.ui.SynexPaper

@Composable
fun OverviewRoute(
    repository: SynexRepository,
    onMarkets: () -> Unit,
    onPortfolio: () -> Unit,
    onAccount: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OverviewViewModel = viewModel(factory = OverviewViewModel.factory(repository)),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    OverviewScreen(state, viewModel::refresh, onMarkets, onPortfolio, onAccount, modifier)
}

@Composable
fun OverviewScreen(
    state: OverviewUiState,
    onRetry: () -> Unit,
    onMarkets: () -> Unit,
    onPortfolio: () -> Unit,
    onAccount: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize().background(SynexPaper),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item { PageHeading("Portfolio overview", "Account overview") }
        when {
            state.isLoading -> item { LoadingState() }
            state.errorMessage != null -> item { ErrorState(state.errorMessage, onRetry) }
            state.snapshot != null -> overviewContent(state.snapshot, onMarkets, onPortfolio, onAccount)
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.overviewContent(
    snapshot: OverviewSnapshot,
    onMarkets: () -> Unit,
    onPortfolio: () -> Unit,
    onAccount: () -> Unit,
) {
    item { BalanceCard(snapshot) }
    item { QuickActions(onMarkets, onPortfolio, onAccount) }
    item { SectionHeading("Markets", "Live watchlist", Modifier.padding(top = 4.dp)) }
    items(snapshot.watchlist, key = { it.symbol }) { MarketRow(it) }
    item { Spacer(Modifier.height(8.dp)) }
}
