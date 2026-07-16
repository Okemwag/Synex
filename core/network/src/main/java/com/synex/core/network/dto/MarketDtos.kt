package com.synex.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SymbolsResponse(
    val data: List<MarketSymbolDto> = emptyList(),
)

@Serializable
data class MarketSymbolDto(
    val symbol: String = "",
    @SerialName("display_name")
    val displayName: String = "",
    val market: String = "",
    val quote: Double = 0.0,
    @SerialName("change_percent")
    val changePercent: Double = 0.0,
    @SerialName("exchange_is_open") 
    val exchangeIsOpen: Int = 1,
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
