package com.synex.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Image
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SynexWordmark(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SynexBrandMark(Modifier.size(28.dp))
        Text(
            text = "Synex",
            style = MaterialTheme.typography.titleLarge,
            letterSpacing = (-0.5).sp,
        )
    }
}

@Composable
fun SynexBrandMark(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(R.drawable.synex_logo),
        contentDescription = "Synex",
        modifier = modifier.clip(RoundedCornerShape(8.dp)),
    )
}
