package com.synex.feature.activity

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
import com.synex.core.model.ActivityRow
import com.synex.core.ui.ActionState
import com.synex.core.ui.ErrorState
import com.synex.core.ui.LoadingState
import com.synex.core.ui.PageHeading
import com.synex.core.ui.SynexCard
import com.synex.core.ui.SynexGreen
import com.synex.core.ui.SynexMuted
import com.synex.core.ui.SynexPaper
import com.synex.core.ui.SynexRed
import com.synex.core.ui.formatMoney
import java.text.DateFormat
import java.util.Date

@Composable
fun ActivityRoute(
    repository: SynexRepository,
    onAccount: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ActivityViewModel = viewModel(factory = ActivityViewModel.factory(repository)),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LazyColumn(
        modifier = modifier.fillMaxSize().background(SynexPaper),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item { PageHeading("Transactions and settled outcomes", "Activity") }
        if (!state.hasAccount) {
            item { ActionState("Connect your Deriv account", "Your statement and trade results will appear after an account is linked.", "Connect account", onAccount) }
            return@LazyColumn
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ActivityTab.entries.forEach { tab ->
                    if (state.tab == tab) Button(onClick = { viewModel.selectTab(tab) }) { Text(tab.label()) }
                    else OutlinedButton(onClick = { viewModel.selectTab(tab) }) { Text(tab.label()) }
                }
            }
        }
        item { ActivityFilters(state, viewModel) }
        when {
            state.isLoading -> item { LoadingState() }
            state.errorMessage != null -> item { ErrorState(state.errorMessage, viewModel::refresh) }
            state.rows.isEmpty() -> item { SynexCard { Text("Nothing matches these filters yet.", Modifier.padding(24.dp), color = SynexMuted) } }
            else -> items(state.rows) { row -> ActivityCard(row, state.tab) }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    "Showing ${if (state.rows.isEmpty()) 0 else state.offset + 1}–${state.offset + state.rows.size}${if (state.count > state.rows.size) " of ${state.count}" else ""}",
                    style = MaterialTheme.typography.labelMedium,
                    color = SynexMuted,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = viewModel::previous, enabled = state.hasPrevious && !state.isLoading) { Text("Previous") }
                    OutlinedButton(onClick = viewModel::next, enabled = state.hasNext && !state.isLoading) { Text("Next") }
                }
            }
        }
    }
}

@Composable
private fun ActivityFilters(state: ActivityUiState, actions: ActivityViewModel) {
    SynexCard {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(state.dateFrom, actions::setDateFrom, label = { Text("From YYYY-MM-DD") }, singleLine = true, modifier = Modifier.weight(1f))
                OutlinedTextField(state.dateTo, actions::setDateTo, label = { Text("To YYYY-MM-DD") }, singleLine = true, modifier = Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (state.tab == ActivityTab.TRANSACTIONS) {
                    FilterMenu("Type", state.actionType, listOf("" to "All", "buy" to "Buy", "sell" to "Sell", "deposit" to "Deposit", "withdrawal" to "Withdrawal"), actions::setActionType, Modifier.weight(1f))
                }
                FilterMenu(
                    "Order",
                    state.sort,
                    listOf("DESC" to if (state.tab == ActivityTab.TRANSACTIONS) "Newest on page" else "Newest first", "ASC" to if (state.tab == ActivityTab.TRANSACTIONS) "Oldest on page" else "Oldest first"),
                    actions::setSort,
                    Modifier.weight(1f),
                )
                TextButton(onClick = actions::clearFilters) { Text("Clear") }
                Button(onClick = actions::refresh) { Text("Apply") }
            }
        }
    }
}

@Composable
private fun FilterMenu(label: String, value: String, choices: List<Pair<String, String>>, onValue: (String) -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = SynexMuted)
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text(choices.firstOrNull { it.first == value }?.second ?: label, maxLines = 1)
        }
        DropdownMenu(expanded, onDismissRequest = { expanded = false }) {
            choices.forEach { choice -> DropdownMenuItem(text = { Text(choice.second) }, onClick = { expanded = false; onValue(choice.first) }) }
        }
    }
}

@Composable
private fun ActivityCard(row: ActivityRow, tab: ActivityTab) {
    var expanded by remember { mutableStateOf(false) }
    val result = row.profit ?: row.amount
    SynexCard(Modifier.fillMaxWidth().clickable { expanded = !expanded }) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(Modifier.weight(1f)) {
                    Text(row.type, style = MaterialTheme.typography.titleMedium)
                    Text(row.description.ifBlank { row.contractId?.let { "Contract #$it" } ?: "Transaction #${row.transactionId}" }, style = MaterialTheme.typography.bodySmall, color = SynexMuted, maxLines = 2)
                }
                result?.let {
                    Text(
                        formatMoney(it, row.currency),
                        style = MaterialTheme.typography.titleMedium,
                        color = if (it >= 0) SynexGreen else SynexRed,
                    )
                }
            }
            Text(row.epochSeconds.takeIf { it > 0 }?.let { DateFormat.getDateTimeInstance().format(Date(it * 1000)) } ?: "Time unavailable", style = MaterialTheme.typography.labelSmall, color = SynexMuted)
            if (tab == ActivityTab.TRADE_RESULTS && row.buyPrice != null) {
                Text("Paid ${formatMoney(row.buyPrice, row.currency)} · Returned ${row.sellPrice?.let { formatMoney(it, row.currency) } ?: "—"}", style = MaterialTheme.typography.bodySmall)
            }
            Text(if (expanded) "Hide full details" else "View full details", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            if (expanded) {
                row.details.forEach { (label, value) ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(label, style = MaterialTheme.typography.bodySmall, color = SynexMuted, modifier = Modifier.weight(.45f))
                        Text(value, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(.55f))
                    }
                }
            }
        }
    }
}

private fun ActivityTab.label() = if (this == ActivityTab.TRANSACTIONS) "Transactions" else "Trade results"
