package com.example.minder.domain.usecase.restrictedapp

import com.example.minder.domain.model.RestrictedApp
import com.example.minder.domain.repository.RestrictedAppRepository

class SaveRestrictedAppsUseCase(
    private val restrictedAppRepository: RestrictedAppRepository
) {

    suspend operator fun invoke(
        userId: Int,
        selectedApps: List<SelectedApp>
    ) {
        selectedApps.forEach { app ->

            val existingApp =
                restrictedAppRepository.getAppByPackageName(
                    userId = userId,
                    packageName = app.packageName
                )

            if (existingApp == null) {

                val restrictedApp = RestrictedApp(
                    id = 0,
                    userId = userId,
                    appName = app.appName,
                    packageName = app.packageName,
                    dailyLimit = app.dailyLimit,
                    isEnabled = true,
                    addedAt = System.currentTimeMillis()
                )

                restrictedAppRepository.addApp(restrictedApp)
            }
        }
    }
}

data class SelectedApp(
    val appName: String,
    val packageName: String,
    val dailyLimit: Int
)