package com.synex.feature.auth

import android.app.Activity
import android.content.Context
import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.authentication.storage.SecureCredentialsManager
import com.auth0.android.authentication.storage.SharedPreferencesStorage
import com.auth0.android.callback.Callback
import com.auth0.android.provider.WebAuthProvider
import com.auth0.android.result.Credentials

class Auth0Session(
    context: Context,
    clientId: String,
    domain: String,
    private val audience: String,
) : AuthSession {
    override val isConfigured = clientId.isNotBlank() && domain.isNotBlank() && audience.isNotBlank()
    private val account = if (isConfigured) Auth0.getInstance(clientId, domain) else null
    private val manager = account?.let {
        SecureCredentialsManager(context, it, SharedPreferencesStorage(context))
    }

    override fun hasSession(): Boolean = manager?.hasValidCredentials() == true

    override suspend fun accessToken(): String? = runCatching {
        manager?.awaitCredentials()?.accessToken?.takeIf(String::isNotBlank)
    }.getOrNull()

    override fun login(activity: Activity, onResult: (Result<Unit>) -> Unit) {
        val auth0 = account ?: return onResult(Result.failure(configurationError()))
        WebAuthProvider.login(auth0)
            .withScheme(AuthConstants.SCHEME)
            .withAudience(audience)
            .withScope(AuthConstants.SCOPES)
            .start(activity, authCallback(onResult))
    }

    override fun logout(activity: Activity, onResult: (Result<Unit>) -> Unit) {
        val auth0 = account ?: return onResult(Result.failure(configurationError()))
        WebAuthProvider.logout(auth0).withScheme(AuthConstants.SCHEME)
            .start(activity, object : Callback<Void?, AuthenticationException> {
                override fun onSuccess(result: Void?) {
                    manager?.clearCredentials()
                    onResult(Result.success(Unit))
                }

                override fun onFailure(error: AuthenticationException) {
                    onResult(Result.failure(error))
                }
            })
    }

    private fun authCallback(onResult: (Result<Unit>) -> Unit) =
        object : Callback<Credentials, AuthenticationException> {
            override fun onSuccess(result: Credentials) {
                onResult(runCatching { checkNotNull(manager).saveCredentials(result) })
            }

            override fun onFailure(error: AuthenticationException) {
                onResult(Result.failure(error))
            }
        }

    private fun configurationError() = IllegalStateException(
        "Set SYNEX_AUTH0_CLIENT_ID, SYNEX_AUTH0_DOMAIN, and SYNEX_AUTH0_AUDIENCE.",
    )
}
