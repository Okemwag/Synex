package com.synex.feature.portfolio

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.synex.core.data.SynexRepository
import com.synex.core.model.PortfolioSummary
import com.synex.core.ui.ErrorState
import com.synex.core.ui.ActionState
import com.synex.core.ui.LoadingState
import com.synex.core.ui.PageHeading
import com.synex.core.ui.SectionHeading
import com.synex.core.ui.SynexPaper

@Composable
fun PortfolioRoute(
    repository: SynexRepository,
    onAccount: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PortfolioViewModel = viewModel(factory = PortfolioViewModel.factory(repository)),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    PortfolioScreen(state, viewModel::refresh, onAccount, modifier)
}

@Composable
fun PortfolioScreen(
    state: PortfolioUiState,
    onRetry: () -> Unit,
    onAccount: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize().background(SynexPaper),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item { PageHeading("Your live exposure", "Portfolio") }
        when {
            state.isLoading -> item { LoadingState() }
            state.requiresDerivAccount -> item {
                ActionState(
                    title = "Connect your Deriv account",
                    message = "Your positions and portfolio will appear here after an account is linked.",
                    actionLabel = "Connect Deriv account",
                    onAction = onAccount,
                )
            }
            state.errorMessage != null -> item { ErrorState(state.errorMessage, onRetry) }
            state.portfolio != null -> portfolioContent(state.portfolio)
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.portfolioContent(portfolio: PortfolioSummary) {
    item { PortfolioSummaryCard(portfolio) }
    item { SectionHeading("Open positions", "${portfolio.positions.size} active") }
    items(portfolio.positions, key = { it.contractId }) { PositionCard(it) }
}
