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

    fun login(activity: Activity) = runAuth { session.login(activity, it) }

    fun logout(activity: Activity) = runAuth { session.logout(activity, it) }

    private fun runAuth(action: ((Result<Unit>) -> Unit) -> Unit) {
        mutableState.value = mutableState.value.copy(isLoading = true, errorMessage = null)
        action { result ->
            mutableState.value = mutableState.value.copy(
                authenticated = session.hasSession(),
                isLoading = false,
                errorMessage = result.exceptionOrNull()?.message,
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
