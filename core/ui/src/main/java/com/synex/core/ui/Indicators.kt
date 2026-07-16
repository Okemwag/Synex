package com.synex.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowOutward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun ChangePill(changePercent: Double, modifier: Modifier = Modifier) {
    val positive = changePercent >= 0
    val color = if (positive) SynexGreen else SynexRed
    Row(
        modifier = modifier.clip(CircleShape).background(color.copy(alpha = 0.13f))
            .padding(horizontal = 9.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Outlined.ArrowOutward, null, Modifier.size(12.dp), tint = color)
        Text(
            text = "${if (positive) "+" else ""}${"%.2f".format(changePercent)}%",
            style = MaterialTheme.typography.labelMedium,
            color = color,
        )
    }
}
