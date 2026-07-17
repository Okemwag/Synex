package com.synex.core.data.mapper

import com.synex.core.model.Position
import com.synex.core.model.PositionDirection
import com.synex.core.network.dto.PositionDto

internal fun PositionDto.toDomain() = Position(
    contractId = contractId,
    symbol = symbol,
    displayName = longcode.ifBlank { symbol },
    direction = if (contractType.contains("PUT", ignoreCase = true)) {
        PositionDirection.DOWN
    } else {
        PositionDirection.UP
    },
    buyPrice = buyPrice,
    currentValue = null,
    profitLoss = null,
    currency = currency,
    purchaseEpochSeconds = purchaseTime,
)
