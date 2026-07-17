package com.synex.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
fun MarketRow(
    market: MarketQuote,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val rowModifier = if (onClick == null) modifier else modifier.clickable(onClick = onClick)
    Row(
        modifier = rowModifier.fillMaxWidth().padding(vertical = 13.dp),
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
            val price = market.price
            val change = market.changePercent
            if (price != null && change != null) {
                Text(formatQuote(price), style = MaterialTheme.typography.titleMedium)
                Text(
                    "${if (change >= 0) "+" else ""}${"%.2f".format(change)}%",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (change >= 0) SynexGreen else SynexRed,
                )
            } else {
                Text(if (market.isOpen) "Market open" else "Market closed", style = MaterialTheme.typography.labelMedium)
                Text("Live price unavailable", style = MaterialTheme.typography.bodySmall, color = SynexMuted)
            }
        }
    }
}

private fun formatQuote(value: Double): String = when {
    abs(value) >= 1_000 -> "%,.2f".format(value)
    value == 0.0 -> "—"
    else -> "%.4f".format(value)
}
