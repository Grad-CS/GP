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

    suspend fun getUsageForDate(
        appId: Int,
        date: Long
    ): UsageSummary?

    suspend fun updateDailyUsage(
        usage: UsageSummary
    )
}