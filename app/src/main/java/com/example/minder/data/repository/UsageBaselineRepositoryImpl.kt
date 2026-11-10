package com.example.minder.data.repository

import com.example.minder.data.local.dao.UsageBaselineDao
import com.example.minder.data.local.entities.UsageBaselineEntity
import com.example.minder.domain.model.UsageBaseline
import com.example.minder.domain.repository.UsageBaselineRepository

class UsageBaselineRepositoryImpl(
    private val usageBaselineDao: UsageBaselineDao
) : UsageBaselineRepository {

    //Ragahd-: Save baseline records only when no previous baseline exists.
    override suspend fun saveBaselines(records: List<UsageBaseline>): Boolean {
        return usageBaselineDao.saveBaselinesIfAbsent(
            records = records.map { it.toEntity() }
        )
    }

    override suspend fun getBaselineForDay(
        appId: Int,
        dayOfWeek: Int
    ): UsageBaseline? {
        return usageBaselineDao.getBaselineForDay(
            appId = appId,
            dayOfWeek = dayOfWeek
        )?.toDomain()
    }

    override suspend fun getBaselineByAppId(
        appId: Int
    ): List<UsageBaseline> {
        return usageBaselineDao.getBaselineByAppId(appId)
            .map { it.toDomain() }
    }

    //Ragahd-: Convert the database entity into a domain model.
    private fun UsageBaselineEntity.toDomain() = UsageBaseline(
        baselineId = baselineId,
        appId = appId,
        dayOfWeek = dayOfWeek,
        averageUsageSeconds = averageUsageSeconds,
        sampleCount = sampleCount,
        periodStart = periodStart,
        periodEnd = periodEnd,
        capturedAt = capturedAt
    )

    //Ragahd-: Convert the domain model into a database entity.
    private fun UsageBaseline.toEntity() = UsageBaselineEntity(
        baselineId = baselineId,
        appId = appId,
        dayOfWeek = dayOfWeek,
        averageUsageSeconds = averageUsageSeconds,
        sampleCount = sampleCount,
        periodStart = periodStart,
        periodEnd = periodEnd,
        capturedAt = capturedAt
    )
}