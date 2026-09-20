package com.example.minder.data.repository

import com.example.minder.data.local.dao.RestrictedAppDao
import com.example.minder.data.local.entities.RestrictedAppEntity
import com.example.minder.domain.model.RestrictedApp
import com.example.minder.domain.repository.RestrictedAppRepository

class RestrictedAppRepositoryImpl(
    private val restrictedAppDao: RestrictedAppDao
) : RestrictedAppRepository {

    override suspend fun getAppsByUserId(
        userId: Int
    ): List<RestrictedApp> {
        return restrictedAppDao
            .getAppsByUserId(userId)
            .map { it.toDomain() }
    }

    override suspend fun getAppById(
        appId: Int
    ): RestrictedApp? {
        return restrictedAppDao
            .getAppById(appId)
            ?.toDomain()
    }

    override suspend fun getAppByPackageName(
        userId: Int,
        packageName: String
    ): RestrictedApp? {
        return restrictedAppDao
            .getAppByPackageName(userId, packageName)
            ?.toDomain()
    }

    override suspend fun addApp(
        app: RestrictedApp
    ): Long {
        return restrictedAppDao.insertApp(
            app.toEntity()
        )
    }

    override suspend fun updateApp(
        app: RestrictedApp
    ) {
        restrictedAppDao.updateApp(
            app.toEntity()
        )
    }

    override suspend fun deleteApp(
        app: RestrictedApp
    ) {
        restrictedAppDao.deleteApp(
            app.toEntity()
        )
    }
}

private fun RestrictedAppEntity.toDomain(): RestrictedApp {
    return RestrictedApp(
        id = appId,
        userId = userId,
        appName = appName,
        packageName = packageName,
        dailyLimit = dailyLimit,
        isEnabled = isEnabled,
        addedAt = addedAt
    )
}

private fun RestrictedApp.toEntity(): RestrictedAppEntity {
    return RestrictedAppEntity(
        appId = id,
        userId = userId,
        appName = appName,
        packageName = packageName,
        dailyLimit = dailyLimit,
        isEnabled = isEnabled,
        addedAt = addedAt
    )
}