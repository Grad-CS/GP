package com.example.minder.data.repository

import com.example.minder.data.local.dao.ChallengeDao
import com.example.minder.data.local.entities.ChallengeEntity
import com.example.minder.domain.model.Challenge
import com.example.minder.domain.repository.ChallengeRepository

class ChallengeRepositoryImpl(
    private val challengeDao: ChallengeDao
) : ChallengeRepository {

    override suspend fun getChallengeById(id: Int): Challenge? {
        return challengeDao.getChallengeById(id)?.toDomain()
    }

    override suspend fun getChallengesByDifficulty(
        difficulty: String
    ): List<Challenge> {
        return challengeDao
            .getChallengesByDifficulty(difficulty)
            .map { it.toDomain() }
    }

    override suspend fun getChallengesByType(
        type: String
    ): List<Challenge> {
        return challengeDao
            .getChallengesByType(type)
            .map { it.toDomain() }
    }

    override suspend fun getAllChallenges(): List<Challenge> {
        return challengeDao
            .getAllChallenges()
            .map { it.toDomain() }
    }
}

private fun ChallengeEntity.toDomain(): Challenge {
    return Challenge(
        id = challengeId,
        question = question,
        correctAnswer = correctAnswer,
        difficulty = difficulty,
        type = challengeType
    )
}