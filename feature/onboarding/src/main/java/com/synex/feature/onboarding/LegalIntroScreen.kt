package com.synex.feature.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Gavel
import androidx.compose.material.icons.rounded.PrivacyTip
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.synex.core.ui.PremiumIcon
import com.synex.core.ui.SynexCard
import com.synex.core.ui.SynexMuted

@Composable
internal fun LegalIntroScreen(onContinue: () -> Unit) {
    OnboardingFrame(1, "Review privacy", onContinue) {
        item {
            Text("A clear start", style = MaterialTheme.typography.labelLarge, color = SynexMuted)
            Text(
                "Know what matters before you begin.",
                style = MaterialTheme.typography.displaySmall,
                modifier = Modifier.padding(top = 10.dp),
            )
            Text(
                "We keep the important details easy to find and easy to understand.",
                style = MaterialTheme.typography.bodyLarge,
                color = SynexMuted,
                modifier = Modifier.padding(top = 14.dp),
            )
        }
        item { LegalTopic(Icons.Rounded.PrivacyTip, "Privacy", "How we use and protect your information.") }
        item { LegalTopic(Icons.Rounded.Gavel, "Your agreement", "The rules that apply when you use Synex.") }
        item { LegalTopic(Icons.Rounded.WarningAmber, "Trading risk", "What you could lose and what to consider before trading.") }
    }
}

@Composable
private fun LegalTopic(icon: ImageVector, title: String, detail: String) {
    SynexCard(Modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            PremiumIcon(icon, null)
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleLarge)
                Text(detail, style = MaterialTheme.typography.bodyMedium, color = SynexMuted)
            }
        }
    }
}
