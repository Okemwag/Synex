package com.synex.core.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.request.accept
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.prepareGet
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import com.synex.core.network.dto.AccountsResponse
import com.synex.core.network.dto.CandlesResponse
import com.synex.core.network.dto.DerivConnectUrlResponse
import com.synex.core.network.dto.PortfolioResponse
import com.synex.core.network.dto.SymbolsResponse
import com.synex.core.network.dto.OnboardingStatusDto
import com.synex.core.network.dto.RiskAcknowledgementRequest
import com.synex.core.network.dto.RiskAcknowledgementResponse
import com.synex.core.network.dto.AccountStreamEventDto
import com.synex.core.network.dto.AccountStreamTicketRequest
import com.synex.core.network.dto.AccountStreamTicketResponse
import com.synex.core.network.dto.ActivityResponse
import com.synex.core.network.dto.BuyRequestDto
import com.synex.core.network.dto.ContractActionRequestDto
import com.synex.core.network.dto.ContractsResponse
import com.synex.core.network.dto.ContractUpdateRequestDto
import com.synex.core.network.dto.JsonDataResponse
import com.synex.core.network.dto.JsonRowsResponse
import com.synex.core.network.dto.PositionResponse
import com.synex.core.network.dto.OrderStatusResponse
import com.synex.core.network.dto.ProposalRequestDto
import com.synex.core.network.dto.ProposalResponse
import com.synex.core.network.dto.ReceiptResponse
import com.synex.core.network.dto.SellRequestDto
import io.ktor.utils.io.readUTF8Line
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.contentOrNull

