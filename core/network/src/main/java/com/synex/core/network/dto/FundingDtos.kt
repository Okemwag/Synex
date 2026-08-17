package com.synex.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class JsonElementResponse(val data: JsonElement? = null)

@Serializable
data class FundingCapabilitiesDto(
    val connected: Boolean = false,
    val scopes: List<String> = emptyList(),
    @SerialName("payment_enabled") val paymentEnabled: Boolean = false,
    @SerialName("reconnect_required") val reconnectRequired: Boolean = false,
)

@Serializable
data class WalletsResponse(val data: List<WalletDto> = emptyList())

@Serializable
data class WalletDto(
    @SerialName("wallet_id") val walletId: String = "",
    val type: String = "main",
    val balances: Map<String, WalletBalanceDto> = emptyMap(),
    @SerialName("total_balance") val totalBalance: WalletTotalDto? = null,
)

@Serializable
data class WalletBalanceDto(
    val balance: String = "0",
    val input: String = "0",
    val output: String = "0",
)

@Serializable
data class WalletTotalDto(
    @SerialName("converted_to") val convertedTo: String = "",
    @SerialName("approximate_total_balance") val approximateTotalBalance: String = "0",
)

@Serializable
data class WalletTransactionsResponse(
    val data: WalletTransactionsDataDto = WalletTransactionsDataDto(),
    val links: WalletLinksDto = WalletLinksDto(),
)

@Serializable
data class WalletTransactionsDataDto(val transactions: List<WalletTransactionDto> = emptyList())

@Serializable
data class WalletLinksDto(
    val next: String? = null,
    val prev: String? = null,
    val first: String? = null,
)

@Serializable
data class WalletTransactionDto(
    @SerialName("request_id") val requestId: String = "",
    @SerialName("transaction_id") val transactionId: Long = 0,
    val timestamp: String = "",
    val category: String = "",
    val channel: String = "",
    val metadata: WalletTransactionMetadataDto = WalletTransactionMetadataDto(),
)

@Serializable
data class WalletTransactionMetadataDto(
    @SerialName("transaction_status") val transactionStatus: String = "",
    @SerialName("transaction_gross_amount") val transactionGrossAmount: String = "0",
    @SerialName("transaction_net_amount") val transactionNetAmount: String = "0",
    @SerialName("transaction_currency") val transactionCurrency: String = "",
)

@Serializable
data class PaymentAgentStatisticsResponse(val data: PaymentAgentStatisticsDto = PaymentAgentStatisticsDto())

@Serializable
data class PaymentAgentStatisticsDto(
    @SerialName("available_countries") val availableCountries: List<String> = emptyList(),
    @SerialName("available_currencies") val availableCurrencies: List<String> = emptyList(),
)

@Serializable
data class PaymentAgentsResponse(val data: List<PaymentAgentDto> = emptyList())

@Serializable
data class PaymentAgentResponse(val data: PaymentAgentDto = PaymentAgentDto())

@Serializable
data class PaymentAgentDto(
    val id: Long = 0,
    val name: String? = null,
    val nickname: String? = null,
    val information: String? = null,
    val email: String? = null,
    @SerialName("phone_numbers") val phoneNumbers: List<String>? = null,
    @SerialName("payment_methods") val paymentMethods: List<String>? = null,
    val countries: List<String>? = null,
    val urls: List<String>? = null,
    @SerialName("deposit_commission") val depositCommission: Double? = null,
    @SerialName("withdrawal_commission") val withdrawalCommission: Double? = null,
    @SerialName("withdrawal_minimum") val withdrawalMinimum: String? = null,
    @SerialName("withdrawal_maximum") val withdrawalMaximum: String? = null,
)

@Serializable
data class PaymentAgentSettingsResponse(val data: PaymentAgentSettingsDto = PaymentAgentSettingsDto())

@Serializable
data class PaymentAgentSettingsDto(
    @SerialName("deposit_enabled") val depositEnabled: Boolean = false,
    @SerialName("withdraw_enabled") val withdrawEnabled: Boolean = false,
    @SerialName("show_real_name") val showRealName: Boolean = false,
)

@Serializable
data class UpdatePaymentAgentSettingsRequest(@SerialName("show_real_name") val showRealName: Boolean)

@Serializable
data class PaymentAgentTransferRequest(
    @SerialName("to_nickname") val toNickname: String,
    val amount: String,
    val currency: String,
    @SerialName("request_id") val requestId: String,
    @SerialName("dry_run") val dryRun: Boolean = false,
)

@Serializable
data class PaymentOperationResponse(val data: PaymentOperationDto = PaymentOperationDto())

@Serializable
data class PaymentOperationDto(
    val status: String = "",
    @SerialName("transaction_id") val transactionId: Long? = null,
)

@Serializable
data class WithdrawalVerificationRequest(
    @SerialName("agent_id") val agentId: Long,
    val amount: String,
    val currency: String,
)

@Serializable
data class WithdrawalVerificationResponse(val data: WithdrawalVerificationDto = WithdrawalVerificationDto())

@Serializable
data class WithdrawalVerificationDto(
    val message: String = "",
    @SerialName("next_request_at") val nextRequestAt: Long = 0,
    @SerialName("expires_at") val expiresAt: Long = 0,
)

@Serializable
data class PaymentAgentWithdrawalRequest(
    @SerialName("agent_id") val agentId: Long,
    val amount: String,
    val currency: String,
    @SerialName("verification_code") val verificationCode: String,
    @SerialName("request_id") val requestId: String,
)
