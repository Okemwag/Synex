package com.synex.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.synex.core.model.MarketQuote
import kotlin.math.abs

@Composable
fun MarketRow(market: MarketQuote, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        androidx.compose.foundation.layout.Box(
            Modifier.size(40.dp).clip(RoundedCornerShape(13.dp)).background(SynexGreenSoft),
            contentAlignment = Alignment.Center,
        ) {
            Text(market.displayName.take(1), style = MaterialTheme.typography.titleMedium, color = SynexGreen)
        }
        Column(Modifier.padding(start = 12.dp).weight(1f)) {
            Text(
                market.displayName,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(market.market, style = MaterialTheme.typography.bodyMedium, color = SynexMuted)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(formatQuote(market.price), style = MaterialTheme.typography.titleMedium)
            Text(
                "${if (market.changePercent >= 0) "+" else ""}${"%.2f".format(market.changePercent)}%",
                style = MaterialTheme.typography.labelMedium,
                color = if (market.changePercent >= 0) SynexGreen else SynexRed,
            )
        }
    }
}

private fun formatQuote(value: Double): String = when {
    abs(value) >= 1_000 -> "%,.2f".format(value)
    value == 0.0 -> "—"
    else -> "%.4f".format(value)
}
