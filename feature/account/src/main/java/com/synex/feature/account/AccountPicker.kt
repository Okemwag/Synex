package com.synex.feature.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.synex.core.model.TradingAccount
import com.synex.core.ui.SynexCard

@Composable
internal fun AccountPicker(
    accounts: List<TradingAccount>,
    selectedLoginId: String?,
    onSelected: (String) -> Unit,
) {
    SynexCard(Modifier.fillMaxWidth()) {
        Column {
            accounts.forEach { account ->
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    RadioButton(
                        selected = account.loginId == selectedLoginId,
                        onClick = { onSelected(account.loginId) },
                    )
                    Column(Modifier.weight(1f)) {
                        Text(account.loginId)
                        Text(if (account.isVirtual) "Demo account" else "Live account")
                    }
                    Text(account.currency)
                }
            }
        }
    }
}
