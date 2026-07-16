package com.synex.feature.portfolio

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.CallMade
import androidx.compose.material.icons.automirrored.outlined.CallReceived
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.synex.core.model.Position
import com.synex.core.model.PositionDirection
import com.synex.core.ui.SynexCard
import com.synex.core.ui.SynexGreen
import com.synex.core.ui.SynexMuted
import com.synex.core.ui.SynexRed
import com.synex.core.ui.formatMoney

@Composable
internal fun PositionCard(position: Position) {
    val directionColor = if (position.direction == PositionDirection.UP) SynexGreen else SynexRed
    SynexCard(Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier.size(42.dp).background(directionColor.copy(alpha = 0.14f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center,
            ) {
                val icon = if (position.direction == PositionDirection.UP) {
                    Icons.AutoMirrored.Outlined.CallMade
                } else {
                    Icons.AutoMirrored.Outlined.CallReceived
                }
                Icon(icon, null, tint = directionColor)
            }
            Column(Modifier.padding(start = 12.dp).weight(1f)) {
                Text(position.displayName, style = MaterialTheme.typography.titleMedium)
                Text(
                    "${position.direction.displayName()} · #${position.contractId}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SynexMuted,
                )
            }
            PositionValue(position)
        }
    }
}

@Composable
private fun PositionValue(position: Position) {
    val positive = position.profitLoss >= 0
    Column(horizontalAlignment = Alignment.End) {
        Text(formatMoney(position.currentValue, position.currency), style = MaterialTheme.typography.titleMedium)
        Text(
            "${if (positive) "+" else ""}${formatMoney(position.profitLoss, position.currency)}",
            style = MaterialTheme.typography.labelMedium,
            color = if (positive) SynexGreen else SynexRed,
        )
    }
}

private fun PositionDirection.displayName() = name.lowercase().replaceFirstChar(Char::uppercase)
