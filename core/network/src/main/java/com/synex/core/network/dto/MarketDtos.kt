package com.synex.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SymbolsResponse(
    val data: List<MarketSymbolDto> = emptyList(),
)

@Serializable
data class MarketSymbolDto(
    @SerialName("underlying_symbol")
    val symbol: String = "",
    @SerialName("underlying_symbol_name")
    val displayName: String = "",
    val market: String = "",
    @SerialName("exchange_is_open")
    val exchangeIsOpen: Int = 1,
    @SerialName("is_trading_suspended")
    val isTradingSuspended: Int = 0,
)

@Serializable
data class CandlesResponse(
    val data: List<CandleDto> = emptyList(),
)

@Serializable
data class CandleDto(
    val epoch: Long = 0,
    val open: Double = 0.0,
    val high: Double = 0.0,
    val low: Double = 0.0,
    val close: Double = 0.0,
)
