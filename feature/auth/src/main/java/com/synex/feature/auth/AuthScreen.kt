package com.synex.feature.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.synex.core.ui.SynexBrandMark
import com.synex.core.ui.SynexInk
import com.synex.core.ui.SynexMuted
import com.synex.core.ui.SynexPaper

@Composable
fun AuthScreen(state: AuthUiState, onAction: (AuthAction) -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    Column(
        Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFFAFAF7), SynexPaper)))
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AnimatedVisibility(visible, enter = fadeIn(tween(420)) + scaleIn(tween(520), initialScale = 0.9f)) {
            SynexBrandMark(Modifier.size(76.dp))
        }
        AnimatedVisibility(
            visible,
            enter = fadeIn(tween(480, 100)) + slideInVertically(tween(560, 100, FastOutSlowInEasing)) { it / 8 },
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(Modifier.height(30.dp))
                Text("Welcome to Synex", style = MaterialTheme.typography.displaySmall, textAlign = TextAlign.Center)
                Text(
                    "Sign in to continue to your trading workspace.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = SynexMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 10.dp),
                )
                state.errorMessage?.let {
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 18.dp),
                    )
                }
                if (!state.configured) {
                    Text(
                        "Sign-in is temporarily unavailable. Please try again later.",
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 18.dp),
                    )
                }
                Spacer(Modifier.height(30.dp))
                Button(
                    onClick = { onAction(AuthAction.SIGN_IN) },
                    enabled = state.configured && !state.isLoading,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(SynexInk, Color.White),
                ) {
                    if (state.isLoading) CircularProgressIndicator(Modifier.size(22.dp), Color.White, strokeWidth = 2.dp)
                    else Text("Sign in")
                }
                OutlinedButton(
                    onClick = { onAction(AuthAction.CREATE_ACCOUNT) },
                    enabled = state.configured && !state.isLoading,
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp).height(56.dp),
                    shape = CircleShape,
                ) {
                    Text("Create account", color = SynexInk)
                }
            }
        }
    }
}
