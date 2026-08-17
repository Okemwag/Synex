package com.synex.core.network.dto

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive

object FlexibleDoubleSerializer : KSerializer<Double> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("FlexibleDouble", PrimitiveKind.DOUBLE)
    override fun deserialize(decoder: Decoder): Double =
        (decoder as? kotlinx.serialization.json.JsonDecoder)?.decodeJsonElement()?.jsonPrimitive?.content?.toDoubleOrNull() ?: decoder.decodeDouble()
    override fun serialize(encoder: Encoder, value: Double) = encoder.encodeDouble(value)
}

object FlexibleNullableDoubleSerializer : KSerializer<Double?> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("FlexibleNullableDouble", PrimitiveKind.STRING)
    override fun deserialize(decoder: Decoder): Double? {
        val json = decoder as? kotlinx.serialization.json.JsonDecoder ?: return decoder.decodeDouble()
        val value = json.decodeJsonElement()
        return (value as? JsonPrimitive)?.content?.toDoubleOrNull()
    }
    override fun serialize(encoder: Encoder, value: Double?) {
        if (value == null) encoder.encodeString("") else encoder.encodeDouble(value)
    }
}

object FlexibleBooleanSerializer : KSerializer<Boolean> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("FlexibleBoolean", PrimitiveKind.BOOLEAN)
    override fun deserialize(decoder: Decoder): Boolean {
        val json = decoder as? kotlinx.serialization.json.JsonDecoder ?: return decoder.decodeBoolean()
        return when (json.decodeJsonElement().jsonPrimitive.content.lowercase()) {
            "true", "1" -> true
            else -> false
        }
    }
    override fun serialize(encoder: Encoder, value: Boolean) = encoder.encodeBoolean(value)
}

@Serializable
data class ContractsResponse(val data: ContractsDataDto = ContractsDataDto())

@Serializable
data class ContractsDataDto(val available: List<ContractOptionDto> = emptyList())

@Serializable
data class ContractOptionDto(
    @SerialName("contract_type") val contractType: String = "",
    @SerialName("contract_display") val displayName: String = "",
    @SerialName("min_contract_duration") val minimumDuration: String = "",
    @SerialName("max_contract_duration") val maximumDuration: String = "",
    @SerialName("min_stake") @Serializable(with = FlexibleNullableDoubleSerializer::class) val minimumStake: Double? = null,
    @SerialName("max_stake") @Serializable(with = FlexibleNullableDoubleSerializer::class) val maximumStake: Double? = null,
    @SerialName("multiplier_range") val multiplierChoices: List<Double> = emptyList(),
    @SerialName("growth_rate_range") val growthRateChoices: List<Double> = emptyList(),
    @SerialName("cancellation_range") val cancellationChoices: List<JsonPrimitive> = emptyList(),
    @SerialName("synex_rules") val rules: ContractRulesDto = ContractRulesDto(),
)

@Serializable
data class ContractRulesDto(
    val family: String = "options",
    @SerialName("required_fields") val requiredFields: List<String> = emptyList(),
    @SerialName("optional_fields") val optionalFields: List<String> = emptyList(),
)

@Serializable
data class ProposalRequestDto(
    @SerialName("login_id") val loginId: String,
    @SerialName("contract_type") val contractType: String,
    val symbol: String,
    val amount: Double,
    val basis: String,
    val currency: String,
    val duration: Int? = null,
    @SerialName("duration_unit") val durationUnit: String? = null,
    @SerialName("date_expiry") val dateExpiry: Long? = null,
    val barrier: String? = null,
    val barrier2: String? = null,
    val multiplier: Double? = null,
    @SerialName("growth_rate") val growthRate: Double? = null,
    val cancellation: String? = null,
    @SerialName("stop_loss") val stopLoss: Double? = null,
    @SerialName("take_profit") val takeProfit: Double? = null,
    @SerialName("payout_per_point") val payoutPerPoint: Double? = null,
    @SerialName("selected_tick") val selectedTick: Int? = null,
)

@Serializable
data class ProposalResponse(val data: ProposalDto = ProposalDto())

