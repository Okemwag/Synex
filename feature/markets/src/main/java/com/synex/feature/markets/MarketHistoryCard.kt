package com.synex.feature.markets

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.synex.core.model.Candle
import com.synex.core.model.MarketQuote
import com.synex.core.ui.SynexCard
import com.synex.core.ui.SynexGreen
import com.synex.core.ui.SynexGreenSoft
import com.synex.core.ui.SynexMuted
import kotlin.math.abs

@Composable
internal fun MarketHistoryCard(
    market: MarketQuote,
    candles: List<Candle>,
    isLoading: Boolean,
    errorMessage: String?,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SynexCard(modifier.fillMaxWidth()) {
        Column(
            Modifier.fillMaxWidth().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(market.displayName, style = MaterialTheme.typography.titleLarge)
                    Text("30-day price history · ${market.symbol}", color = SynexMuted, style = MaterialTheme.typography.bodyMedium)
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Outlined.Close, contentDescription = "Close market history")
                }
            }

            when {
                isLoading -> Box(Modifier.fillMaxWidth().height(170.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = SynexGreen)
                }
                errorMessage != null -> Box(Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                    Text(errorMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                }
                candles.isEmpty() -> Box(Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                    Text("No price history is currently available.", color = SynexMuted)
                }
                else -> CandleHistory(candles)
            }
        }
    }
}

@Composable
private fun CandleHistory(candles: List<Candle>) {
    val lows = candles.map(Candle::low)
    val highs = candles.map(Candle::high)
    val lowest = lows.minOrNull() ?: 0.0
    val highest = highs.maxOrNull() ?: lowest
    val first = candles.first().close
    val latest = candles.last().close
    val change = if (first == 0.0) 0.0 else ((latest - first) / abs(first)) * 100.0

    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Column {
            Text(formatPrice(latest), style = MaterialTheme.typography.headlineSmall)
            Text("Latest close", color = SynexMuted, style = MaterialTheme.typography.labelMedium)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                "${if (change >= 0) "+" else ""}${"%.2f".format(change)}%",
                color = if (change >= 0) SynexGreen else MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.titleMedium,
            )
            Text("30-day change", color = SynexMuted, style = MaterialTheme.typography.labelMedium)
        }
    }

    Canvas(
        Modifier
            .fillMaxWidth()
            .height(170.dp)
            .background(SynexGreenSoft, MaterialTheme.shapes.large)
            .padding(12.dp),
    ) {
        val range = (highest - lowest).takeIf { it > 0.0 } ?: 1.0
        val stepX = if (candles.size <= 1) 0f else size.width / (candles.size - 1)
        val path = Path()
        candles.forEachIndexed { index, candle ->
            val x = stepX * index
            val y = size.height - (((candle.close - lowest) / range).toFloat() * size.height)
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, color = SynexGreen, style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round))
        val lastY = size.height - (((latest - lowest) / range).toFloat() * size.height)
        drawCircle(SynexGreen, radius = 5.dp.toPx(), center = Offset(size.width, lastY))
    }

    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("Low ${formatPrice(lowest)}", color = SynexMuted, style = MaterialTheme.typography.labelMedium)
        Text("High ${formatPrice(highest)}", color = SynexMuted, style = MaterialTheme.typography.labelMedium)
    }
}

private fun formatPrice(value: Double): String = when {
    abs(value) >= 1_000 -> "%,.2f".format(value)
    abs(value) >= 1 -> "%.4f".format(value)
    else -> "%.6f".format(value)
}
