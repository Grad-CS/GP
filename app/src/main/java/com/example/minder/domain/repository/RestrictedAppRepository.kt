package com.example.minder.domain.repository

import com.example.minder.domain.model.RestrictedApp

interface RestrictedAppRepository {

    suspend fun getAppsByUserId(userId: Int): List<RestrictedApp>

    suspend fun getAppById(appId: Int): RestrictedApp?

    suspend fun getAppByPackageName(
        userId: Int,
        packageName: String
    ): RestrictedApp?

    suspend fun addApp(app: RestrictedApp): Long

    suspend fun updateApp(app: RestrictedApp)

    suspend fun deleteApp(app: RestrictedApp)
}