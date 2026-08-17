package com.synex.feature.trade

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PriceCheck
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.synex.core.data.SynexRepository
import com.synex.core.model.ContractOption
import com.synex.core.ui.ActionState
import com.synex.core.ui.ErrorState
import com.synex.core.ui.LoadingState
import com.synex.core.ui.PageHeading
import com.synex.core.ui.SynexCard
import com.synex.core.ui.SynexGreen
import com.synex.core.ui.SynexInk
import com.synex.core.ui.SynexMuted
import com.synex.core.ui.SynexPaper
import com.synex.core.ui.formatMoney

@Composable
fun TradeRoute(
    repository: SynexRepository,
    onAccount: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TradeViewModel = viewModel(factory = TradeViewModel.factory(repository)),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    TradeScreen(state, viewModel, onAccount, modifier)
}

@Composable
private fun TradeScreen(
    state: TradeUiState,
    actions: TradeViewModel,
    onAccount: () -> Unit,
    modifier: Modifier,
) {
    var showConfirmation by remember { mutableStateOf(false) }
    var realMoneyConfirmed by remember(state.proposal?.id) { mutableStateOf(false) }
    LazyColumn(
        modifier = modifier.fillMaxSize().background(SynexPaper),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { PageHeading("Build and price your contract", "Trade") }
        when {
            state.isLoading -> item { LoadingState() }
            state.accounts.isEmpty() -> item {
                ActionState(
                    "Connect your Deriv account",
                    "A connected Deriv demo or real account is required before a proposal can be priced.",
                    "Connect account",
                    onAccount,
                )
            }
            state.errorMessage != null && state.markets.isEmpty() -> item { ErrorState(state.errorMessage, actions::refresh) }
            else -> {
                item {
                    ChoiceStrip(
                        label = "Account",
                        selected = state.activeAccount?.loginId.orEmpty(),
                        choices = state.accounts.map { it.loginId to "${it.loginId} · ${if (it.isVirtual) "Demo" else "Real"}" },
                        onSelected = actions::selectAccount,
                    )
                }
                item {
                    TradeTicket(state, actions)
                }
                state.proposal?.let { proposal ->
                    item {
                        SynexCard(dark = true) {
                            Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Live Deriv price", style = MaterialTheme.typography.labelLarge, color = SynexMuted)
                                Text(formatMoney(proposal.askPrice, state.activeAccount?.currency ?: "USD"), style = MaterialTheme.typography.displaySmall)
                                proposal.payout?.let { Text("Potential payout ${formatMoney(it, state.activeAccount?.currency ?: "USD")}") }
                                Text(proposal.longcode, style = MaterialTheme.typography.bodyMedium, color = androidx.compose.ui.graphics.Color.White.copy(alpha = .64f))
                                Text(
                                    if (state.quoteSecondsRemaining > 0) "Review within ${state.quoteSecondsRemaining}s" else "Price expired — request a fresh quote",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (state.quoteSecondsRemaining > 0) SynexGreen else androidx.compose.ui.graphics.Color(0xffffa69e),
                                )
                                if (state.activeAccount?.isVirtual == false) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(realMoneyConfirmed, onCheckedChange = { realMoneyConfirmed = it })
                                        Text("I understand this order uses real money that I could lose.", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                                Button(
                                    onClick = { showConfirmation = true },
                                    enabled = !state.isBusy && !state.purchaseLocked && state.quoteSecondsRemaining > 0 && (state.activeAccount?.isVirtual != false || realMoneyConfirmed),
                                    colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color.White, contentColor = SynexInk),
                                    modifier = Modifier.fillMaxWidth(),
                                ) { Text(if (state.activeAccount?.isVirtual == true) "Review demo purchase" else "Review real purchase") }
                            }
                        }
                    }
                }
                state.receipt?.let { receipt ->
                    item {
                        SynexCard {
                            Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Purchase receipt", style = MaterialTheme.typography.titleLarge)
                                Text(receipt.longcode.ifBlank { "${receipt.contractType} on ${receipt.symbol}" }, color = SynexMuted)
                                ReceiptLine("Status", receipt.status)
                                ReceiptLine("Contract ID", receipt.contractId?.toString() ?: "Pending")
                                ReceiptLine("Deriv transaction", receipt.transactionId?.toString() ?: "Pending")
                                ReceiptLine("Paid", formatMoney(receipt.purchasePrice, receipt.currency))
                                ReceiptLine("Maximum loss", formatMoney(receipt.maximumLoss, receipt.currency))
                                ReceiptLine("Potential payout", formatMoney(receipt.potentialPayout, receipt.currency))
                            }
                        }
                    }
                }
                state.errorMessage?.let { item { MessageCard(it, false, actions::clearMessage) } }
                state.successMessage?.let { item { MessageCard(it, true, actions::clearMessage) } }
                item {
                    Text(
                        "Prices and execution come directly from Deriv. Practise on demo first and never stake money you cannot afford to lose.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SynexMuted,
                    )
                }
            }
        }
    }

    if (showConfirmation && state.proposal != null) {
        AlertDialog(
            onDismissRequest = { showConfirmation = false },
            title = { Text(if (state.activeAccount?.isVirtual == true) "Place demo trade?" else "Place real-money trade?") },
            text = {
                Text("Buy ${state.form.contractType} on ${state.form.symbol} for ${formatMoney(state.proposal.askPrice, state.activeAccount?.currency ?: "USD")}. This confirmation submits the order to Deriv.")
            },
            confirmButton = {
                Button(onClick = { showConfirmation = false; actions.purchase(realMoneyConfirmed) }) { Text("Place trade") }
            },
            dismissButton = { TextButton(onClick = { showConfirmation = false }) { Text("Go back") } },
        )
    }
}

