package com.synex.feature.overview

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
import com.synex.core.model.OverviewSnapshot
import com.synex.core.ui.SynexCard
import com.synex.core.ui.SynexGreen
import com.synex.core.ui.SynexRed
import com.synex.core.ui.formatMoney

@Composable
internal fun BalanceCard(snapshot: OverviewSnapshot) {
    val portfolio = snapshot.portfolio
    SynexCard(Modifier.fillMaxWidth(), dark = true) {
        Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    MetricLabel("TOTAL EQUITY")
                    Spacer(Modifier.height(6.dp))
                    Text(
                        portfolio.equity?.let { formatMoney(it, portfolio.currency) } ?: "Unavailable",
                        style = MaterialTheme.typography.displaySmall,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    MetricLabel("OPEN P/L")
                    val profitLoss = portfolio.profitLoss
                    if (profitLoss != null) {
                        Text(
                            formatMoney(profitLoss, portfolio.currency),
                            color = if (profitLoss >= 0) SynexGreen else SynexRed,
                        )
                    } else {
                        Text("Unavailable", color = Color.White.copy(alpha = 0.7f))
                    }
                }
            }
            Text(
                if (snapshot.account.isVirtual) "DEMO ACCOUNT" else "LIVE ACCOUNT",
                style = MaterialTheme.typography.labelMedium,
                color = SynexGreen,
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    MetricLabel("Available cash")
                    Text(formatMoney(portfolio.availableCash, portfolio.currency))
                }
                Column(horizontalAlignment = Alignment.End) {
                    MetricLabel("Open positions")
                    Text(portfolio.positions.size.toString().padStart(2, '0'))
                }
            }
        }
    }
}

@Composable
private fun MetricLabel(value: String) {
    Text(value, style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.55f))
}
