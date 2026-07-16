package com.synex.feature.account

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.synex.core.ui.SynexCard
import com.synex.core.ui.PremiumIcon
import com.synex.core.ui.SynexMuted

data class SettingItem(
    val icon: ImageVector,
    val title: String,
    val detail: String,
    val onClick: (() -> Unit)? = null,
)

@Composable
internal fun SettingsCard(items: List<SettingItem>) {
    SynexCard(Modifier.fillMaxWidth()) {
        Column { items.forEach { SettingsRow(it) } }
    }
}

@Composable
private fun SettingsRow(item: SettingItem) {
    val rowModifier = item.onClick?.let {
        Modifier.fillMaxWidth().clickable(onClick = it)
    } ?: Modifier.fillMaxWidth()
    Row(
        rowModifier.padding(horizontal = 16.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PremiumIcon(item.icon, null)
        Column(Modifier.padding(start = 12.dp).weight(1f)) {
            Text(item.title, style = MaterialTheme.typography.titleMedium)
            Text(item.detail, style = MaterialTheme.typography.bodyMedium, color = SynexMuted)
        }
        if (item.onClick != null) Icon(Icons.Outlined.ChevronRight, null, tint = SynexMuted)
    }
}
