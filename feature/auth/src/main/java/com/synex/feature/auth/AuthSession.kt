package com.synex.feature.auth

import android.app.Activity
import com.synex.core.network.AccessTokenProvider

interface AuthSession : AccessTokenProvider {
    val isConfigured: Boolean
    fun hasSession(): Boolean
    fun login(activity: Activity, action: AuthAction, onResult: (Result<Unit>) -> Unit)
    fun logout(activity: Activity, onResult: (Result<Unit>) -> Unit)
}

enum class AuthAction { SIGN_IN, CREATE_ACCOUNT }
