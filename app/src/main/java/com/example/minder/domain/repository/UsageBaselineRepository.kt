package com.example.minder.domain.repository

import com.example.minder.domain.model.UsageBaseline

interface UsageBaselineRepository {

    //Ragahd-: Save baseline records for an app.
    suspend fun saveBaselines(records: List<UsageBaseline>): Boolean

    //Ragahd-: Retrieve the baseline for a specific app and weekday.
    suspend fun getBaselineForDay(
        appId: Int,
        dayOfWeek: Int
    ): UsageBaseline?

    //Ragahd-: Retrieve all baseline records for an app.
    suspend fun getBaselineByAppId(
        appId: Int
    ): List<UsageBaseline>
}