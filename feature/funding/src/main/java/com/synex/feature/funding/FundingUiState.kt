package com.synex.feature.funding

import com.synex.core.model.DerivWallet
import com.synex.core.model.FundingCapabilities
import com.synex.core.model.PaymentAgent
import com.synex.core.model.PaymentAgentSettings
import com.synex.core.model.PaymentOperation
import com.synex.core.model.WalletTransaction

data class FundingUiState(
    val isLoading: Boolean = true,
    val isBusy: Boolean = false,
    val capabilities: FundingCapabilities? = null,
    val wallets: List<DerivWallet> = emptyList(),
    val selectedWalletType: String? = null,
    val transactions: List<WalletTransaction> = emptyList(),
    val nextCursor: String? = null,
    val previousCursor: String? = null,
    val currencies: List<String> = emptyList(),
    val countries: List<String> = emptyList(),
    val currency: String = "USD",
    val country: String = "",
    val agents: List<PaymentAgent> = emptyList(),
    val selectedAgentId: Long? = null,
    val settings: PaymentAgentSettings? = null,
    val ownAgent: PaymentAgent? = null,
    val amount: String = "",
    val verificationCode: String = "",
    val verificationExpiresAt: Long = 0,
    val recipientNickname: String = "",
    val transferAmount: String = "",
    val pendingOperation: PaymentOperation? = null,
    val confirmWithdrawal: Boolean = false,
    val confirmTransferRequestId: String? = null,
    val message: String? = null,
    val errorMessage: String? = null,
)
