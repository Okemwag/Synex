package com.synex.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.synex.core.ui.SynexCard
import com.synex.core.ui.SynexMuted
import com.synex.core.ui.SynexPaper
import com.synex.core.ui.SynexWordmark

@Composable
fun AuthScreen(state: AuthUiState, onLogin: () -> Unit) {
    Column(
        Modifier.fillMaxSize().background(SynexPaper).padding(24.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        SynexWordmark()
        Spacer(Modifier.height(44.dp))
        Text("Secure access", style = MaterialTheme.typography.labelLarge, color = SynexMuted)
        Text(
            "Your trading workspace, protected.",
            style = MaterialTheme.typography.displaySmall,
            modifier = Modifier.padding(top = 10.dp),
        )
        Text(
            "Sign in through Auth0. Synex never receives or stores your password.",
            style = MaterialTheme.typography.bodyLarge,
            color = SynexMuted,
            modifier = Modifier.padding(top = 14.dp),
        )
        Spacer(Modifier.height(28.dp))
        SynexCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(18.dp)) {
                Icon(Icons.Outlined.Lock, null)
                Text(
                    "Universal Login uses Authorization Code with PKCE and returns securely to this app.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 12.dp),
                )
            }
        }
        state.errorMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 16.dp))
        }
        if (!state.configured) {
            Text(
                "A Native Auth0 application client ID is required in local Gradle properties.",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 16.dp),
            )
        }
        Button(
            onClick = onLogin,
            enabled = state.configured && !state.isLoading,
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp).height(54.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF111310)),
        ) {
            if (state.isLoading) CircularProgressIndicator(color = Color.White)
            else {
                Text("Continue to secure sign in")
                Icon(Icons.AutoMirrored.Outlined.ArrowForward, null, Modifier.padding(start = 10.dp))
            }
        }
        Text(
            "Review the Synex legal centre and trading disclosures before using live services.",
            style = MaterialTheme.typography.bodySmall,
            color = SynexMuted,
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 18.dp),
        )
    }
}
