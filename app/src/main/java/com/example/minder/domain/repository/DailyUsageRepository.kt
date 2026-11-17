package com.example.minder.domain.repository

import com.example.minder.domain.model.UsageSummary

interface DailyUsageRepository {

    suspend fun addDailyUsage(
        usage: UsageSummary
    ): Long

    suspend fun getDailyUsageByAppId(
        appId: Int
    ): List<UsageSummary>

    suspend fun getUsageForPeriod(
        appId: Int,
        startDate: Long,
        endDate: Long
    ): List<UsageSummary>

    //Ragahd-: Retrieve usage within a local day range
    suspend fun getUsageForDate(
        appId: Int,
        dayStart: Long,
        nextDayStart: Long
    ): UsageSummary?

    suspend fun updateDailyUsage(
        usage: UsageSummary
    )
}