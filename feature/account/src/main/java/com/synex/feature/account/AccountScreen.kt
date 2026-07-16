package com.synex.feature.account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Gavel
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
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
    val uriHandler = LocalUriHandler.current
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(state.connectionUrl) {
        state.connectionUrl?.let { url ->
            runCatching { uriHandler.openUri(url) }.fold(
                onSuccess = { viewModel.onBrowserOpened() },
                onFailure = { viewModel.onBrowserLaunchFailed() },
            )
        }
    }

    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.onAppResumed()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    AccountScreen(
        state,
        onAuthenticationAction,
        onLegalClick,
        viewModel::selectAccount,
        viewModel::connectDeriv,
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
    onConnectDeriv: () -> Unit,
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
                item {
                    ProfileCard(
                        account = selected,
                        isConnecting = state.isConnecting,
                        connectionMessage = state.connectionMessage,
                        onConnectDeriv = onConnectDeriv,
                        onAuthenticationAction = onAuthenticationAction,
                    )
                }
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