class SynexApiClient private constructor(
    private val tokenProvider: AccessTokenProvider,
    private val client: HttpClient,
) {
    constructor(baseUrl: String, tokenProvider: AccessTokenProvider) : this(
        tokenProvider = tokenProvider,
        client = defaultHttpClient(baseUrl),
    )

    suspend fun accounts(): AccountsResponse = authenticatedGet(ApiRoutes.ACCOUNTS)

    suspend fun symbols(): SymbolsResponse = publicGet(ApiRoutes.SYMBOLS)

    suspend fun candles(
        symbol: String,
        granularitySeconds: Int = ApiDefaults.CANDLE_GRANULARITY_SECONDS,
        count: Int = ApiDefaults.CANDLE_COUNT,
    ): CandlesResponse = publicGet(ApiRoutes.CANDLES) {
        parameter("symbol", symbol)
        parameter("granularity", granularitySeconds)
        parameter("count", count)
    }

    suspend fun portfolio(loginId: String): PortfolioResponse =
        authenticatedGet(ApiRoutes.PORTFOLIO) { parameter("login_id", loginId) }

    fun accountStream(loginId: String): Flow<AccountStreamEventDto> = flow {
        while (currentCoroutineContext().isActive) {
            try {
                val ticket = authenticatedPost<AccountStreamTicketResponse, AccountStreamTicketRequest>(
                    ApiRoutes.ACCOUNT_STREAM_TICKET,
                    AccountStreamTicketRequest(loginId),
                ).ticket
                if (ticket.isBlank()) throw IllegalStateException("The live account stream is unavailable.")

                client.prepareGet(ApiRoutes.ACCOUNT_STREAM) {
                    parameter("ticket", ticket)
                    accept(ContentType.Text.EventStream)
                }.execute { response ->
                    val channel = response.bodyAsChannel()
                    while (!channel.isClosedForRead && currentCoroutineContext().isActive) {
                        val line = channel.readUTF8Line() ?: break
                        if (!line.startsWith("data:")) continue
                        val payload = line.removePrefix("data:").trim()
                        if (payload.isEmpty()) continue
                        runCatching { streamJson.decodeFromString<AccountStreamEventDto>(payload) }
                            .getOrNull()
                            ?.let { emit(it) }
                    }
                }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                emit(AccountStreamEventDto(type = "status", status = "reconnecting", loginId = loginId))
            }

            if (currentCoroutineContext().isActive) delay(3_000)
        }
    }

    suspend fun derivConnectUrl(): DerivConnectUrlResponse =
        authenticatedGet(ApiRoutes.DERIV_CONNECT_URL) {
            parameter("return_to", ANDROID_DERIV_RETURN_URL)
        }

    suspend fun onboardingStatus(): OnboardingStatusDto =
        authenticatedGet(ApiRoutes.ONBOARDING_STATUS)

    suspend fun acknowledgeRisk(disclosureVersion: String): RiskAcknowledgementResponse =
        authenticatedPost(
            ApiRoutes.RISK_ACKNOWLEDGEMENT,
            RiskAcknowledgementRequest(
                accepted = true,
                disclosureVersion = disclosureVersion,
            ),
        )

    suspend fun contracts(symbol: String): ContractsResponse = publicGet(ApiRoutes.CONTRACTS) {
        parameter("symbol", symbol)
    }

    suspend fun proposal(request: ProposalRequestDto): ProposalResponse =
        authenticatedPost(ApiRoutes.PROPOSAL, request)

    suspend fun buy(request: BuyRequestDto, instructionKey: String): JsonDataResponse {
        val token = tokenProvider.accessToken() ?: throw MissingAccessTokenException()
        return client.post(ApiRoutes.BUY) {
            bearerAuth(token)
            header("Idempotency-Key", instructionKey)
            setBody(request)
        }.body()
    }

    suspend fun receipt(instructionKey: String): ReceiptResponse = authenticatedGet(ApiRoutes.RECEIPT) {
        parameter("idempotency_key", instructionKey)
    }

    suspend fun orderStatus(instructionKey: String): OrderStatusResponse = authenticatedGet(ApiRoutes.ORDER_STATUS) {
        parameter("idempotency_key", instructionKey)
    }

    suspend fun position(loginId: String, contractId: Long): PositionResponse = authenticatedGet(ApiRoutes.POSITION) {
        parameter("login_id", loginId)
        parameter("contract_id", contractId)
    }

    suspend fun sell(request: SellRequestDto): JsonDataResponse = authenticatedPost(ApiRoutes.SELL, request)

    suspend fun cancel(request: ContractActionRequestDto): JsonDataResponse = authenticatedPost(ApiRoutes.CANCEL, request)

    suspend fun updateContract(request: ContractUpdateRequestDto): JsonDataResponse =
        authenticatedPost(ApiRoutes.CONTRACT_UPDATE, request)

    suspend fun contractUpdateHistory(loginId: String, contractId: Long): JsonRowsResponse =
        authenticatedGet(ApiRoutes.CONTRACT_UPDATE_HISTORY) {
            parameter("login_id", loginId)
            parameter("contract_id", contractId)
        }

    suspend fun statement(
        loginId: String,
        offset: Int,
        limit: Int,
        dateFrom: Long?,
        dateTo: Long?,
        actionType: String?,
    ): ActivityResponse = authenticatedGet(ApiRoutes.STATEMENT) {
        parameter("login_id", loginId)
        parameter("offset", offset)
        parameter("limit", limit)
        dateFrom?.let { parameter("date_from", it) }
        dateTo?.let { parameter("date_to", it) }
        actionType?.takeIf(String::isNotBlank)?.let { parameter("action_type", it) }
    }

    suspend fun profitTable(
        loginId: String,
        offset: Int,
        limit: Int,
        dateFrom: String?,
        dateTo: String?,
        sort: String,
    ): ActivityResponse = authenticatedGet(ApiRoutes.PROFIT_TABLE) {
        parameter("login_id", loginId)
        parameter("offset", offset)
        parameter("limit", limit)
        dateFrom?.takeIf(String::isNotBlank)?.let { parameter("date_from", it) }
        dateTo?.takeIf(String::isNotBlank)?.let { parameter("date_to", it) }
        parameter("sort", sort)
    }

    fun close() = client.close()

    private suspend inline fun <reified T> authenticatedGet(
        path: String,
        crossinline configure: io.ktor.client.request.HttpRequestBuilder.() -> Unit = {},
    ): T {
        val token = tokenProvider.accessToken()
            ?: throw MissingAccessTokenException()
        return client.get(path) {
            bearerAuth(token)
            configure()
        }.body()
    }

    private suspend inline fun <reified T> publicGet(
        path: String,
        crossinline configure: io.ktor.client.request.HttpRequestBuilder.() -> Unit = {},
    ): T = client.get(path) { configure() }.body()

    private suspend inline fun <reified T, reified B> authenticatedPost(path: String, body: B): T {
        val token = tokenProvider.accessToken() ?: throw MissingAccessTokenException()
        return client.post(path) {
            bearerAuth(token)
            setBody(body)
        }.body()
    }

    companion object {
        private const val ANDROID_DERIV_RETURN_URL = "synex://deriv-connect"
        private val streamJson = Json { ignoreUnknownKeys = true }

        private fun defaultHttpClient(baseUrl: String): HttpClient = HttpClient(OkHttp) {
            expectSuccess = false
            HttpResponseValidator {
                validateResponse { response ->
                    if (!response.status.isSuccess()) {
                        val payload = runCatching { streamJson.parseToJsonElement(response.bodyAsText()).jsonObject }.getOrNull()
                        val code = payload?.get("code")?.jsonPrimitive?.contentOrNull.orEmpty()
                        val message = payload?.get("message")?.jsonPrimitive?.contentOrNull
                            ?: "The request was not accepted."
                        throw SynexApiException(code, message)
                    }
                }
            }
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
            install(WebSockets)
            defaultRequest {
                url(baseUrl.trimEnd('/') + "/")
                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
            }
        }
    }
}

class MissingAccessTokenException : IllegalStateException(
    "A user access token is required before calling the Synex API.",
)

class SynexApiException(val code: String, override val message: String) : IllegalStateException(message)
