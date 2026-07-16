package com.synex.feature.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ShowChart
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.synex.core.ui.PremiumIcon
import com.synex.core.ui.SynexCard
import com.synex.core.ui.SynexMuted

@Composable
internal fun RiskScreen(
    checked: Boolean,
    isSaving: Boolean,
    errorMessage: String?,
    onCheckedChange: (Boolean) -> Unit,
    onBack: () -> Unit,
    onAccept: () -> Unit,
) {
    OnboardingFrame(
        step = 3,
        buttonLabel = "I understand — continue",
        onButtonClick = onAccept,
        onBack = onBack,
        buttonEnabled = checked,
        isSaving = isSaving,
    ) {
        item {
            PremiumIcon(Icons.AutoMirrored.Rounded.ShowChart, null)
            Text(
                "Trading can result in loss.",
                style = MaterialTheme.typography.displaySmall,
                modifier = Modifier.padding(top = 18.dp),
            )
            Text(
                "Prices can move quickly. Only trade with money you can afford to lose.",
                style = MaterialTheme.typography.bodyLarge,
                color = SynexMuted,
                modifier = Modifier.padding(top = 14.dp),
            )
        }
        item { RiskPoints() }
        item {
            SynexCard(Modifier.fillMaxWidth()) {
                Row(
                    Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Checkbox(checked, onCheckedChange)
                    Text(
                        "I have read and understood this risk disclosure.",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(top = 11.dp),
                    )
                }
            }
        }
        if (errorMessage != null) item {
            Text(errorMessage, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun RiskPoints() {
    SynexCard(Modifier.fillMaxWidth(), dark = true) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
            Text("Keep in mind", style = MaterialTheme.typography.titleLarge)
            Text("• You may lose the full amount placed on a trade.")
            Text("• Results from a demo account do not predict live results.")
            Text("• Past performance does not guarantee future returns.")
            Text("• Synex does not provide personal investment advice.")
        }
    }
}
