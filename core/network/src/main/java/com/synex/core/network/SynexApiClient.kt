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
import io.ktor.client.request.setBody
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
import kotlinx.serialization.json.Json

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

    suspend fun derivConnectUrl(): DerivConnectUrlResponse =
        authenticatedGet(ApiRoutes.DERIV_CONNECT_URL)

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
