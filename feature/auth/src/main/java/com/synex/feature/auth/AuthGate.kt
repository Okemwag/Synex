package com.synex.feature.auth

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
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
    val activity = LocalContext.current.findActivity()
    if (state.authenticated) {
        authenticatedContent { viewModel.logout(activity) }
    } else {
        AuthScreen(state, onLogin = { activity?.let(viewModel::login) })
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
