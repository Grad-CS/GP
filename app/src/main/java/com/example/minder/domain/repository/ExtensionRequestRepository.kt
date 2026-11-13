package com.example.minder.domain.repository

import com.example.minder.domain.model.ExtensionRequest

interface ExtensionRequestRepository {

    suspend fun getRequestForAppOnDate(
        appId: Int,
        dayStart: Long,
        nextDayStart: Long
    ): ExtensionRequest?

    suspend fun addDailyExtensionIfAbsent(
        request: ExtensionRequest,
        dayStart: Long,
        nextDayStart: Long
    ): Long?
}