@Composable
private fun TradeTicket(state: TradeUiState, actions: TradeViewModel) {
    val form = state.form
    val contract = state.selectedContract
    SynexCard {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("Trade setup", style = MaterialTheme.typography.titleLarge)
            ChoiceMenu("Market", form.symbol, state.markets.map { it.symbol to it.displayName }, actions::selectSymbol)
            ChoiceMenu("Contract", form.contractType, state.contracts.map { it.contractType to "${it.displayName} · ${it.rulesLabel()}" }, actions::selectContract)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TradeField("Amount", form.amount, actions::setAmount, Modifier.weight(1f))
                ChoiceMenu("Basis", form.basis, listOf("stake" to "Stake", "payout" to "Payout"), actions::setBasis, Modifier.weight(1f))
            }
            ChoiceMenu("Expiry", form.expiryMode, listOf("duration" to "Duration", "date" to "Date and time"), actions::setExpiryMode)
            if (form.expiryMode == "duration") {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    TradeField("Duration", form.duration, actions::setDuration, Modifier.weight(1f))
                    ChoiceMenu("Unit", form.durationUnit, listOf("t" to "Ticks", "s" to "Seconds", "m" to "Minutes", "h" to "Hours", "d" to "Days"), actions::setDurationUnit, Modifier.weight(1f))
                }
                if (!contract?.minimumDuration.isNullOrBlank() || !contract?.maximumDuration.isNullOrBlank()) {
                    Text("Available duration: ${contract?.minimumDuration.orEmpty()} to ${contract?.maximumDuration.orEmpty()}", style = MaterialTheme.typography.bodySmall, color = SynexMuted)
                }
            } else {
                TradeField("Expiry (YYYY-MM-DD HH:mm)", form.dateExpiry, actions::setDateExpiry, keyboardType = KeyboardType.Text)
            }
            contract?.let {
                ContractSpecificFields(it, form, actions)
                if (it.minimumStake != null || it.maximumStake != null) {
                    Text("Stake limits: ${it.minimumStake ?: "—"} to ${it.maximumStake ?: "—"}", style = MaterialTheme.typography.bodySmall, color = SynexMuted)
                }
            }
            Button(
                onClick = actions::requestProposal,
                enabled = !state.isBusy && !state.purchaseLocked && form.contractType.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = SynexInk),
            ) {
                androidx.compose.material3.Icon(Icons.Outlined.PriceCheck, null)
                Text(if (state.isBusy) "Loading…" else "Get live price", Modifier.padding(start = 8.dp))
            }
        }
    }
}

