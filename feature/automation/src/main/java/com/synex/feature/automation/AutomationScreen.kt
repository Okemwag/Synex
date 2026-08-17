package com.synex.feature.automation

import androidx.compose.foundation.background
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.synex.core.data.SynexRepository
import com.synex.core.model.AutomationRun
import com.synex.core.model.AutomationStrategy
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
fun AutomationRoute(
    repository: SynexRepository,
    onAccount: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AutomationViewModel = viewModel(factory = AutomationViewModel.factory(repository)),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    AutomationScreen(state, viewModel, onAccount, modifier)
}

@Composable
private fun AutomationScreen(state: AutomationUiState, actions: AutomationViewModel, onAccount: () -> Unit, modifier: Modifier) {
    state.pendingRealStrategy?.let { strategy -> AlertDialog(
        onDismissRequest = actions::dismissRealConfirmation,
        title = { Text("Start repeated real-money trades?") },
        text = { Text("${strategy.name} may place up to ${strategy.maxTrades} trades and commit up to ${strategy.maxLoss} ${strategy.currency} of loss budget. Account-level limits still apply.") },
        confirmButton = { TextButton(onClick = actions::confirmRealStart) { Text("Start real automation") } },
        dismissButton = { TextButton(onClick = actions::dismissRealConfirmation) { Text("Cancel") } },
    ) }
    if (state.confirmEmergencyStop) AlertDialog(
        onDismissRequest = actions::dismissEmergencyStop,
        title = { Text("Emergency stop all automation?") },
        text = { Text("Every active and paused run will be stopped permanently. An order already sent to Deriv may still finish.") },
        confirmButton = { TextButton(onClick = actions::toggleKillSwitch) { Text("Stop everything") } },
        dismissButton = { TextButton(onClick = actions::dismissEmergencyStop) { Text("Cancel") } },
    )
    LazyColumn(
        modifier = modifier.fillMaxSize().background(SynexPaper),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item { PageHeading("Bounded strategies that survive app restarts", "Automation") }
        if (!state.hasAccount && !state.isLoading) {
            item { ActionState("Connect Deriv first", "Automation needs a selected Deriv Options account.", "Open account", onAccount) }
            return@LazyColumn
        }
        if (state.isLoading) item { LoadingState() }
        state.errorMessage?.let { item { ErrorState(it, actions::refresh) } }
        state.message?.let { item { Notice(it) } }
        item {
            if (state.killSwitchEnabled) Button(onClick = actions::toggleKillSwitch, enabled = !state.isBusy) { Text("Enable new automation") }
            else Button(onClick = actions::askEmergencyStop, enabled = !state.isBusy) { Text("Emergency stop", color = androidx.compose.ui.graphics.Color.White) }
        }
        if (state.killSwitchEnabled) item { Notice("Emergency stop is active. New runs and resumes are blocked.", danger = true) }

        item { SectionHeading("New strategy", "Use a practice account first") }
        item { StrategyForm(state, actions) }
        item { SectionHeading("Strategies", "Fresh Deriv proposals are requested before every trade") }
        if (state.strategies.isEmpty()) item { Notice("No strategies yet.") }
        items(state.strategies, key = { it.id }) { strategy -> StrategyCard(strategy, state.isBusy || state.killSwitchEnabled, actions::askToStart) }
        item { SectionHeading("Runs", "Status refreshes while this screen is open") }
        if (state.runs.isEmpty()) item { Notice("No automation runs yet.") }
        items(state.runs, key = { it.id }) { run -> RunCard(run, state.isBusy || state.killSwitchEnabled, actions::transition) }
        item { Notice("Automation does not guarantee profit. Synex stops on configured limits, changed onboarding, unresolved orders, or repeated Deriv failures, but an in-flight order may finish after you tap stop.") }
    }
}

