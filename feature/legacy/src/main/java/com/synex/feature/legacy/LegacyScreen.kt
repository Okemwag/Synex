package com.synex.feature.legacy

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
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import com.synex.core.ui.ErrorState
import com.synex.core.ui.LoadingState
import com.synex.core.ui.PageHeading
import com.synex.core.ui.SectionHeading
import com.synex.core.ui.SynexCard
import com.synex.core.ui.SynexMuted
import com.synex.core.ui.SynexPaper
import com.synex.core.ui.formatMoney
import java.text.DateFormat
import java.util.Date

@Composable
fun LegacyRoute(repository: SynexRepository, modifier: Modifier = Modifier, viewModel: LegacyViewModel = viewModel(factory = LegacyViewModel.factory(repository))) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LazyColumn(modifier.fillMaxSize().background(SynexPaper), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { PageHeading("Read-only pre-upgrade records", "Legacy Deriv history") }
        item { Notice("Temporary feature: Deriv will remove these endpoints after platform migration is complete. Current account activity remains in the normal Activity screen.") }
        item { Notice("Migration status: ${state.migrationStatus.replace('_', ' ')}") }
        state.errorMessage?.let { item { ErrorState(it, viewModel::loadSummary) } }
        if (state.isLoading) item { LoadingState() }
        if (!state.isLoading && state.loginIds.isEmpty()) item { Notice("No legacy account mapping was found. This is normal for accounts created after Deriv's platform upgrade.") }
        if (state.loginIds.isNotEmpty()) {
            item { LegacyFilters(state, viewModel) }
            item { SectionHeading("Legacy statement", "${state.count} historical transactions") }
            if (!state.isLoading && state.rows.isEmpty()) item { Notice("No legacy transactions match these filters.") }
            items(state.rows) { row -> LegacyRow(row) }
            item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { OutlinedButton(viewModel::previous, enabled = state.hasPrevious && !state.isLoading) { Text("Previous") }; OutlinedButton(viewModel::next, enabled = state.hasNext && !state.isLoading) { Text("Next") } } }
        }
    }
}

@Composable
private fun LegacyFilters(state: LegacyUiState, actions: LegacyViewModel) { SynexCard { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
    Menu("Legacy account", state.selectedLoginId, state.loginIds.map { it to it }, actions::setLoginId)
    Menu("Type", state.actionType, listOf("" to "All", "buy" to "Buy", "sell" to "Sell", "deposit" to "Deposit", "withdrawal" to "Withdrawal", "transfer" to "Transfer", "hold" to "Hold", "release" to "Release", "adjustment" to "Adjustment", "escrow" to "Escrow"), actions::setActionType)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { OutlinedTextField(state.dateFrom, actions::setDateFrom, label = { Text("From YYYY-MM-DD") }, singleLine = true, modifier = Modifier.weight(1f)); OutlinedTextField(state.dateTo, actions::setDateTo, label = { Text("To YYYY-MM-DD") }, singleLine = true, modifier = Modifier.weight(1f)) }
    Button(actions::refresh, enabled = !state.isLoading, modifier = Modifier.fillMaxWidth()) { Text("Apply filters") }
} } }

@Composable
private fun LegacyRow(row: ActivityRow) { SynexCard { Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(row.type.ifBlank { "Transaction" }, style = MaterialTheme.typography.titleMedium); row.amount?.let { Text(formatMoney(it, row.currency), style = MaterialTheme.typography.titleMedium) } }; Text(row.epochSeconds.takeIf { it > 0 }?.let { DateFormat.getDateTimeInstance().format(Date(it * 1000)) } ?: "Time unavailable", style = MaterialTheme.typography.bodySmall, color = SynexMuted); Text("Transaction #${row.transactionId} · balance after ${row.balanceAfter ?: "—"}", style = MaterialTheme.typography.bodySmall, color = SynexMuted) } } }

@Composable
private fun Menu(label: String, value: String, choices: List<Pair<String, String>>, onValue: (String) -> Unit) { var open by remember { mutableStateOf(false) }; Column { Text(label, style = MaterialTheme.typography.labelSmall, color = SynexMuted); OutlinedButton({ open = true }, modifier = Modifier.fillMaxWidth()) { Text(choices.firstOrNull { it.first == value }?.second ?: label) }; DropdownMenu(open, onDismissRequest = { open = false }) { choices.forEach { choice -> DropdownMenuItem(text = { Text(choice.second) }, onClick = { open = false; onValue(choice.first) }) } } } }

@Composable
private fun Notice(message: String) { SynexCard { Text(message, Modifier.padding(18.dp), color = SynexMuted) } }
