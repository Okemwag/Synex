package com.synex.core.network

fun interface AccessTokenProvider {
    suspend fun accessToken(): String?
}
