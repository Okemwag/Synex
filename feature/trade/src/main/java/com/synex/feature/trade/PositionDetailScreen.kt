package com.synex.feature.trade

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.synex.core.ui.ErrorState
import com.synex.core.ui.LoadingState
import com.synex.core.ui.PageHeading
import com.synex.core.ui.SynexCard
import com.synex.core.ui.SynexGreen
import com.synex.core.ui.SynexInk
import com.synex.core.ui.SynexMuted
import com.synex.core.ui.SynexPaper
import com.synex.core.ui.SynexRed
import com.synex.core.ui.formatMoney
import java.text.DateFormat
import java.util.Date

@Composable
fun PositionDetailRoute(
    repository: SynexRepository,
    contractId: Long,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PositionDetailViewModel = viewModel(factory = PositionDetailViewModel.factory(repository, contractId)),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var confirmAction by remember { mutableStateOf<String?>(null) }
    var stopLoss by remember { mutableStateOf("") }
    var takeProfit by remember { mutableStateOf("") }
    LazyColumn(
        modifier = modifier.fillMaxSize().background(SynexPaper),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { TextButton(onClick = onBack) { Text("← Back to portfolio") } }
        item { PageHeading("Live Deriv contract #$contractId", "Position") }
        when {
            state.isLoading -> item { LoadingState() }
            state.position == null && state.errorMessage != null -> item { ErrorState(state.errorMessage, viewModel::refresh) }
            state.position != null -> {
                val position = state.position
                item {
                    SynexCard(dark = true) {
                        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("${position.contractType} · ${position.symbol}", color = SynexMuted)
                            Text(
                                formatMoney(position.profit, position.currency),
                                style = MaterialTheme.typography.displayMedium,
                                color = if (position.profit >= 0) SynexGreen else SynexRed,
                            )
                            Text("${position.profitPercentage.formatPercent()} · ${position.status}")
                            Text(position.longcode, style = MaterialTheme.typography.bodyMedium, color = androidx.compose.ui.graphics.Color.White.copy(alpha = .62f))
                            DetailLine("Paid", formatMoney(position.buyPrice, position.currency))
                            DetailLine("Current spot", position.currentSpot.toString())
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(
                                    onClick = { confirmAction = "sell" },
                                    enabled = !state.isBusy && !state.closed,
                                    colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color.White, contentColor = SynexInk),
                                ) { Text("Close now") }
                                OutlinedButton(onClick = { confirmAction = "cancel" }, enabled = !state.isBusy && !state.closed) { Text("Cancel") }
                            }
                        }
                    }
                }
                item {
                    SynexCard {
                        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Protect this trade", style = MaterialTheme.typography.titleLarge)
                            OutlinedTextField(stopLoss, { stopLoss = it }, label = { Text("Stop loss") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(takeProfit, { takeProfit = it }, label = { Text("Take profit") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                            Button(
                                onClick = { viewModel.updateLimits(stopLoss, takeProfit) },
                                enabled = !state.isBusy && !state.closed && (stopLoss.isNotBlank() || takeProfit.isNotBlank()),
                                modifier = Modifier.fillMaxWidth(),
                            ) { Text("Save protection") }
                        }
                    }
                }
                item {
                    SynexCard {
                        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Update history", style = MaterialTheme.typography.titleLarge)
                            if (state.history.isEmpty()) Text("No contract updates yet.", color = SynexMuted)
                            state.history.forEach { event ->
                                DetailLine(event.type, "${event.amount}${event.epochSeconds.takeIf { it > 0 }?.let { " · ${DateFormat.getDateTimeInstance().format(Date(it * 1000))}" }.orEmpty()}")
                            }
                        }
                    }
                }
                state.errorMessage?.let { item { Text(it, color = MaterialTheme.colorScheme.error) } }
                state.successMessage?.let { item { Text(it, color = SynexGreen) } }
            }
        }
    }

    confirmAction?.let { action ->
        AlertDialog(
            onDismissRequest = { confirmAction = null },
            title = { Text(if (action == "sell") "Close this trade?" else "Cancel this trade?") },
            text = { Text(if (action == "sell") "Deriv will sell at the currently available market price." else "Only eligible contracts can be cancelled. Deriv will decide whether this contract qualifies.") },
            confirmButton = {
                Button(onClick = { confirmAction = null; if (action == "sell") viewModel.sell() else viewModel.cancel() }) { Text("Confirm") }
            },
            dismissButton = { TextButton(onClick = { confirmAction = null }) { Text("Go back") } },
        )
    }
}

@Composable
private fun DetailLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = SynexMuted)
        Text(value, style = MaterialTheme.typography.labelLarge)
    }
}

private fun Double.formatPercent() = "${String.format(java.util.Locale.US, "%.2f", this)}%"
