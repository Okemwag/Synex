package com.synex.feature.auth

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
    AnimatedContent(
        targetState = state.authenticated,
        transitionSpec = { fadeIn(tween(450)) togetherWith fadeOut(tween(260)) },
        label = "account access",
    ) { authenticated ->
        if (authenticated) {
            authenticatedContent { activity?.let(viewModel::logout) }
        } else {
            AuthScreen(state) { action -> activity?.let { viewModel.login(it, action) } }
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
