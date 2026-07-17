package com.synex.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PortfolioResponse(
    val data: PortfolioDto = PortfolioDto(),
)

@Serializable
data class PortfolioDto(
    val contracts: List<PositionDto> = emptyList(),
)

@Serializable
data class PositionDto(
    @SerialName("contract_id") val contractId: Long = 0,
    @SerialName("underlying_symbol")
    val symbol: String = "",
    val longcode: String = "",
    @SerialName("contract_type") val contractType: String = "CALL",
    @SerialName("buy_price") val buyPrice: Double = 0.0,
    val payout: Double = 0.0,
    val currency: String = "USD",
    @SerialName("purchase_time") val purchaseTime: Long = 0,
    @SerialName("expiry_time") val expiryTime: Long = 0,
)
