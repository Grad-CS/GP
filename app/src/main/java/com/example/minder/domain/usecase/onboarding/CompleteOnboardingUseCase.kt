package com.example.minder.domain.usecase.onboarding

import com.example.minder.domain.repository.OnboardingStateRepository

class CompleteOnboardingUseCase(
    private val repository: OnboardingStateRepository
) {
    suspend operator fun invoke() {
        repository.setOnboardingCompleted(true)
    }
}