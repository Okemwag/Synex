package com.synex.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PageHeading(
    eyebrow: String,
    title: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(eyebrow.uppercase(), style = MaterialTheme.typography.labelMedium, color = SynexMuted)
            Text(title, style = MaterialTheme.typography.headlineLarge)
        }
        action?.invoke()
    }
}

@Composable
fun SectionHeading(
    title: String,
    detail: String? = null,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge)
        detail?.let { Text(it, style = MaterialTheme.typography.labelMedium, color = SynexMuted) }
    }
}
