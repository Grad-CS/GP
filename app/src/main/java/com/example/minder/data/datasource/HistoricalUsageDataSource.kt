package com.example.minder.data.datasource

import com.example.minder.domain.model.HistoricalDailyUsage

interface HistoricalUsageDataSource {

    suspend fun getDailyUsage(
        packageName: String,
        startDate: Long,
        endDateExclusive: Long
    ): List<HistoricalDailyUsage>
}