@Composable
private fun ContractSpecificFields(contract: ContractOption, form: TradeForm, actions: TradeViewModel) {
    fun visible(name: String) = (contract.requiredFields + contract.optionalFields).any { it == name || it.endsWith(".$name") }
    if (visible("barrier")) TradeField(requiredLabel("Barrier", "barrier", contract), form.barrier, actions::setBarrier, keyboardType = KeyboardType.Text)
    if (visible("barrier2")) TradeField(requiredLabel("Second barrier", "barrier2", contract), form.barrier2, actions::setBarrier2, keyboardType = KeyboardType.Text)
    if (visible("multiplier")) TradeField(requiredLabel("Multiplier", "multiplier", contract), form.multiplier, actions::setMultiplier)
    if (visible("growth_rate")) TradeField(requiredLabel("Growth rate", "growth_rate", contract), form.growthRate, actions::setGrowthRate)
    if (visible("selected_tick")) TradeField(requiredLabel("Selected tick", "selected_tick", contract), form.selectedTick, actions::setSelectedTick)
    if (visible("payout_per_point")) TradeField("Payout per point", form.payoutPerPoint, actions::setPayoutPerPoint)
    if (visible("cancellation")) TradeField(requiredLabel("Cancellation", "cancellation", contract), form.cancellation, actions::setCancellation, keyboardType = KeyboardType.Text)
    if (visible("stop_loss") || visible("take_profit")) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            TradeField("Stop loss", form.stopLoss, actions::setStopLoss, Modifier.weight(1f))
            TradeField("Take profit", form.takeProfit, actions::setTakeProfit, Modifier.weight(1f))
        }
    }
}

@Composable
private fun TradeField(
    label: String,
    value: String,
    onValue: (String) -> Unit,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Decimal,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValue,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
private fun ChoiceMenu(
    label: String,
    selected: String,
    choices: List<Pair<String, String>>,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = SynexMuted)
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text(choices.firstOrNull { it.first == selected }?.second ?: "Choose $label", maxLines = 1)
        }
        DropdownMenu(expanded, onDismissRequest = { expanded = false }) {
            choices.forEach { (value, display) ->
                DropdownMenuItem(text = { Text(display) }, onClick = { expanded = false; onSelected(value) })
            }
        }
    }
}

@Composable
private fun ChoiceStrip(label: String, selected: String, choices: List<Pair<String, String>>, onSelected: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = SynexMuted)
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            choices.forEach { (value, display) ->
                if (value == selected) Button(onClick = { onSelected(value) }) { Text(display) }
                else OutlinedButton(onClick = { onSelected(value) }) { Text(display) }
            }
        }
    }
}

@Composable
private fun ReceiptLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = SynexMuted)
        Text(value, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun MessageCard(message: String, success: Boolean, onDismiss: () -> Unit) {
    SynexCard {
        Row(Modifier.padding(18.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(message, Modifier.weight(1f), color = if (success) SynexGreen else MaterialTheme.colorScheme.error)
            TextButton(onClick = onDismiss) { Text("Dismiss") }
        }
    }
}

private fun ContractOption.rulesLabel() = family.replace('_', ' ').replaceFirstChar(Char::uppercase)
private fun requiredLabel(label: String, name: String, contract: ContractOption) = if (name in contract.requiredFields) "$label *" else label
