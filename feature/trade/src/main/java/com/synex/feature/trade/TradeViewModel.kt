package com.synex.feature.trade

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.synex.core.data.SynexRepository
import com.synex.core.data.TradeStatusPendingException
import com.synex.core.model.ContractOption
import com.synex.core.model.TradeRequest
import com.synex.core.ui.customerMessage
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import java.util.UUID
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TradeViewModel(private val repository: SynexRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(TradeUiState())
    val state: StateFlow<TradeUiState> = mutableState.asStateFlow()
    private var lastRequest: TradeRequest? = null
    private var quoteTimer: Job? = null

    init {
        refresh()
        viewModelScope.launch {
            repository.activeLoginId.collectLatest { loginId ->
                mutableState.update { current ->
                    current.copy(activeAccount = current.accounts.firstOrNull { it.loginId == loginId })
                }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            mutableState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                val accounts = repository.accounts()
                val markets = repository.markets()
                accounts to markets
            }.onSuccess { (accounts, markets) ->
                val active = accounts.firstOrNull { it.loginId == repository.activeLoginId.value }
                val symbol = mutableState.value.form.symbol.ifBlank { markets.firstOrNull()?.symbol.orEmpty() }
                mutableState.update {
                    it.copy(
                        isLoading = false,
                        accounts = accounts,
                        activeAccount = active,
                        markets = markets,
                        form = it.form.copy(symbol = symbol),
                    )
                }
                if (symbol.isNotBlank()) loadContracts(symbol)
            }.onFailure { error ->
                mutableState.update { it.copy(isLoading = false, errorMessage = error.customerMessage("load the trade ticket")) }
            }
        }
    }

    fun selectAccount(loginId: String) {
        repository.selectAccount(loginId)
        mutableState.update { current ->
            current.copy(
                activeAccount = current.accounts.firstOrNull { it.loginId == loginId },
                proposal = null,
                receipt = null,
            )
        }
    }

    fun selectSymbol(symbol: String) {
        clearQuote { it.copy(symbol = symbol, contractType = "") }
        loadContracts(symbol)
    }

    fun selectContract(contractType: String) = clearQuote { it.copy(contractType = contractType) }
    fun setAmount(value: String) = clearQuote { it.copy(amount = value) }
    fun setBasis(value: String) = clearQuote { it.copy(basis = value) }
    fun setExpiryMode(value: String) = clearQuote { it.copy(expiryMode = value) }
    fun setDuration(value: String) = clearQuote { it.copy(duration = value) }
    fun setDurationUnit(value: String) = clearQuote { it.copy(durationUnit = value) }
    fun setDateExpiry(value: String) = clearQuote { it.copy(dateExpiry = value) }
    fun setBarrier(value: String) = clearQuote { it.copy(barrier = value) }
    fun setBarrier2(value: String) = clearQuote { it.copy(barrier2 = value) }
    fun setMultiplier(value: String) = clearQuote { it.copy(multiplier = value) }
    fun setGrowthRate(value: String) = clearQuote { it.copy(growthRate = value) }
    fun setCancellation(value: String) = clearQuote { it.copy(cancellation = value) }
    fun setStopLoss(value: String) = clearQuote { it.copy(stopLoss = value) }
    fun setTakeProfit(value: String) = clearQuote { it.copy(takeProfit = value) }
    fun setPayoutPerPoint(value: String) = clearQuote { it.copy(payoutPerPoint = value) }
    fun setSelectedTick(value: String) = clearQuote { it.copy(selectedTick = value) }

    fun requestProposal() {
        val request = buildRequest() ?: return
        viewModelScope.launch {
            mutableState.update { it.copy(isBusy = true, errorMessage = null, successMessage = null, receipt = null) }
            runCatching { repository.proposal(request) }
                .onSuccess { proposal ->
                    lastRequest = request
                    mutableState.update { it.copy(isBusy = false, proposal = proposal, quoteSecondsRemaining = 30) }
                    startQuoteTimer()
                }
                .onFailure { error -> mutableState.update { it.copy(isBusy = false, errorMessage = error.customerMessage("get a live price")) } }
        }
    }

    fun purchase(realMoneyConfirmed: Boolean) {
        val current = mutableState.value
        val proposal = current.proposal ?: return
        val request = lastRequest ?: return
        if (current.activeAccount?.isVirtual != true && !realMoneyConfirmed) {
            mutableState.update { it.copy(errorMessage = "Confirm that this purchase uses real money before continuing.") }
            return
        }
        if (current.quoteSecondsRemaining <= 0) {
            mutableState.update { it.copy(errorMessage = "This price expired. A fresh price has been requested for your review.") }
            requestProposal()
            return
        }
        viewModelScope.launch {
            mutableState.update { it.copy(isBusy = true, errorMessage = null, successMessage = null) }
            runCatching {
                repository.buy(request, proposal, realMoneyConfirmed, UUID.randomUUID().toString())
            }.onSuccess { receipt ->
                quoteTimer?.cancel()
                mutableState.update { current ->
                    if (receipt.status == "succeeded") current.copy(
                            isBusy = false,
                            proposal = null,
                            quoteSecondsRemaining = 0,
                            receipt = receipt,
                            successMessage = "Trade placed${receipt.contractId?.let { id -> " · Contract $id" }.orEmpty()}.",
                        ) else current.copy(
                            isBusy = false,
                            proposal = null,
                            quoteSecondsRemaining = 0,
                            receipt = receipt,
                            purchaseLocked = receipt.status == "review" || receipt.status == "pending",
                            errorMessage = if (receipt.status == "failed") "Deriv did not accept this order." else "This order is still being reviewed. Do not place another trade on this account yet.",
                        )
                }
            }.onFailure { error ->
                mutableState.update {
                    it.copy(
                        isBusy = false,
                        purchaseLocked = error is TradeStatusPendingException,
                        errorMessage = if (error is TradeStatusPendingException) error.message else error.customerMessage("place this trade"),
                    )
                }
            }
        }
    }

    fun clearMessage() = mutableState.update { it.copy(errorMessage = null, successMessage = null) }

    private fun loadContracts(symbol: String) {
        viewModelScope.launch {
            mutableState.update { it.copy(isBusy = true, contracts = emptyList(), proposal = null) }
            runCatching { repository.contracts(symbol) }
                .onSuccess { contracts ->
                    mutableState.update {
                        it.copy(
                            isBusy = false,
                            contracts = contracts,
                            form = it.form.copy(contractType = contracts.firstOrNull()?.contractType.orEmpty()),
                        )
                    }
                }
                .onFailure { error -> mutableState.update { it.copy(isBusy = false, errorMessage = error.customerMessage("load available contracts")) } }
        }
    }

    private fun buildRequest(): TradeRequest? {
        val current = mutableState.value
        val form = current.form
        val account = current.activeAccount ?: return fail("Connect and select a Deriv account first.")
        val contract = current.selectedContract ?: return fail("Choose an available contract type.")
        val amount = form.amount.toDoubleOrNull()?.takeIf { it > 0 }
            ?: return fail("Enter a stake or payout greater than zero.")
        if (form.basis == "stake" && contract.minimumStake != null && amount < contract.minimumStake) {
            return fail("The minimum stake for this contract is ${contract.minimumStake}.")
        }
        if (form.basis == "stake" && contract.maximumStake != null && amount > contract.maximumStake) {
            return fail("The maximum stake for this contract is ${contract.maximumStake}.")
        }
        val duration = if (form.expiryMode == "duration") {
            form.duration.toIntOrNull()?.takeIf { it > 0 } ?: return fail("Enter a whole-number duration greater than zero.")
        } else null
        val dateExpiry = if (form.expiryMode == "date") parseExpiry(form.dateExpiry)
            ?: return fail("Enter a future expiry as YYYY-MM-DD HH:mm.") else null

        fun required(name: String, value: String): String? {
            return value.trim().ifBlank { null }
        }
        val requiredText = mapOf(
            "barrier" to form.barrier,
            "barrier2" to form.barrier2,
            "cancellation" to form.cancellation,
        )
        requiredText.entries.firstOrNull { (name, value) -> name in contract.requiredFields && value.isBlank() }
            ?.let { return fail("${it.key.displayField()} is required for this contract.") }
        val barrier = required("barrier", form.barrier)
        val barrier2 = required("barrier2", form.barrier2)
        val barrierPattern = Regex("^[+-]?[0-9]+\\.?[0-9]*$")
        if (barrier != null && (!barrierPattern.matches(barrier) || barrier.length > 20)) {
            return fail("Enter the barrier as a number, optionally beginning with + or -.")
        }
        if (barrier != null && contract.family == "digits" && !Regex("^[0-9]$").matches(barrier)) {
            return fail("Choose a predicted digit from 0 to 9.")
        }
        if (barrier2 != null && (!barrierPattern.matches(barrier2) || barrier2.length > 20)) {
            return fail("Enter the second barrier as a number, optionally beginning with + or -.")
        }
        if (duration != null) {
            val minimum = parseDurationLimit(contract.minimumDuration)
            val maximum = parseDurationLimit(contract.maximumDuration)
            if (minimum != null && minimum.second == form.durationUnit && duration < minimum.first) {
                return fail("The minimum duration for this contract is ${contract.minimumDuration}.")
            }
            if (maximum != null && maximum.second == form.durationUnit && duration > maximum.first) {
                return fail("The maximum duration for this contract is ${contract.maximumDuration}.")
            }
        }
        val multiplier = requiredNumber(contract, "multiplier", form.multiplier) ?: if ("multiplier" in contract.requiredFields) return null else null
        val growthRate = requiredNumber(contract, "growth_rate", form.growthRate) ?: if ("growth_rate" in contract.requiredFields) return null else null
        val selectedTick = requiredInt(contract, "selected_tick", form.selectedTick) ?: if ("selected_tick" in contract.requiredFields) return null else null
        val cancellation = required("cancellation", form.cancellation)
        if (cancellation != null && contract.cancellationChoices.isNotEmpty() && cancellation !in contract.cancellationChoices) {
            return fail("Choose an available cancellation duration: ${contract.cancellationChoices.joinToString()}.")
        }

        return TradeRequest(
            loginId = account.loginId,
            contractType = form.contractType,
            symbol = form.symbol,
            amount = amount,
            basis = form.basis,
            currency = account.currency,
            duration = duration,
            durationUnit = if (duration != null) form.durationUnit else null,
            dateExpiry = dateExpiry,
            barrier = barrier,
            barrier2 = barrier2,
            multiplier = multiplier,
            growthRate = growthRate,
            cancellation = cancellation,
            stopLoss = optionalPositive("stop loss", form.stopLoss) ?: if (form.stopLoss.isNotBlank()) return null else null,
            takeProfit = optionalPositive("take profit", form.takeProfit) ?: if (form.takeProfit.isNotBlank()) return null else null,
            payoutPerPoint = optionalPositive("payout per point", form.payoutPerPoint) ?: if (form.payoutPerPoint.isNotBlank()) return null else null,
            selectedTick = selectedTick,
        )
    }

    private fun requiredNumber(contract: ContractOption, name: String, raw: String): Double? {
        if (raw.isBlank()) {
            if (name in contract.requiredFields) fail("${name.displayField()} is required for this contract.")
            return null
        }
        val value = raw.toDoubleOrNull()?.takeIf { it > 0 }
            ?: return fail("Enter a valid ${name.displayField().lowercase()} greater than zero.")
        val choices = if (name == "multiplier") contract.multiplierChoices else contract.growthRateChoices
        if (choices.isNotEmpty() && choices.none { kotlin.math.abs(it - value) < 0.00000001 }) {
            return fail("Choose an available ${name.displayField().lowercase()}: ${choices.joinToString()}.")
        }
        return value
    }

    private fun requiredInt(contract: ContractOption, name: String, raw: String): Int? {
        if (raw.isBlank()) {
            if (name in contract.requiredFields) fail("${name.displayField()} is required for this contract.")
            return null
        }
        return raw.toIntOrNull()?.takeIf { it > 0 } ?: run { fail("Enter a valid ${name.displayField().lowercase()} greater than zero."); null }
    }

    private fun optionalPositive(label: String, raw: String): Double? {
        if (raw.isBlank()) return null
        return raw.toDoubleOrNull()?.takeIf { it > 0 } ?: run { fail("Enter a valid $label greater than zero."); null }
    }

    private fun parseExpiry(raw: String): Long? = runCatching {
        val parser = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).apply {
            isLenient = false
            timeZone = TimeZone.getDefault()
        }
        parser.parse(raw.trim())?.time?.div(1000)?.takeIf { it > System.currentTimeMillis() / 1000 }
    }.getOrNull()

    private fun parseDurationLimit(raw: String): Pair<Int, String>? {
        val match = Regex("^(\\d+)([tsmhd])?$").matchEntire(raw.trim()) ?: return null
        return match.groupValues[1].toIntOrNull()?.let { it to match.groupValues[2] }
    }

    private fun startQuoteTimer() {
        quoteTimer?.cancel()
        quoteTimer = viewModelScope.launch {
            while (mutableState.value.quoteSecondsRemaining > 0) {
                delay(1_000)
                mutableState.update { it.copy(quoteSecondsRemaining = (it.quoteSecondsRemaining - 1).coerceAtLeast(0)) }
            }
        }
    }

    private fun clearQuote(update: (TradeForm) -> TradeForm) {
        quoteTimer?.cancel()
        lastRequest = null
        mutableState.update {
            it.copy(form = update(it.form), proposal = null, quoteSecondsRemaining = 0, receipt = null, errorMessage = null)
        }
    }

    private fun fail(message: String): Nothing? {
        mutableState.update { it.copy(errorMessage = message) }
        return null
    }

    companion object {
        fun factory(repository: SynexRepository): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = TradeViewModel(repository) as T
        }
    }
}

private fun String.displayField() = replace('_', ' ').replaceFirstChar(Char::uppercase)
