package com.synex.feature.funding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.synex.core.data.SynexRepository
import com.synex.core.model.PaymentOperation
import com.synex.core.ui.customerMessage
import java.util.UUID
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FundingViewModel(private val repository: SynexRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(FundingUiState())
    val state: StateFlow<FundingUiState> = mutableState.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            mutableState.update { it.copy(isLoading = true, errorMessage = null, message = null) }
            runCatching {
                val capabilities = repository.fundingCapabilities()
                if (!capabilities.paymentEnabled) return@runCatching Loaded(capabilities)
                val accountCurrency = repository.accounts().firstOrNull { it.loginId == repository.activeLoginId.value }?.currency ?: "USD"
                val directory = repository.paymentAgentDirectory()
                val currency = directory.currencies.firstOrNull { it == accountCurrency } ?: directory.currencies.firstOrNull() ?: accountCurrency
                val wallets = repository.wallets(accountCurrency)
                val walletType = wallets.firstOrNull()?.type
                val page = walletType?.let { repository.walletTransactions(it) }
                Loaded(
                    capabilities = capabilities,
                    wallets = wallets,
                    walletType = walletType,
                    transactions = page?.transactions.orEmpty(),
                    nextCursor = page?.nextCursor,
                    previousCursor = page?.previousCursor,
                    currencies = directory.currencies,
                    countries = directory.countries,
                    currency = currency,
                    agents = repository.paymentAgents(currency),
                    settings = repository.paymentAgentSettings(),
                    ownAgent = repository.ownPaymentAgent(),
                )
            }.fold(
                onSuccess = { loaded ->
                    mutableState.update {
                        it.copy(
                            isLoading = false,
                            capabilities = loaded.capabilities,
                            wallets = loaded.wallets,
                            selectedWalletType = loaded.walletType,
                            transactions = loaded.transactions,
                            nextCursor = loaded.nextCursor,
                            previousCursor = loaded.previousCursor,
                            currencies = loaded.currencies,
                            countries = loaded.countries,
                            currency = loaded.currency,
                            agents = loaded.agents,
                            selectedAgentId = loaded.agents.firstOrNull()?.id,
                            settings = loaded.settings,
                            ownAgent = loaded.ownAgent,
                        )
                    }
                },
                onFailure = { error -> mutableState.update { it.copy(isLoading = false, errorMessage = error.customerMessage("load funding")) } },
            )
        }
    }

    fun selectWallet(walletType: String) = loadWalletPage(walletType, null)
    fun nextWalletPage() {
        val current = state.value
        if (current.selectedWalletType != null && current.nextCursor != null) loadWalletPage(current.selectedWalletType, current.nextCursor)
    }

    fun previousWalletPage() {
        val current = state.value
        if (current.selectedWalletType != null && current.previousCursor != null) loadWalletPage(current.selectedWalletType, current.previousCursor)
    }

    private fun loadWalletPage(walletType: String, cursor: String?) {
        viewModelScope.launch {
            mutableState.update { it.copy(isBusy = true, errorMessage = null, selectedWalletType = walletType) }
            runCatching { repository.walletTransactions(walletType, cursor) }.fold(
                onSuccess = { page -> mutableState.update { it.copy(isBusy = false, transactions = page.transactions, nextCursor = page.nextCursor, previousCursor = page.previousCursor) } },
                onFailure = { error -> mutableState.update { it.copy(isBusy = false, errorMessage = error.customerMessage("load wallet transactions")) } },
            )
        }
    }

    fun setCurrency(value: String) = mutableState.update { it.copy(currency = value.uppercase()) }
    fun setCountry(value: String) = mutableState.update { it.copy(country = value.lowercase()) }
    fun setAmount(value: String) = mutableState.update { it.copy(amount = value) }
    fun setVerificationCode(value: String) = mutableState.update { it.copy(verificationCode = value.filter(Char::isDigit).take(6)) }
    fun setRecipientNickname(value: String) = mutableState.update { it.copy(recipientNickname = value) }
    fun setTransferAmount(value: String) = mutableState.update { it.copy(transferAmount = value) }
    fun selectAgent(id: Long) = mutableState.update { it.copy(selectedAgentId = id) }

    fun searchAgents() {
        val current = state.value
        if (current.currency.length != 3) return fail("Enter a three-letter currency code.")
        viewModelScope.launch {
            mutableState.update { it.copy(isBusy = true, errorMessage = null) }
            runCatching { repository.paymentAgents(current.currency, current.country) }.fold(
                onSuccess = { agents -> mutableState.update { it.copy(isBusy = false, agents = agents, selectedAgentId = agents.firstOrNull()?.id) } },
                onFailure = { error -> mutableState.update { it.copy(isBusy = false, errorMessage = error.customerMessage("find payment agents")) } },
            )
        }
    }

    fun updatePrivacy(showRealName: Boolean) {
        viewModelScope.launch {
            mutableState.update { it.copy(isBusy = true, errorMessage = null) }
            runCatching { repository.updatePaymentAgentSettings(showRealName) }.fold(
                onSuccess = { settings -> mutableState.update { it.copy(isBusy = false, settings = settings, message = "Payment-agent privacy updated.") } },
                onFailure = { error -> mutableState.update { it.copy(isBusy = false, errorMessage = error.customerMessage("update payment-agent privacy")) } },
            )
        }
    }

    fun requestWithdrawalCode() {
        val current = state.value
        val agentId = current.selectedAgentId ?: return fail("Choose a payment agent first.")
        if (!validAmount(current.amount)) return fail("Enter a positive withdrawal amount.")
        viewModelScope.launch {
            mutableState.update { it.copy(isBusy = true, errorMessage = null, message = null) }
            runCatching { repository.requestWithdrawalCode(agentId, current.amount, current.currency) }.fold(
                onSuccess = { verification -> mutableState.update { it.copy(isBusy = false, verificationExpiresAt = verification.expiresAt, message = verification.message) } },
                onFailure = { error -> mutableState.update { it.copy(isBusy = false, errorMessage = error.customerMessage("request a verification code")) } },
            )
        }
    }

    fun askToWithdraw() {
        val current = state.value
        when {
            current.selectedAgentId == null -> fail("Choose a payment agent first.")
            !validAmount(current.amount) -> fail("Enter a positive withdrawal amount.")
            current.verificationCode.length != 6 -> fail("Enter the six-digit code sent by Deriv.")
            else -> mutableState.update { it.copy(confirmWithdrawal = true, errorMessage = null) }
        }
    }

    fun dismissWithdrawalConfirmation() = mutableState.update { it.copy(confirmWithdrawal = false) }

    fun confirmWithdrawal() {
        val current = state.value
        val agentId = current.selectedAgentId ?: return
        val requestId = UUID.randomUUID().toString()
        mutableState.update { it.copy(confirmWithdrawal = false) }
        viewModelScope.launch {
            mutableState.update { it.copy(isBusy = true, errorMessage = null, message = null) }
            runCatching { repository.paymentAgentWithdrawal(agentId, current.amount, current.currency, current.verificationCode, requestId) }.fold(
                onSuccess = { operation ->
                    mutableState.update { it.copy(isBusy = false, verificationCode = "", pendingOperation = operation, message = "Withdrawal ${operation.status}. Reference ${operation.requestId}.") }
                    poll(operation, withdrawal = true)
                },
                onFailure = { error -> mutableState.update { it.copy(isBusy = false, errorMessage = error.customerMessage("submit the withdrawal")) } },
            )
        }
    }

    fun validateTransfer() {
        val current = state.value
        if (current.recipientNickname.isBlank() || !validAmount(current.transferAmount)) return fail("Enter the recipient nickname and a positive amount.")
        val requestId = UUID.randomUUID().toString()
        viewModelScope.launch {
            mutableState.update { it.copy(isBusy = true, errorMessage = null, message = null) }
            runCatching { repository.paymentAgentTransfer(current.recipientNickname, current.transferAmount, current.currency, requestId, true) }.fold(
                onSuccess = { mutableState.update { it.copy(isBusy = false, confirmTransferRequestId = requestId) } },
                onFailure = { error -> mutableState.update { it.copy(isBusy = false, errorMessage = error.customerMessage("validate the transfer")) } },
            )
        }
    }

    fun dismissTransferConfirmation() = mutableState.update { it.copy(confirmTransferRequestId = null) }

    fun confirmTransfer() {
        val current = state.value
        val requestId = current.confirmTransferRequestId ?: return
        mutableState.update { it.copy(confirmTransferRequestId = null) }
        viewModelScope.launch {
            mutableState.update { it.copy(isBusy = true, errorMessage = null) }
            runCatching { repository.paymentAgentTransfer(current.recipientNickname, current.transferAmount, current.currency, requestId, false) }.fold(
                onSuccess = { operation ->
                    mutableState.update { it.copy(isBusy = false, pendingOperation = operation, message = "Transfer ${operation.status}. Reference ${operation.requestId}.") }
                    poll(operation, withdrawal = false)
                },
                onFailure = { error -> mutableState.update { it.copy(isBusy = false, errorMessage = error.customerMessage("complete the transfer")) } },
            )
        }
    }

    private suspend fun poll(initial: PaymentOperation, withdrawal: Boolean) {
        var operation = initial
        for (attempt in 0 until 15) {
            if (operation.status !in setOf("requested", "pending")) break
            delay(4_000)
            val next = runCatching {
                if (withdrawal) repository.paymentAgentWithdrawalStatus(operation.requestId)
                else repository.paymentAgentTransferStatus(operation.requestId)
            }.getOrNull() ?: break
            operation = next
            mutableState.update { state -> state.copy(pendingOperation = operation, message = "${if (withdrawal) "Withdrawal" else "Transfer"} ${operation.status}. Reference ${operation.requestId}.") }
        }
        state.value.selectedWalletType?.let { walletType ->
            runCatching { repository.walletTransactions(walletType) }.getOrNull()?.let { page ->
                mutableState.update { it.copy(transactions = page.transactions, nextCursor = page.nextCursor, previousCursor = page.previousCursor) }
            }
        }
    }

    private fun fail(message: String) { mutableState.update { it.copy(errorMessage = message, message = null) } }
    private fun validAmount(value: String) = value.toDoubleOrNull()?.let { it > 0 } == true

    companion object {
        fun factory(repository: SynexRepository): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = FundingViewModel(repository) as T
        }
    }
}

private data class Loaded(
    val capabilities: com.synex.core.model.FundingCapabilities,
    val wallets: List<com.synex.core.model.DerivWallet> = emptyList(),
    val walletType: String? = null,
    val transactions: List<com.synex.core.model.WalletTransaction> = emptyList(),
    val nextCursor: String? = null,
    val previousCursor: String? = null,
    val currencies: List<String> = emptyList(),
    val countries: List<String> = emptyList(),
    val currency: String = "USD",
    val agents: List<com.synex.core.model.PaymentAgent> = emptyList(),
    val settings: com.synex.core.model.PaymentAgentSettings? = null,
    val ownAgent: com.synex.core.model.PaymentAgent? = null,
)
