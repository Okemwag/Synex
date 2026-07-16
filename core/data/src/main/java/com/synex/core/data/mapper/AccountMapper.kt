package com.synex.core.data.mapper

import com.synex.core.model.TradingAccount
import com.synex.core.network.dto.AccountDto

internal fun AccountDto.toDomain() = TradingAccount(
    loginId = loginId,
    currency = live?.currency?.takeIf(String::isNotBlank) ?: currency,
    balance = live?.balance ?: balance,
    isVirtual = isVirtual,
)
