package com.example.minder.domain.repository

import com.example.minder.domain.model.HistoricalDailyUsage

interface HistoricalUsageRepository {

    suspend fun getDailyUsage(
        packageName: String,
        startDate: Long,
        endDateExclusive: Long
    ): List<HistoricalDailyUsage>
}