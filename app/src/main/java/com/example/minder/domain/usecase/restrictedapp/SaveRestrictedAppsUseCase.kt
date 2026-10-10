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
            //Ragahd-: Find an existing app using its package name and user ID
            val existingApp = restrictedAppRepository.getAppByPackageName(
                userId = userId,
                packageName = app.packageName
            )

            if (existingApp == null) {
                //Ragahd-: Create and save a new restricted app
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
            } else {
                //Ragahd-: Update the existing app with the newly selected daily limit
                restrictedAppRepository.updateApp(
                    existingApp.copy(
                        appName = app.appName,
                        dailyLimit = app.dailyLimit,
                        isEnabled = true
                    )
                )
            }
        }
    }

}

data class SelectedApp(
    val appName: String,
    val packageName: String,
    val dailyLimit: Int
)