package com.example.minder.data.repository

import com.example.minder.data.local.dao.InterventionDao
import com.example.minder.data.local.entities.InterventionEntity
import com.example.minder.domain.model.Intervention
import com.example.minder.domain.repository.InterventionRepository

class InterventionRepositoryImpl(
    private val interventionDao: InterventionDao
) : InterventionRepository {

    override suspend fun addIntervention(
        intervention: Intervention
    ): Long {
        return interventionDao.insertIntervention(
            intervention.toEntity()
        )
    }

    override suspend fun getInterventionById(
        interventionId: Int
    ): Intervention? {
        return interventionDao
            .getInterventionById(interventionId)
            ?.toDomain()
    }

    override suspend fun getInterventionsByAppId(
        appId: Int
    ): List<Intervention> {
        return interventionDao
            .getInterventionsByAppId(appId)
            .map { it.toDomain() }
    }

    override suspend fun getInterventionsForPeriod(
        appId: Int,
        startTime: Long,
        endTime: Long
    ): List<Intervention> {
        return interventionDao
            .getInterventionsForPeriod(
                appId,
                startTime,
                endTime
            )
            .map { it.toDomain() }
    }

    override suspend fun completeIntervention(
        interventionId: Int,
        unlockTime: Long?,
        status: String
    ) {
        interventionDao.completeIntervention(
            interventionId,
            unlockTime,
            status
        )
    }
}

private fun InterventionEntity.toDomain(): Intervention {
    return Intervention(
        id = interventionId,
        appId = appId,
        challengeId = challengeId,
        triggerTime = triggerTime,
        unlockTime = unlockTime,
        status = status,
        savedTime = savedTime
    )
}

private fun Intervention.toEntity(): InterventionEntity {
    return InterventionEntity(
        interventionId = id,
        appId = appId,
        challengeId = challengeId,
        triggerTime = triggerTime,
        unlockTime = unlockTime,
        status = status,
        savedTime = savedTime
    )
}