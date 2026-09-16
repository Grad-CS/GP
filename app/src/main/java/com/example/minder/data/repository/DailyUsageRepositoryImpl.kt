package com.example.minder.data.repository

import com.example.minder.data.local.dao.DailyUsageDao
import com.example.minder.data.local.entities.DailyUsageEntity
import com.example.minder.domain.model.UsageSummary
import com.example.minder.domain.repository.DailyUsageRepository

class DailyUsageRepositoryImpl(
    private val dailyUsageDao: DailyUsageDao
) : DailyUsageRepository {

    override suspend fun addDailyUsage(
        usage: UsageSummary
    ): Long {
        return dailyUsageDao.insertDailyUsage(
            usage.toEntity()
        )
    }

    override suspend fun getDailyUsageByAppId(
        appId: Int
    ): List<UsageSummary> {
        return dailyUsageDao
            .getDailyUsageByAppId(appId)
            .map { it.toDomain() }
    }

    override suspend fun getUsageForPeriod(
        appId: Int,
        startDate: Long,
        endDate: Long
    ): List<UsageSummary> {
        return dailyUsageDao
            .getUsageForPeriod(
                appId,
                startDate,
                endDate
            )
            .map { it.toDomain() }
    }

    override suspend fun getUsageForDate(
        appId: Int,
        date: Long
    ): UsageSummary? {
        return dailyUsageDao
            .getUsageForDate(appId, date)
            ?.toDomain()
    }

    override suspend fun updateDailyUsage(
        usage: UsageSummary
    ) {
        dailyUsageDao.updateDailyUsage(
            usage.toEntity()
        )
    }
}

private fun DailyUsageEntity.toDomain(): UsageSummary {
    return UsageSummary(
        id = dailyUsageId,
        appId = appId,
        date = date,
        totalUsage = totalUsage,
        savedTime = savedTime,
        challengeCount = challengeCount
    )
}

private fun UsageSummary.toEntity(): DailyUsageEntity {
    return DailyUsageEntity(
        dailyUsageId = id,
        appId = appId,
        date = date,
        totalUsage = totalUsage,
        savedTime = savedTime,
        challengeCount = challengeCount
    )
}