package com.example.minder.domain.repository

import com.example.minder.domain.model.Challenge

interface ChallengeRepository {

    suspend fun getChallengeById(id: Int): Challenge?

    suspend fun getChallengesByDifficulty(
        difficulty: String
    ): List<Challenge>

    suspend fun getChallengesByType(
        type: String
    ): List<Challenge>

    suspend fun getAllChallenges(): List<Challenge>
}