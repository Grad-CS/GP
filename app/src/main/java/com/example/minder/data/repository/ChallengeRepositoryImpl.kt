package com.example.minder.data.repository

import com.example.minder.data.local.dao.ChallengeDao
import com.example.minder.data.local.entities.ChallengeEntity
import com.example.minder.domain.model.Challenge
import com.example.minder.domain.model.GeneratedChallenge
import com.example.minder.domain.repository.ChallengeRepository

class ChallengeRepositoryImpl(
    private val challengeDao: ChallengeDao
) : ChallengeRepository {

    override suspend fun getChallengeById(id: Int): Challenge? =
        challengeDao.getChallengeById(id)?.toDomain()

    override suspend fun getChallengesByDifficulty(difficulty: String): List<Challenge> =
        challengeDao.getChallengesByDifficulty(difficulty).map { it.toDomain() }

    override suspend fun getChallengesByType(type: String): List<Challenge> =
        challengeDao.getChallengesByType(type).map { it.toDomain() }

    override suspend fun getAllChallenges(): List<Challenge> =
        challengeDao.getAllChallenges().map { it.toDomain() }

    // Lama - Persist generated math, logic and puzzle challenges.
    override suspend fun saveGeneratedChallenge(challenge: GeneratedChallenge): Int {
        val entity = ChallengeEntity(
            question = challenge.question,
            correctAnswer = challenge.correctAnswer,
            difficulty = challenge.difficulty,
            challengeType = challenge.challengeType.name,
            gameData = challenge.gameData
        )
        return challengeDao.insertChallenge(entity).toInt()
    }
}

private fun ChallengeEntity.toDomain(): Challenge = Challenge(
    id = challengeId,
    question = question,
    correctAnswer = correctAnswer,
    difficulty = difficulty,
    type = challengeType,
    gameData = gameData
)
