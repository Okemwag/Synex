package com.synex.feature.funding

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.synex.core.data.SynexRepository
import com.synex.core.model.DerivWallet
import com.synex.core.model.PaymentAgent
import com.synex.core.model.WalletTransaction
import com.synex.core.ui.ActionState
import com.synex.core.ui.ErrorState
import com.synex.core.ui.LoadingState
import com.synex.core.ui.PageHeading
import com.synex.core.ui.SectionHeading
import com.synex.core.ui.SynexCard
import com.synex.core.ui.SynexGreen
import com.synex.core.ui.SynexMuted
import com.synex.core.ui.SynexPaper
import com.synex.core.ui.SynexRed

@Composable
fun FundingRoute(
    repository: SynexRepository,
    onAccount: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FundingViewModel = viewModel(factory = FundingViewModel.factory(repository)),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    FundingScreen(state, viewModel, onAccount, modifier)
}

@Composable
private fun FundingScreen(state: FundingUiState, actions: FundingViewModel, onAccount: () -> Unit, modifier: Modifier) {
    if (state.confirmWithdrawal) AlertDialog(
        onDismissRequest = actions::dismissWithdrawalConfirmation,
        title = { Text("Confirm payment-agent withdrawal") },
        text = { Text("Withdraw ${state.amount} ${state.currency} through the selected independent payment agent? Confirm their identity, fees, exchange rate, and settlement method first.") },
        confirmButton = { TextButton(onClick = actions::confirmWithdrawal) { Text("Submit withdrawal") } },
        dismissButton = { TextButton(onClick = actions::dismissWithdrawalConfirmation) { Text("Cancel") } },
    )
    state.confirmTransferRequestId?.let { AlertDialog(
        onDismissRequest = actions::dismissTransferConfirmation,
        title = { Text("Confirm payment-agent transfer") },
        text = { Text("Validation passed. Transfer ${state.transferAmount} ${state.currency} to ${state.recipientNickname}?") },
        confirmButton = { TextButton(onClick = actions::confirmTransfer) { Text("Transfer") } },
        dismissButton = { TextButton(onClick = actions::dismissTransferConfirmation) { Text("Cancel") } },
    ) }

    LazyColumn(
        modifier = modifier.fillMaxSize().background(SynexPaper),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item { PageHeading("Deriv wallets and verified money movement", "Funding") }
        when {
            state.isLoading -> item { LoadingState() }
            state.errorMessage != null && state.capabilities == null -> item { ErrorState(state.errorMessage, actions::refresh) }
            state.capabilities?.connected == false -> item { ActionState("Connect Deriv first", "Your wallets and payment permissions belong to your Deriv profile.", "Open account", onAccount) }
            state.capabilities?.paymentEnabled == false -> item { ActionState("Approve payment access", "Reconnect your existing Deriv link once and approve the payment permission. Synex never receives your Deriv password.", "Reconnect Deriv", onAccount) }
            else -> {
                state.errorMessage?.let { item { Notice(it, danger = true) } }
                state.message?.let { item { Notice(it) } }
                item { SectionHeading("Wallets", "Balances remain held by Deriv") }
                if (state.wallets.isEmpty()) item { Notice("No Deriv wallets are available for this profile yet.") }
                items(state.wallets, key = { it.walletId }) { wallet -> WalletCard(wallet, wallet.type == state.selectedWalletType) { actions.selectWallet(wallet.type) } }

                item { SectionHeading("Wallet transactions", state.selectedWalletType?.replace('_', ' ') ?: "Choose a wallet") }
                if (state.transactions.isEmpty()) item { Notice("No transactions are present in this wallet.") }
                items(state.transactions, key = { "${it.requestId}-${it.transactionId}" }) { TransactionCard(it) }
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        OutlinedButton(onClick = actions::previousWalletPage, enabled = state.previousCursor != null && !state.isBusy) { Text("Previous") }
                        OutlinedButton(onClick = actions::nextWalletPage, enabled = state.nextCursor != null && !state.isBusy) { Text("Next") }
                    }
                }

                item { SectionHeading("Payment agents", "Only agents returned by Deriv are shown") }
                item {
                    SynexCard {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(state.currency, actions::setCurrency, label = { Text("Currency") }, singleLine = true, supportingText = { Text(state.currencies.take(8).joinToString(" · ")) }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(state.country, actions::setCountry, label = { Text("Country code (optional)") }, singleLine = true, supportingText = { Text(state.countries.take(10).joinToString(" · ")) }, modifier = Modifier.fillMaxWidth())
                            Button(onClick = actions::searchAgents, enabled = !state.isBusy) { Text("Find agents") }
                        }
                    }
                }
                if (state.agents.isEmpty()) item { Notice("No payment agents match these filters.") }
                items(state.agents, key = { it.id }) { agent -> AgentCard(agent, state.selectedAgentId == agent.id) { actions.selectAgent(agent.id) } }

                item { WithdrawalCard(state, actions) }
                state.settings?.let { settings -> item {
                    SynexCard {
                        Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(Modifier.weight(1f)) {
                                Text("Payment-agent privacy", style = MaterialTheme.typography.titleMedium)
                                Text("Deposits ${if (settings.depositEnabled) "enabled" else "disabled"} · withdrawals ${if (settings.withdrawEnabled) "enabled" else "disabled"}", style = MaterialTheme.typography.bodySmall, color = SynexMuted)
                                Text("Share my real name with agents", style = MaterialTheme.typography.bodyMedium)
                            }
                            Switch(settings.showRealName, actions::updatePrivacy, enabled = !state.isBusy)
                        }
                    }
                } }

                state.ownAgent?.let { item { AgentTransferCard(state, actions, it) } }
                state.pendingOperation?.let { operation -> item { Notice("${operation.status.replaceFirstChar(Char::uppercase)} · reference ${operation.requestId}${operation.transactionId?.let { " · transaction $it" }.orEmpty()}") } }
                item { Notice("Payment agents are independent third parties. Synex cannot reverse cash, bank, mobile-money, or other settlement completed outside Deriv. Verify the agent before proceeding.") }
            }
        }
    }
}

