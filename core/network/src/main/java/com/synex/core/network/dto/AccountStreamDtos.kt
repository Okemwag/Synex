package com.synex.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AccountStreamTicketResponse(
    val ticket: String = "",
    @SerialName("expires_in") val expiresIn: Int = 0,
)

@Serializable
data class AccountStreamTicketRequest(
    @SerialName("login_id") val loginId: String,
)

@Serializable
data class AccountStreamEventDto(
    val type: String = "",
    val status: String? = null,
    @SerialName("login_id") val loginId: String = "",
    val balance: AccountBalanceEventDto? = null,
    val position: AccountPositionEventDto? = null,
)

@Serializable
data class AccountBalanceEventDto(
    val amount: Double = 0.0,
    val currency: String = "",
    @SerialName("login_id") val loginId: String = "",
)

@Serializable
data class AccountPositionEventDto(
    @SerialName("contract_id") val contractId: Long = 0,
    @SerialName("contract_type") val contractType: String = "",
    val symbol: String = "",
    val status: String = "",
    @SerialName("buy_price") val buyPrice: Double = 0.0,
    @SerialName("current_spot") val currentSpot: Double = 0.0,
    val profit: Double = 0.0,
    @SerialName("profit_percentage") val profitPercentage: Double = 0.0,
    val payout: Double = 0.0,
    val currency: String = "",
    @SerialName("is_expired") val isExpired: Boolean = false,
    @SerialName("is_sold") val isSold: Boolean = false,
)
