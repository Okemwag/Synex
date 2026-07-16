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
    val live: LiveAccountDto? = null,
)

@Serializable
data class LiveAccountDto(
    val balance: Double = 0.0,
    val currency: String = "",
)
