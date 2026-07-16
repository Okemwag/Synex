package com.synex.feature.account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Gavel
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.synex.core.data.SynexRepository
import com.synex.core.ui.ErrorState
import com.synex.core.ui.LoadingState
import com.synex.core.ui.PageHeading
import com.synex.core.ui.SectionHeading
import com.synex.core.ui.SynexMuted
import com.synex.core.ui.SynexPaper

@Composable
fun AccountRoute(
    repository: SynexRepository,
    onAuthenticationAction: () -> Unit,
    onLegalClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AccountViewModel = viewModel(factory = AccountViewModel.factory(repository)),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    AccountScreen(state, onAuthenticationAction, onLegalClick, viewModel::refresh, modifier)
}

@Composable
fun AccountScreen(
    state: AccountUiState,
    onAuthenticationAction: () -> Unit,
    onLegalClick: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize().background(SynexPaper),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item { PageHeading("Profile and security", "Account") }
        when {
            state.isLoading -> item { LoadingState() }
            state.errorMessage != null -> item { ErrorState(state.errorMessage, onRetry) }
            else -> {
                item { ProfileCard(state.accounts.firstOrNull(), onAuthenticationAction) }
                item { SectionHeading("Preferences") }
                item { SettingsCard(preferenceItems(onLegalClick)) }
                item { SectionHeading("Money movement") }
                item { SettingsCard(fundingItems) }
                item {
                    Text(
                        "Synex Mobile · Secure build",
                        style = MaterialTheme.typography.labelMedium,
                        color = SynexMuted,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
                    )
                }
            }
        }
    }
}

private fun preferenceItems(onLegalClick: () -> Unit) = listOf(
    SettingItem(Icons.Outlined.PersonOutline, "Personal information", "Identity and contact details"),
    SettingItem(Icons.Outlined.Shield, "Security", "Passkeys and trusted devices"),
    SettingItem(Icons.Outlined.NotificationsNone, "Notifications", "Trading and account alerts"),
    SettingItem(Icons.Outlined.Gavel, "Legal and privacy", "Policies, risks, and data rights", onLegalClick),
)

private val fundingItems = listOf(
    SettingItem(Icons.Outlined.CreditCard, "Deposit methods", "Payment gateway wiring follows"),
    SettingItem(Icons.Outlined.AccountBalanceWallet, "Withdrawals", "Payment gateway wiring follows"),
)
