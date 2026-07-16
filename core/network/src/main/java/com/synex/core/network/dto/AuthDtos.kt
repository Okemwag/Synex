package com.synex.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DerivConnectUrlResponse(
    @SerialName("authorize_url") val authorizeUrl: String,
)
