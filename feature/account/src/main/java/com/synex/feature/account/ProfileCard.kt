package com.synex.feature.account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.synex.core.model.TradingAccount
import com.synex.core.ui.SynexCard
import com.synex.core.ui.SynexGreen
import com.synex.core.ui.SynexInk
import com.synex.core.ui.formatMoney

@Composable
internal fun ProfileCard(
    account: TradingAccount?,
    isConnecting: Boolean,
    connectionMessage: String?,
    onConnectDeriv: () -> Unit,
    onAuthenticationAction: () -> Unit,
) {
    SynexCard(Modifier.fillMaxWidth(), dark = true) {
        Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Your Synex account", style = MaterialTheme.typography.titleLarge)
                    Text(
                        when {
                            account == null -> "No Deriv account connected"
                            account.isVirtual -> "Demo trading"
                            else -> "Live trading"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = SynexGreen,
                    )
                }
                Icon(Icons.Outlined.Lock, null, tint = SynexGreen)
            }
            if (account == null) {
                Text(
                    "Connect a Deriv demo or live trading account to load your balance, portfolio, and positions.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.78f),
                )
                connectionMessage?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = SynexGreen)
                }
                Button(
                    onClick = onConnectDeriv,
                    enabled = !isConnecting,
                    modifier = Modifier.fillMaxWidth(),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(Color.White, SynexInk),
                ) {
                    if (isConnecting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = SynexInk,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text("Connect Deriv account")
                    }
                }
                TextButton(onClick = onAuthenticationAction, modifier = Modifier.fillMaxWidth()) {
                    Text("Sign out", color = Color.White)
                }
            } else {
                AccountBalance(account)
                connectionMessage?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = SynexGreen)
                }
                Button(
                    onClick = onAuthenticationAction,
                    modifier = Modifier.fillMaxWidth(),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(Color.White, SynexInk),
                ) {
                    Text("Sign out")
                }
            }
        }
    }
}

@Composable
private fun AccountBalance(account: TradingAccount) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Column {
            BalanceLabel("DERIV ACCOUNT")
            Text(account.loginId, style = MaterialTheme.typography.titleMedium)
        }
        Column(horizontalAlignment = Alignment.End) {
            BalanceLabel("BALANCE")
            Text(formatMoney(account.balance, account.currency), style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun BalanceLabel(value: String) {
    Text(value, style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.5f))
}
