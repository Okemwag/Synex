package com.synex.feature.account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Gavel
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.synex.core.data.SynexRepository
import com.synex.core.ui.ErrorState
import com.synex.core.ui.LoadingState
import com.synex.core.ui.PageHeading
import com.synex.core.ui.SectionHeading
import com.synex.core.ui.SynexPaper

@Composable
fun AccountRoute(
    repository: SynexRepository,
    onAuthenticationAction: () -> Unit,
    onLegalClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AccountViewModel = viewModel(factory = AccountViewModel.factory(repository)),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    AccountScreen(
        state,
        onAuthenticationAction,
        onLegalClick,
        viewModel::selectAccount,
        viewModel::refresh,
        modifier,
    )
}

@Composable
fun AccountScreen(
    state: AccountUiState,
    onAuthenticationAction: () -> Unit,
    onLegalClick: () -> Unit,
    onAccountSelected: (String) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize().background(SynexPaper),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item { PageHeading("Trading access and policies", "Account") }
        when {
            state.isLoading -> item { LoadingState() }
            state.errorMessage != null -> item { ErrorState(state.errorMessage, onRetry) }
            else -> {
                val selected = state.accounts.firstOrNull { it.loginId == state.selectedLoginId }
                    ?: state.accounts.firstOrNull()
                item { ProfileCard(selected, onAuthenticationAction) }
                if (state.accounts.isNotEmpty()) {
                    item { SectionHeading("Trading account", "Choose the account shown across Synex") }
                    item { AccountPicker(state.accounts, selected?.loginId, onAccountSelected) }
                }
                item { SectionHeading("Policies and disclosures") }
                item { SettingsCard(legalItems(onLegalClick)) }
            }
        }
    }
}

private fun legalItems(onLegalClick: () -> Unit) = listOf(
    SettingItem(Icons.Outlined.Gavel, "Legal and privacy", "Policies, risks, and data rights", onLegalClick),
)
