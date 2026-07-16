package com.synex.core.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun SynexCard(
    modifier: Modifier = Modifier,
    dark: Boolean = false,
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(26.dp)
    Surface(
        modifier = modifier
            .clip(shape)
            .then(if (dark) Modifier else Modifier.border(1.dp, SynexLine, shape)),
        shape = shape,
        color = if (dark) SynexInk else Color.White.copy(alpha = 0.78f),
        contentColor = if (dark) Color.White else SynexInk,
        content = content,
    )
}
