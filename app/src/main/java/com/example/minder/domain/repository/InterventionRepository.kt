
package com.example.minder.domain.repository

import com.example.minder.domain.model.Intervention

interface InterventionRepository {

    suspend fun addIntervention(intervention: Intervention): Long

    suspend fun getInterventionById(
        interventionId: Int
    ): Intervention?

    suspend fun getPendingIntervention(
        appId: Int
    ): Intervention?

    suspend fun getInterventionsByAppId(
        appId: Int
    ): List<Intervention>

    suspend fun getInterventionsForPeriod(
        appId: Int,
        startTime: Long,
        endTime: Long
    ): List<Intervention>

    suspend fun completeIntervention(
        interventionId: Int,
        unlockTime: Long?,
        status: String
    )

    // Lama - Retrieve a shared pending challenge.
    suspend fun getPendingInterventionForUser(
        userId: Int
    ): Intervention?

    // Lama - Retrieve a lock that has not expired.
    suspend fun getActiveLockForUser(
        userId: Int,
        currentTimeMillis: Long
    ): Intervention?

    // Lama - Retrieve the latest lock record.
    suspend fun getLatestLockForUser(
        userId: Int
    ): Intervention?
}
