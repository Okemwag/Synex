package com.synex.feature.auth

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun AuthGate(
    session: AuthSession,
    authenticatedContent: @Composable (() -> Unit) -> Unit,
    viewModel: AuthViewModel = viewModel(factory = AuthViewModel.factory(session)),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val activity = LocalContext.current as Activity
    if (state.authenticated) {
        authenticatedContent { viewModel.logout(activity) }
    } else {
        AuthScreen(state, onLogin = { viewModel.login(activity) })
    }
}