@Serializable
data class ProposalDto(
    val id: String = "",
    @SerialName("ask_price") @Serializable(with = FlexibleDoubleSerializer::class) val askPrice: Double = 0.0,
    @Serializable(with = FlexibleNullableDoubleSerializer::class) val payout: Double? = null,
    val longcode: String = "",
    @SerialName("synex_expires_at") val expiresAt: String = "",
    @SerialName("maximum_loss") @Serializable(with = FlexibleDoubleSerializer::class) val maximumLoss: Double = 0.0,
)

@Serializable
data class BuyRequestDto(
    @SerialName("login_id") val loginId: String,
    @SerialName("proposal_id") val proposalId: String,
    @SerialName("max_price") val maxPrice: Double,
    val symbol: String,
    @SerialName("contract_type") val contractType: String,
    val currency: String,
    @SerialName("real_money_confirmed") val realMoneyConfirmed: Boolean,
)

@Serializable
data class JsonDataResponse(val data: JsonObject = JsonObject(emptyMap()))

@Serializable
data class OrderStatusResponse(
    val status: String = "pending",
    @SerialName("contract_id") val contractId: Long = 0,
)

@Serializable
data class ReceiptResponse(
    val data: ReceiptDto = ReceiptDto(),
    @SerialName("fee_disclosure") val feeDisclosure: String = "",
)

@Serializable
data class ReceiptDto(
    @SerialName("order_reference") val orderReference: String = "",
    @SerialName("login_id") val loginId: String = "",
    @SerialName("is_virtual") val isVirtual: Boolean = false,
    val status: String = "",
    val symbol: String = "",
    @SerialName("contract_type") val contractType: String = "",
    val currency: String = "USD",
    @SerialName("purchase_price") val purchasePrice: Double = 0.0,
    @SerialName("maximum_loss") val maximumLoss: Double = 0.0,
    @SerialName("potential_payout") val potentialPayout: Double = 0.0,
    @SerialName("contract_id") val contractId: Long? = null,
    @SerialName("provider_transaction_id") val transactionId: Long? = null,
    val longcode: String = "",
)

@Serializable
data class ContractActionRequestDto(
    @SerialName("login_id") val loginId: String,
    @SerialName("contract_id") val contractId: Long,
)

@Serializable
data class SellRequestDto(
    @SerialName("login_id") val loginId: String,
    @SerialName("contract_id") val contractId: Long,
    val price: Double = 0.0,
    val currency: String = "",
)

@Serializable
data class ContractUpdateRequestDto(
    @SerialName("login_id") val loginId: String,
    @SerialName("contract_id") val contractId: Long,
    @SerialName("stop_loss") val stopLoss: Double? = null,
    @SerialName("take_profit") val takeProfit: Double? = null,
)

@Serializable
data class PositionResponse(val data: PositionStatusDto = PositionStatusDto())

@Serializable
data class PositionStatusDto(
    @SerialName("contract_id") val contractId: Long = 0,
    @SerialName("contract_type") val contractType: String = "",
    @SerialName("underlying") val symbol: String = "",
    val longcode: String = "",
    val status: String = "",
    @SerialName("buy_price") @Serializable(with = FlexibleDoubleSerializer::class) val buyPrice: Double = 0.0,
    @SerialName("current_spot") @Serializable(with = FlexibleDoubleSerializer::class) val currentSpot: Double = 0.0,
    @Serializable(with = FlexibleDoubleSerializer::class) val profit: Double = 0.0,
    @SerialName("profit_percentage") @Serializable(with = FlexibleDoubleSerializer::class) val profitPercentage: Double = 0.0,
    val currency: String = "USD",
    @SerialName("is_expired") @Serializable(with = FlexibleBooleanSerializer::class) val isExpired: Boolean = false,
    @SerialName("is_sold") @Serializable(with = FlexibleBooleanSerializer::class) val isSold: Boolean = false,
)

@Serializable
data class JsonRowsResponse(val data: List<JsonObject> = emptyList())

@Serializable
data class ActivityResponse(val data: ActivityDataDto = ActivityDataDto())

@Serializable
data class ActivityDataDto(
    val transactions: List<JsonObject> = emptyList(),
    val count: Int = 0,
)
