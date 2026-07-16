package com.synex.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OnboardingStatusDto(
    @SerialName("risk_acknowledged") val riskAcknowledged: Boolean = false,
    @SerialName("disclosure_version") val disclosureVersion: String = "",
)

@Serializable
data class RiskAcknowledgementRequest(
    val accepted: Boolean,
    @SerialName("disclosure_version") val disclosureVersion: String,
)

@Serializable
data class RiskAcknowledgementResponse(
    val accepted: Boolean = false,
    @SerialName("disclosure_version") val disclosureVersion: String = "",
)
