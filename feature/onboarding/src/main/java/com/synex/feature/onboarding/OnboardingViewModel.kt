package com.synex.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.synex.core.data.SynexRepository
import com.synex.core.ui.customerMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OnboardingViewModel(private val repository: SynexRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(OnboardingUiState())
    val state: StateFlow<OnboardingUiState> = mutableState.asStateFlow()

    init { refresh() }

    fun refresh() = viewModelScope.launch {
        mutableState.update { it.copy(isLoading = true, errorMessage = null) }
        runCatching { repository.onboardingStatus() }.fold(
            onSuccess = { status ->
                mutableState.update {
                    it.copy(
                        isLoading = false,
                        completed = status.riskAcknowledged,
                        disclosureVersion = status.disclosureVersion,
                    )
                }
            },
            onFailure = { error ->
                mutableState.update {
                    it.copy(isLoading = false, errorMessage = error.customerMessage("open your account"))
                }
            },
        )
    }

    fun next() = mutableState.update { state ->
        state.copy(step = when (state.step) {
            OnboardingStep.LEGAL -> OnboardingStep.PRIVACY
            OnboardingStep.PRIVACY -> OnboardingStep.RISK
            OnboardingStep.RISK -> OnboardingStep.RISK
        })
    }

    fun back() = mutableState.update { state ->
        state.copy(step = when (state.step) {
            OnboardingStep.LEGAL -> OnboardingStep.LEGAL
            OnboardingStep.PRIVACY -> OnboardingStep.LEGAL
            OnboardingStep.RISK -> OnboardingStep.PRIVACY
        })
    }

    fun setRiskChecked(checked: Boolean) = mutableState.update { it.copy(riskChecked = checked) }

    fun acceptRisk() = viewModelScope.launch {
        val version = state.value.disclosureVersion
        if (!state.value.riskChecked || version.isBlank()) return@launch
        mutableState.update { it.copy(isSaving = true, errorMessage = null) }
        runCatching { repository.acknowledgeRisk(version) }.fold(
            onSuccess = { mutableState.update { it.copy(isSaving = false, completed = true) } },
            onFailure = { error ->
                mutableState.update {
                    it.copy(isSaving = false, errorMessage = error.customerMessage("save your choice"))
                }
            },
        )
    }

    companion object {
        fun factory(repository: SynexRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                OnboardingViewModel(repository) as T
        }
    }
}
