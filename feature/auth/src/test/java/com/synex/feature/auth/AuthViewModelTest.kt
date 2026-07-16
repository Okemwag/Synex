package com.synex.feature.auth

import android.app.Activity
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthViewModelTest {
    @Test
    fun exposesExistingAuthenticatedSession() {
        val state = AuthViewModel(FakeSession(isConfigured = true, authenticated = true)).state.value

        assertTrue(state.configured)
        assertTrue(state.authenticated)
        assertFalse(state.isLoading)
    }
}

private class FakeSession(
    override val isConfigured: Boolean,
    private val authenticated: Boolean,
) : AuthSession {
    override fun hasSession() = authenticated
    override suspend fun accessToken() = if (authenticated) "token" else null
    override fun login(activity: Activity, onResult: (Result<Unit>) -> Unit) = Unit
    override fun logout(activity: Activity, onResult: (Result<Unit>) -> Unit) = Unit
}
