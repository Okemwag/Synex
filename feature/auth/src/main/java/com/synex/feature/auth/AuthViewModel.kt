package com.synex.feature.auth

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthViewModel(private val session: AuthSession) : ViewModel() {
    private val mutableState = MutableStateFlow(
        AuthUiState(session.isConfigured, session.hasSession()),
    )
    val state: StateFlow<AuthUiState> = mutableState.asStateFlow()

    fun login(activity: Activity, action: AuthAction) =
        runAuth(if (action == AuthAction.SIGN_IN) "sign you in" else "create your account") {
            session.login(activity, action, it)
        }

    fun logout(activity: Activity) = runAuth("sign you out") { session.logout(activity, it) }

    private fun runAuth(failureAction: String, action: ((Result<Unit>) -> Unit) -> Unit) {
        mutableState.value = mutableState.value.copy(isLoading = true, errorMessage = null)
        action { result ->
            mutableState.value = mutableState.value.copy(
                authenticated = session.hasSession(),
                isLoading = false,
                errorMessage = result.exceptionOrNull()?.let { "We couldn't $failureAction. Please try again." },
            )
        }
    }

    companion object {
        fun factory(session: AuthSession) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                AuthViewModel(session) as T
        }
    }
}
