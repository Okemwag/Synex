package com.synex.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun PremiumIcon(
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    dark: Boolean = false,
) {
    val colors = if (dark) listOf(Color.White.copy(alpha = 0.18f), Color.White.copy(alpha = 0.06f))
    else listOf(SynexGoldSoft, Color.White)
    Box(
        modifier.size(46.dp).background(Brush.linearGradient(colors), RoundedCornerShape(15.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            icon,
            contentDescription,
            Modifier.size(21.dp),
            tint = if (dark) SynexGold else SynexTealDark,
        )
    }
}
