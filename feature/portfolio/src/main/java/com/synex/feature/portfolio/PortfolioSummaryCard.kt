package com.synex.feature.portfolio

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.synex.core.model.PortfolioSummary
import com.synex.core.ui.SynexCard
import com.synex.core.ui.formatMoney

@Composable
internal fun PortfolioSummaryCard(portfolio: PortfolioSummary) {
    SynexCard(Modifier.fillMaxWidth(), dark = true) {
        Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Column {
                SummaryLabel("NET EQUITY")
                Spacer(Modifier.height(6.dp))
                Text(
                    portfolio.equity?.let { formatMoney(it, portfolio.currency) } ?: "Unavailable",
                    style = MaterialTheme.typography.displaySmall,
                )
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                SummaryMetric("Available", formatMoney(portfolio.availableCash, portfolio.currency))
                SummaryMetric(
                    "Total P/L",
                    portfolio.profitLoss?.let {
                        "${if (it >= 0) "+" else ""}${formatMoney(it, portfolio.currency)}"
                    } ?: "Unavailable",
                    Alignment.End,
                )
            }
        }
    }
}

@Composable
private fun SummaryMetric(
    label: String,
    value: String,
    alignment: Alignment.Horizontal = Alignment.Start,
) {
    Column(horizontalAlignment = alignment) {
        SummaryLabel(label)
        Text(value, style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
private fun SummaryLabel(value: String) {
    Text(value, style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.55f))
}
