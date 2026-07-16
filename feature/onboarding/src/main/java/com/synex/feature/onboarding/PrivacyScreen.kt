package com.synex.feature.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Tune
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
internal fun PrivacyScreen(onBack: () -> Unit, onContinue: () -> Unit) {
    OnboardingFrame(2, "Continue", onContinue, onBack) {
        item {
            Text("Your privacy", style = MaterialTheme.typography.labelLarge, color = SynexMuted)
            Text(
                "Your information stays yours.",
                style = MaterialTheme.typography.displaySmall,
                modifier = Modifier.padding(top = 10.dp),
            )
            Text(
                "We only use information needed to run your account, keep it secure, and meet our responsibilities.",
                style = MaterialTheme.typography.bodyLarge,
                color = SynexMuted,
                modifier = Modifier.padding(top = 14.dp),
            )
        }
        item { PrivacyPoint(Icons.Rounded.Fingerprint, "What we use", "Your profile, connected accounts, activity, and device security details.") }
        item { PrivacyPoint(Icons.Rounded.Lock, "How we protect it", "Access is limited, sensitive details are protected, and passwords stay with your sign-in provider.") }
        item { PrivacyPoint(Icons.Rounded.Tune, "Your choices", "You can ask to view, correct, export, or close your account information.") }
        item {
            Text(
                "You can revisit the full Privacy Notice at any time from your Account page.",
                style = MaterialTheme.typography.bodyMedium,
                color = SynexMuted,
                modifier = Modifier.padding(vertical = 6.dp),
            )
        }
    }
}

@Composable
private fun PrivacyPoint(icon: ImageVector, title: String, detail: String) {
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
