package com.example.minder.services.apps

import android.content.Context
import android.content.Intent
import com.example.minder.domain.model.InstalledApp

class InstalledAppsService(
    private val context: Context
) {

    fun getInstalledApps(): List<InstalledApp> {

        val packageManager = context.packageManager

        val launcherIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        return packageManager
            .queryIntentActivities(launcherIntent, 0)
            .filter { resolveInfo ->
                resolveInfo.activityInfo.packageName != context.packageName
            }
            .map { resolveInfo ->
                InstalledApp(
                    packageName = resolveInfo.activityInfo.packageName,
                    appName = resolveInfo
                        .loadLabel(packageManager)
                        .toString()
                )
            }
            .distinctBy { it.packageName }
            .sortedBy { it.appName.lowercase() }
    }
}