@Composable
private fun WalletCard(wallet: DerivWallet, selected: Boolean, onSelect: () -> Unit) {
    SynexCard(Modifier.fillMaxWidth().clickable(onClick = onSelect)) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(wallet.type.replace('_', ' ').replaceFirstChar(Char::uppercase), style = MaterialTheme.typography.titleMedium)
                if (selected) Text("Selected", color = SynexGreen, style = MaterialTheme.typography.labelMedium)
            }
            wallet.balances.forEach { (currency, balance) ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(currency, color = SynexMuted)
                    Text(balance.balance)
                }
            }
            wallet.approximateTotal?.let { Text("Approximately $it ${wallet.totalCurrency.orEmpty()}", style = MaterialTheme.typography.bodySmall, color = SynexMuted) }
        }
    }
}

@Composable
private fun TransactionCard(transaction: WalletTransaction) {
    SynexCard {
        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) {
                Text("${transaction.category.replaceFirstChar(Char::uppercase)} · ${transaction.channel.replace('_', ' ')}", style = MaterialTheme.typography.titleSmall)
                Text("${transaction.timestamp} · #${transaction.transactionId}", style = MaterialTheme.typography.bodySmall, color = SynexMuted)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${if (transaction.category == "deposit") "+" else "−"}${transaction.amount} ${transaction.currency}", color = if (transaction.category == "deposit") SynexGreen else SynexRed)
                Text(transaction.status, style = MaterialTheme.typography.labelSmall, color = SynexMuted)
            }
        }
    }
}

@Composable
private fun AgentCard(agent: PaymentAgent, selected: Boolean, onSelect: () -> Unit) {
    SynexCard(Modifier.fillMaxWidth().clickable(onClick = onSelect)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.Top) {
            RadioButton(selected, onSelect)
            Column(Modifier.weight(1f)) {
                Text(agent.name.ifBlank { agent.nickname.ifBlank { "Agent #${agent.id}" } }, style = MaterialTheme.typography.titleMedium)
                Text(agent.paymentMethods.joinToString(" · ").ifBlank { agent.information.ifBlank { "Contact agent for settlement methods" } }, style = MaterialTheme.typography.bodySmall, color = SynexMuted)
                Text("Withdraw ${agent.withdrawalMinimum ?: "—"}–${agent.withdrawalMaximum ?: "—"} · commission ${agent.withdrawalCommission ?: "—"}%", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun WithdrawalCard(state: FundingUiState, actions: FundingViewModel) {
    SynexCard {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Withdraw through agent", style = MaterialTheme.typography.titleMedium)
            Text("Deriv moves funds to the selected agent; the agent settles with you separately.", style = MaterialTheme.typography.bodySmall, color = SynexMuted)
            OutlinedTextField(state.amount, actions::setAmount, label = { Text("Amount (${state.currency})") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedButton(onClick = actions::requestWithdrawalCode, enabled = !state.isBusy && state.settings?.withdrawEnabled == true, modifier = Modifier.fillMaxWidth()) { Text("Send Deriv verification code") }
            if (state.verificationExpiresAt > System.currentTimeMillis() / 1000) {
                OutlinedTextField(state.verificationCode, actions::setVerificationCode, label = { Text("Six-digit code") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
            Button(onClick = actions::askToWithdraw, enabled = !state.isBusy && state.verificationCode.length == 6 && state.settings?.withdrawEnabled == true, modifier = Modifier.fillMaxWidth()) { Text("Submit withdrawal") }
        }
    }
}

@Composable
private fun AgentTransferCard(state: FundingUiState, actions: FundingViewModel, agent: PaymentAgent) {
    SynexCard {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Payment-agent deposit", style = MaterialTheme.typography.titleMedium)
            Text("Deriv recognises this profile as payment agent #${agent.id}. A dry run validates every transfer before confirmation.", style = MaterialTheme.typography.bodySmall, color = SynexMuted)
            OutlinedTextField(state.recipientNickname, actions::setRecipientNickname, label = { Text("Recipient nickname") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(state.transferAmount, actions::setTransferAmount, label = { Text("Amount (${state.currency})") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Button(onClick = actions::validateTransfer, enabled = !state.isBusy, modifier = Modifier.fillMaxWidth()) { Text("Validate and transfer") }
        }
    }
}

@Composable
private fun Notice(message: String, danger: Boolean = false) {
    SynexCard { Text(message, Modifier.padding(18.dp), color = if (danger) SynexRed else SynexMuted, style = MaterialTheme.typography.bodyMedium) }
}
