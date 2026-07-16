package com.synex.feature.onboarding

enum class OnboardingStep { LEGAL, PRIVACY, RISK }

data class OnboardingUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val completed: Boolean = false,
    val step: OnboardingStep = OnboardingStep.LEGAL,
    val disclosureVersion: String = "",
    val riskChecked: Boolean = false,
    val errorMessage: String? = null,
)
