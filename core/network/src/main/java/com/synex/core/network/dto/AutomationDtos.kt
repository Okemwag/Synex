package com.synex.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AutomationStrategiesResponse(val strategies: List<AutomationStrategyDto> = emptyList())

@Serializable
data class AutomationStrategyDto(
    val id: String = "",
    @SerialName("login_id") val loginId: String = "",
    @SerialName("is_virtual") val isVirtual: Boolean = true,
    val name: String = "",
    val symbol: String = "",
    @SerialName("contract_type") val contractType: String = "",
    val currency: String = "USD",
    val amount: Double = 0.0,
    val basis: String = "stake",
    @SerialName("interval_seconds") val intervalSeconds: Int = 60,
    @SerialName("max_trades") val maxTrades: Int = 1,
    @SerialName("max_loss") val maxLoss: Double = 0.0,
    @SerialName("max_duration_minutes") val maxDurationMinutes: Int = 60,
    @SerialName("max_concurrent_positions") val maxConcurrentPositions: Int = 1,
    val enabled: Boolean = true,
)

@Serializable
data class AutomationRunsResponse(val runs: List<AutomationRunDto> = emptyList())

@Serializable
data class AutomationRunDto(
    val id: String = "",
    @SerialName("strategy_id") val strategyId: String = "",
    @SerialName("strategy_name") val strategyName: String = "",
    @SerialName("login_id") val loginId: String = "",
    val status: String = "",
    @SerialName("is_virtual") val isVirtual: Boolean = true,
    @SerialName("trade_count") val tradeCount: Int = 0,
    @SerialName("successful_trades") val successfulTrades: Int = 0,
    @SerialName("failed_trades") val failedTrades: Int = 0,
    @SerialName("committed_loss") val committedLoss: Double = 0.0,
    @SerialName("settled_trades") val settledTrades: Int = 0,
    @SerialName("realized_profit") val realizedProfit: Double = 0.0,
    @SerialName("started_at") val startedAt: String = "",
    @SerialName("next_execution_at") val nextExecutionAt: String = "",
    @SerialName("last_error") val lastError: String = "",
)

@Serializable
data class CreateAutomationStrategyRequest(
    @SerialName("login_id") val loginId: String,
    val name: String,
    val symbol: String,
    @SerialName("contract_type") val contractType: String,
    val currency: String,
    val amount: Double,
    val basis: String,
    val duration: Int,
    @SerialName("duration_unit") val durationUnit: String,
    @SerialName("interval_seconds") val intervalSeconds: Int,
    @SerialName("max_trades") val maxTrades: Int,
    @SerialName("max_loss") val maxLoss: Double,
    @SerialName("max_duration_minutes") val maxDurationMinutes: Int,
    @SerialName("max_concurrent_positions") val maxConcurrentPositions: Int,
)

@Serializable
data class StartAutomationRequest(
    @SerialName("strategy_id") val strategyId: String,
    @SerialName("real_money_confirmed") val realMoneyConfirmed: Boolean,
)

@Serializable
data class AutomationSafetyDto(@SerialName("kill_switch_enabled") val killSwitchEnabled: Boolean = false)

@Serializable
data class SetAutomationSafetyRequest(val enabled: Boolean)
