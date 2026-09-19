package com.example.minder.domain.usecase.usage

import com.example.minder.domain.model.UsageSession
import com.example.minder.domain.repository.RestrictedAppRepository
import com.example.minder.domain.repository.UsageSessionRepository
import com.example.minder.services.usage.AppUsageSession

class SaveUsageSessionsUseCase(
    private val restrictedAppRepository: RestrictedAppRepository,
    private val usageSessionRepository: UsageSessionRepository
) {

    suspend operator fun invoke(
        userId: Int,
        sessions: List<AppUsageSession>
    ) {

        sessions.forEach { session ->

            // Lama - Finds the selected app using its package name
            val restrictedApp =
                restrictedAppRepository.getAppByPackageName(
                    userId = userId,
                    packageName = session.packageName
                )

            // Lama - Saves usage only for apps selected and enabled by the user
            if (restrictedApp != null && restrictedApp.isEnabled) {

                val usageSession = UsageSession(
                    id = 0,
                    appId = restrictedApp.id,
                    startTime = session.startTime,
                    endTime = session.endTime,
                    duration = session.duration
                )

                // Lama - Saves the usage session through the repository
                usageSessionRepository.addSession(
                    usageSession
                )
            }
        }
    }
}