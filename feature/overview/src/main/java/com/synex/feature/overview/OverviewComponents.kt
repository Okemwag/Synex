package com.synex.feature.overview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.CandlestickChart
import androidx.compose.material.icons.outlined.PieChartOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.synex.core.model.OverviewSnapshot
import com.synex.core.ui.ChangePill
import com.synex.core.ui.SynexCard
import com.synex.core.ui.SynexGreen
import com.synex.core.ui.formatMoney

@Composable
internal fun BalanceCard(snapshot: OverviewSnapshot) {
    SynexCard(Modifier.fillMaxWidth(), dark = true) {
        Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    MetricLabel("TOTAL EQUITY")
                    Spacer(Modifier.height(6.dp))
                    Text(
                        formatMoney(snapshot.portfolio.equity, snapshot.portfolio.currency),
                        style = MaterialTheme.typography.displaySmall,
                    )
                }
                val equity = snapshot.portfolio.equity
                ChangePill(if (equity == 0.0) 0.0 else snapshot.portfolio.profitLoss / equity * 100)
            }
            Text(
                if (snapshot.account.isVirtual) "DEMO ACCOUNT" else "LIVE ACCOUNT",
                style = MaterialTheme.typography.labelMedium,
                color = SynexGreen,
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    MetricLabel("Available cash")
                    Text(formatMoney(snapshot.portfolio.availableCash, snapshot.portfolio.currency))
                }
                Column(horizontalAlignment = Alignment.End) {
                    MetricLabel("Open positions")
                    Text(snapshot.portfolio.positions.size.toString().padStart(2, '0'))
                }
            }
        }
    }
}

@Composable
internal fun QuickActions(onMarkets: () -> Unit, onPortfolio: () -> Unit, onAccount: () -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        QuickAction("Markets", Icons.Outlined.CandlestickChart, onMarkets, Modifier.weight(1f))
        QuickAction("Portfolio", Icons.Outlined.PieChartOutline, onPortfolio, Modifier.weight(1f))
        QuickAction("Account", Icons.Outlined.AccountCircle, onAccount, Modifier.weight(1f))
    }
}

@Composable
private fun QuickAction(label: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = CircleShape,
        color = Color.White.copy(alpha = 0.8f),
    ) {
        Row(
            Modifier.padding(horizontal = 13.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, null, Modifier.size(15.dp), tint = SynexGreen)
            Spacer(Modifier.size(6.dp))
            Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun MetricLabel(value: String) {
    Text(value, style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.55f))
}
