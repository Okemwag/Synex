package com.synex.feature.account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Gavel
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    onFundingClick: () -> Unit,
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
        viewModel::createOptionsAccount,
        viewModel::resetDemoBalance,
        onFundingClick,
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
    onCreateAccount: (String, Boolean) -> Unit,
    onResetDemo: (String) -> Unit,
    onFundingClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var confirmRealCreation by remember { mutableStateOf(false) }
    var resetLoginId by remember { mutableStateOf<String?>(null) }
    if (confirmRealCreation) AlertDialog(
        onDismissRequest = { confirmRealCreation = false },
        title = { Text("Create a real-money account?") },
        text = { Text("Real Options trading can lose money. Creating the account does not bypass Synex onboarding, acknowledgement, or trading limits.") },
        confirmButton = { TextButton(onClick = { confirmRealCreation = false; onCreateAccount("real", true) }) { Text("Create real account") } },
        dismissButton = { TextButton(onClick = { confirmRealCreation = false }) { Text("Cancel") } },
    )
    resetLoginId?.let { loginId -> AlertDialog(
        onDismissRequest = { resetLoginId = null },
        title = { Text("Reset practice balance?") },
        text = { Text("Deriv will reset $loginId. This does not affect any real account.") },
        confirmButton = { TextButton(onClick = { resetLoginId = null; onResetDemo(loginId) }) { Text("Reset balance") } },
        dismissButton = { TextButton(onClick = { resetLoginId = null }) { Text("Cancel") } },
    ) }
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
                    item { AccountPicker(state.accounts, selected?.loginId, onAccountSelected, { resetLoginId = it }, !state.isManagingAccount) }
                    item {
                        com.synex.core.ui.SynexCard {
                            androidx.compose.foundation.layout.Column(
                                Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Text("Options accounts", style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
                                Text("Create USD Options accounts in Deriv's ROW group. Real trading remains locked until setup is complete.")
                                androidx.compose.foundation.layout.Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(onClick = { onCreateAccount("demo", false) }, enabled = !state.isManagingAccount) { Text("Create practice") }
                                    Button(onClick = { confirmRealCreation = true }, enabled = !state.isManagingAccount) { Text("Create real") }
                                }
                                OutlinedButton(onClick = onConnectDeriv, enabled = !state.isConnecting && !state.isManagingAccount) { Text("Reconnect Deriv permissions") }
                            }
                        }
                    }
                    item {
                        com.synex.core.ui.ActionState(
                            "Wallets and payment agents",
                            "Review Deriv wallet balances and transactions, or make a verified payment-agent withdrawal.",
                            "Open funding",
                            onFundingClick,
                        )
                    }
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
