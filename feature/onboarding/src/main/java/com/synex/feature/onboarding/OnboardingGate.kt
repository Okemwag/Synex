package com.synex.feature.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.synex.core.data.SynexRepository
import com.synex.core.ui.ErrorState
import com.synex.core.ui.SynexBrandMark
import com.synex.core.ui.SynexGold
import com.synex.core.ui.SynexMuted
import com.synex.core.ui.SynexPaper
import com.synex.core.ui.SynexTeal

@Composable
fun OnboardingGate(
    repository: SynexRepository,
    viewModel: OnboardingViewModel = viewModel(factory = OnboardingViewModel.factory(repository)),
    content: @Composable () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    AnimatedContent(
        targetState = state.completed,
        transitionSpec = { fadeIn(tween(500)) togetherWith fadeOut(tween(280)) },
        label = "onboarding gate",
    ) { completed ->
        when {
            completed -> content()
            state.isLoading -> JourneyLoading()
            state.errorMessage != null && state.disclosureVersion.isBlank() -> {
                Box(Modifier.fillMaxSize().background(SynexPaper), contentAlignment = Alignment.Center) {
                    ErrorState(state.errorMessage.orEmpty(), viewModel::refresh)
                }
            }
            else -> OnboardingFlow(state, viewModel)
        }
    }
}

@Composable
private fun OnboardingFlow(state: OnboardingUiState, viewModel: OnboardingViewModel) {
    BackHandler(enabled = state.step != OnboardingStep.LEGAL, onBack = viewModel::back)
    AnimatedContent(
        targetState = state.step,
        transitionSpec = {
            val forward = targetState.ordinal > initialState.ordinal
            val enter = slideInHorizontally(tween(420, easing = FastOutSlowInEasing)) {
                if (forward) it / 3 else -it / 3
            } + fadeIn(tween(350))
            val exit = slideOutHorizontally(tween(320)) { if (forward) -it / 4 else it / 4 } + fadeOut(tween(220))
            enter togetherWith exit
        },
        label = "legal onboarding",
    ) { step ->
        when (step) {
            OnboardingStep.LEGAL -> LegalIntroScreen(viewModel::next)
            OnboardingStep.PRIVACY -> PrivacyScreen(viewModel::back, viewModel::next)
            OnboardingStep.RISK -> RiskScreen(
                checked = state.riskChecked,
                isSaving = state.isSaving,
                errorMessage = state.errorMessage,
                onCheckedChange = viewModel::setRiskChecked,
                onBack = viewModel::back,
                onAccept = viewModel::acceptRisk,
            )
        }
    }
}

@Composable
private fun JourneyLoading() {
    val pulse by rememberInfiniteTransition(label = "brand pulse").animateFloat(
        initialValue = 0.94f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "logo scale",
    )
    Box(
        Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(SynexTeal, Color(0xFF075761)))),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            SynexBrandMark(Modifier.size(92.dp).scale(pulse))
            Spacer(Modifier.height(20.dp))
            Text("Preparing your account", style = MaterialTheme.typography.titleLarge, color = Color.White)
            Text("This will only take a moment", color = Color.White.copy(alpha = 0.62f))
        }
    }
}
