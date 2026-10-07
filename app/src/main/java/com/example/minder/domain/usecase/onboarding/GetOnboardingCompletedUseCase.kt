package com.example.minder.domain.usecase.onboarding

import com.example.minder.domain.repository.OnboardingStateRepository

class GetOnboardingCompletedUseCase(
    private val repository: OnboardingStateRepository
) {
    suspend operator fun invoke(): Boolean {
        return repository.isOnboardingCompleted()
    }
}