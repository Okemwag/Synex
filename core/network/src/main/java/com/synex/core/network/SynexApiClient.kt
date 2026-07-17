package com.synex.core.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.request.accept
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.parameter
import io.ktor.client.request.prepareGet
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.ContentType
import io.ktor.http.contentType
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
import io.ktor.utils.io.readUTF8Line
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

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
            expectSuccess = true
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
