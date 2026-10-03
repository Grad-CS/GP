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

            // Find the selected app using its package name
            val restrictedApp =
                restrictedAppRepository.getAppByPackageName(
                    userId = userId,
                    packageName = session.packageName
                )

            // Save usage only for apps selected and enabled by the user
            if (restrictedApp != null && restrictedApp.isEnabled) {

                // Prevent saving the same Android usage session more than once
                val alreadyExists =
                    usageSessionRepository.sessionExists(
                        appId = restrictedApp.id,
                        startTime = session.startTime,
                        endTime = session.endTime
                    )

                if (!alreadyExists) {

                    val usageSession = UsageSession(
                        id = 0,
                        appId = restrictedApp.id,
                        startTime = session.startTime,
                        endTime = session.endTime,
                        duration = session.duration
                    )

                    usageSessionRepository.addSession(
                        usageSession
                    )
                }
            }
        }
    }
}