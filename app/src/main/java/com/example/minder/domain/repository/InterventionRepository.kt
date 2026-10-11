package com.example.minder.domain.repository

import com.example.minder.domain.model.Intervention

interface InterventionRepository {

    suspend fun addIntervention(intervention: Intervention): Long

    suspend fun getInterventionById(
        interventionId: Int
    ): Intervention?

    // Lama - Gets the pending challenge for a restricted app
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
    // Lama - Gets the user's shared pending intervention
    suspend fun getPendingInterventionForUser(
        userId: Int
    ): Intervention?
}