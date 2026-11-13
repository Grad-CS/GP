package com.example.minder.data.repository

import com.example.minder.data.local.dao.ExtensionRequestDao
import com.example.minder.data.local.entities.ExtensionRequestEntity
import com.example.minder.domain.model.ExtensionRequest
import com.example.minder.domain.repository.ExtensionRequestRepository

class ExtensionRequestRepositoryImpl(
    private val extensionRequestDao: ExtensionRequestDao
) : ExtensionRequestRepository {

    override suspend fun getRequestForAppOnDate(
        appId: Int,
        dayStart: Long,
        nextDayStart: Long
    ): ExtensionRequest? {
        return extensionRequestDao.getRequestForAppOnDate(
            appId = appId,
            dayStart = dayStart,
            nextDayStart = nextDayStart
        )?.toDomain()
    }

    override suspend fun addDailyExtensionIfAbsent(
        request: ExtensionRequest,
        dayStart: Long,
        nextDayStart: Long
    ): Long? {
        require(request.requestedMinutes in listOf(10, 15, 20))
        require(request.grantedMinutes in listOf(10, 15, 20))

        return extensionRequestDao.insertDailyExtensionIfAbsent(
            request = request.toEntity(),
            dayStart = dayStart,
            nextDayStart = nextDayStart
        )
    }
}

private fun ExtensionRequestEntity.toDomain() = ExtensionRequest(
    id = extensionId,
    appId = appId,
    requestedMinutes = requestedMinutes,
    grantedMinutes = grantedMinutes,
    requestTime = requestTime,
    status = status
)

private fun ExtensionRequest.toEntity() = ExtensionRequestEntity(
    extensionId = id,
    appId = appId,
    requestedMinutes = requestedMinutes,
    grantedMinutes = grantedMinutes,
    requestTime = requestTime,
    status = status
)