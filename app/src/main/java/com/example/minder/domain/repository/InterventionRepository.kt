package com.example.minder.domain.repository

import com.example.minder.domain.model.Intervention

interface InterventionRepository {

    suspend fun addIntervention(intervention: Intervention): Long

    suspend fun getInterventionById(
        interventionId: Int
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
}