@Composable
private fun StrategyForm(state: AutomationUiState, actions: AutomationViewModel) {
    SynexCard {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(state.name, actions::setName, label = { Text("Strategy name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            MenuField("Market", state.symbol, state.markets.map { it.symbol to it.displayName }, actions::setSymbol)
            MenuField("Contract", state.contractType, state.contracts.map { it.contractType to it.displayName }, actions::setContractType)
            NumericField("Stake amount", state.amount, actions::setAmount)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NumericField("Contract duration", state.duration, actions::setDuration, Modifier.weight(1f))
                MenuField("Unit", state.durationUnit, listOf("t" to "Ticks", "s" to "Seconds", "m" to "Minutes", "h" to "Hours", "d" to "Days"), actions::setDurationUnit, Modifier.weight(1f))
            }
            Text("Safety limits", style = MaterialTheme.typography.titleMedium)
            NumericField("Seconds between trades (minimum 30)", state.intervalSeconds, actions::setInterval)
            NumericField("Maximum trades", state.maxTrades, actions::setMaxTrades)
            NumericField("Maximum loss budget", state.maxLoss, actions::setMaxLoss)
            NumericField("Maximum run minutes", state.maxDurationMinutes, actions::setMaxDuration)
            NumericField("Maximum open positions", state.maxConcurrentPositions, actions::setMaxConcurrent)
            Button(onClick = actions::createStrategy, enabled = !state.isBusy && state.contractType.isNotBlank(), modifier = Modifier.fillMaxWidth()) { Text("Create strategy") }
        }
    }
}

@Composable
private fun StrategyCard(strategy: AutomationStrategy, disabled: Boolean, onStart: (AutomationStrategy) -> Unit) {
    SynexCard {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(Modifier.weight(1f)) { Text(strategy.name, style = MaterialTheme.typography.titleMedium); Text("${strategy.contractType} · ${strategy.symbol} · ${strategy.loginId}", style = MaterialTheme.typography.bodySmall, color = SynexMuted) }
                Text(if (strategy.isVirtual) "PRACTICE" else "REAL", color = if (strategy.isVirtual) SynexGreen else SynexRed, style = MaterialTheme.typography.labelMedium)
            }
            Text("Every ${strategy.intervalSeconds}s · up to ${strategy.maxTrades} trades · ${strategy.maxLoss} ${strategy.currency} loss budget", style = MaterialTheme.typography.bodySmall)
            Button(onClick = { onStart(strategy) }, enabled = !disabled) { Text("Start") }
        }
    }
}

@Composable
private fun RunCard(run: AutomationRun, disabled: Boolean, transition: (String, String) -> Unit) {
    SynexCard {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(run.strategyName, style = MaterialTheme.typography.titleMedium); Text(run.status.uppercase(), style = MaterialTheme.typography.labelMedium) }
            Text("${run.successfulTrades}/${run.tradeCount} successful · ${run.settledTrades} settled · P/L ${run.realizedProfit} · ${run.committedLoss} loss budget committed", style = MaterialTheme.typography.bodySmall, color = SynexMuted)
            run.lastError.takeIf(String::isNotBlank)?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = SynexRed) }
            if (run.status == "active" || run.status == "paused") Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (run.status == "active") OutlinedButton(onClick = { transition(run.id, "pause") }, enabled = !disabled) { Text("Pause") }
                else OutlinedButton(onClick = { transition(run.id, "resume") }, enabled = !disabled) { Text("Resume") }
                OutlinedButton(onClick = { transition(run.id, "stop") }, enabled = !disabled) { Text("Stop") }
            }
        }
    }
}

@Composable
private fun NumericField(label: String, value: String, onValue: (String) -> Unit, modifier: Modifier = Modifier) = OutlinedTextField(value, onValue, label = { Text(label) }, modifier = modifier.fillMaxWidth(), singleLine = true)

@Composable
private fun MenuField(label: String, value: String, options: List<Pair<String, String>>, onValue: (String) -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = SynexMuted)
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) { Text(options.firstOrNull { it.first == value }?.second ?: value.ifBlank { "Choose" }, maxLines = 1) }
        DropdownMenu(expanded, onDismissRequest = { expanded = false }) { options.forEach { option -> DropdownMenuItem(text = { Text(option.second) }, onClick = { expanded = false; onValue(option.first) }) } }
    }
}

@Composable
private fun Notice(message: String, danger: Boolean = false) { SynexCard { Text(message, Modifier.padding(18.dp), color = if (danger) SynexRed else SynexMuted) } }
