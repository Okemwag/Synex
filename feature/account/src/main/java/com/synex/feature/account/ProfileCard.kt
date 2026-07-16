package com.synex.feature.account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.synex.core.model.TradingAccount
import com.synex.core.ui.SynexCard
import com.synex.core.ui.SynexGreen
import com.synex.core.ui.SynexGreenSoft
import com.synex.core.ui.SynexInk
import com.synex.core.ui.formatMoney

@Composable
internal fun ProfileCard(
    account: TradingAccount?,
    onAuthenticationAction: () -> Unit,
) {
    SynexCard(Modifier.fillMaxWidth(), dark = true) {
        Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(52.dp).background(SynexGreenSoft, CircleShape), Alignment.Center) {
                    Text("SO", style = MaterialTheme.typography.titleLarge, color = SynexGreen)
                }
                Column(Modifier.padding(start = 13.dp).weight(1f)) {
                    Text("Synex Investor", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "Authenticated session",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.58f),
                    )
                }
                Icon(Icons.Outlined.Lock, null, tint = SynexGreen)
            }
            account?.let { AccountBalance(it) }
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
