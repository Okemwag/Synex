package com.synex.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AccountsResponse(
    val accounts: List<AccountDto> = emptyList(),
)

@Serializable
data class AccountDto(
    @SerialName("login_id") val loginId: String = "",
    val currency: String = "USD",
    val balance: Double = 0.0,
    @SerialName("is_virtual") val isVirtual: Boolean = false,
    val status: String = "",
    @SerialName("account_type") val accountType: String = "",
    @SerialName("account_group") val accountGroup: String = "",
    val jurisdiction: String = "",
    @SerialName("landing_company") val landingCompany: String = "",
    @SerialName("ready_for_trading") val readyForTrading: Boolean = true,
    @SerialName("readiness_missing") val readinessMissing: List<String> = emptyList(),
    @SerialName("payment_scope") val paymentScope: Boolean = false,
    val live: LiveAccountDto? = null,
)

@Serializable
data class LiveAccountDto(
    val balance: Double = 0.0,
    val currency: String = "",
)

@Serializable
data class CreateOptionsAccountRequest(
    val currency: String = "USD",
    val group: String = "row",
    @SerialName("account_type") val accountType: String,
    @SerialName("real_money_confirmed") val realMoneyConfirmed: Boolean = false,
)
