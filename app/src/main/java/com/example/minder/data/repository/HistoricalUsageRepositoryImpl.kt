package com.example.minder.data.repository

import com.example.minder.data.datasource.HistoricalUsageDataSource
import com.example.minder.domain.model.HistoricalDailyUsage
import com.example.minder.domain.repository.HistoricalUsageRepository

class HistoricalUsageRepositoryImpl(
    private val historicalUsageDataSource: HistoricalUsageDataSource
) : HistoricalUsageRepository {

    override suspend fun getDailyUsage(
        packageName: String,
        startDate: Long,
        endDateExclusive: Long
    ): List<HistoricalDailyUsage> {
        //Ragahd-: Delegate historical usage retrieval to the data source.
        return historicalUsageDataSource.getDailyUsage(
            packageName = packageName,
            startDate = startDate,
            endDateExclusive = endDateExclusive
        )
    }
}