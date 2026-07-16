package com.synex.core.data.mapper

import com.synex.core.model.Candle
import com.synex.core.model.MarketQuote
import com.synex.core.network.dto.CandleDto
import com.synex.core.network.dto.MarketSymbolDto

internal fun MarketSymbolDto.toDomain() = MarketQuote(
    symbol = symbol,
    displayName = displayName.ifBlank { symbol },
    market = market,
    price = quote,
    changePercent = changePercent,
    isOpen = exchangeIsOpen == 1,
)

internal fun CandleDto.toDomain() = Candle(
    epochSeconds = epoch,
    open = open,
    high = high,
    low = low,
    close = close,
)
