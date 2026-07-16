package com.synex.feature.legal

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ArrowOutward
import androidx.compose.material.icons.outlined.Gavel
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import com.synex.core.ui.PageHeading
import com.synex.core.ui.SynexCard
import com.synex.core.ui.SynexMuted
import com.synex.core.ui.SynexPaper

@Composable
fun LegalRoute(onBack: () -> Unit, modifier: Modifier = Modifier) {
    BackHandler(onBack = onBack)
    val uriHandler = LocalUriHandler.current
    LegalScreen(onBack, { uriHandler.openUri(it) }, modifier)
}

@Composable
fun LegalScreen(onBack: () -> Unit, onOpenUrl: (String) -> Unit, modifier: Modifier = Modifier) {
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
        item {
            SynexCard(Modifier.fillMaxWidth(), dark = true) {
                Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Gavel, null)
                    Text(
                        "Drafts require legal and compliance approval before public launch.",
                        Modifier.padding(start = 12.dp),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
        items(legalLinks, key = { it.url }) { link -> LegalRow(link, onOpenUrl) }
    }
}

@Composable
private fun LegalRow(link: LegalLink, onOpenUrl: (String) -> Unit) {
    Row(
        Modifier.fillMaxWidth().background(Color.White.copy(alpha = 0.75f), RoundedCornerShape(20.dp))
            .clickable { onOpenUrl(link.url) }.padding(18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(link.title, style = MaterialTheme.typography.titleMedium)
            Text(link.summary, style = MaterialTheme.typography.bodyMedium, color = SynexMuted)
            Text(link.version.uppercase(), style = MaterialTheme.typography.labelMedium, color = SynexMuted)
        }
        Icon(Icons.Outlined.ArrowOutward, null, tint = SynexMuted)
    }
}
