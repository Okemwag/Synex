package com.synex.feature.legal

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ArrowOutward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.synex.core.ui.PageHeading
import com.synex.core.ui.SynexCard
import com.synex.core.ui.SynexMuted
import com.synex.core.ui.SynexPaper

@Composable
fun LegalRoute(onBack: () -> Unit, onDocument: (String) -> Unit, modifier: Modifier = Modifier) {
    BackHandler(onBack = onBack)
    LegalScreen(onBack, onDocument, modifier)
}

@Composable
fun LegalScreen(onBack: () -> Unit, onDocument: (String) -> Unit, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize().background(SynexPaper),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            PageHeading("Trust and transparency", "Legal centre") {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back") }
            }
        }
        items(legalLinks, key = { it.type }) { link -> LegalRow(link, onDocument) }
    }
}

@Composable
private fun LegalRow(link: LegalLink, onDocument: (String) -> Unit) {
    androidx.compose.foundation.layout.Row(
        Modifier.fillMaxWidth().background(Color.White.copy(alpha = 0.75f), RoundedCornerShape(20.dp))
            .clickable { onDocument(link.type) }.padding(18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(link.title, style = MaterialTheme.typography.titleMedium)
            Text(link.summary, style = MaterialTheme.typography.bodyMedium, color = SynexMuted)
        }
        Icon(Icons.Outlined.ArrowOutward, null, tint = SynexMuted)
    }
}
