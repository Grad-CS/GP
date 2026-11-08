
package com.example.minder.domain.repository

import com.example.minder.domain.model.Challenge
import com.example.minder.domain.model.GeneratedChallenge

interface ChallengeRepository {

    suspend fun getChallengeById(id: Int): Challenge?

    suspend fun getChallengesByDifficulty(
        difficulty: String
    ): List<Challenge>

    suspend fun getChallengesByType(
        type: String
    ): List<Challenge>

    suspend fun getAllChallenges(): List<Challenge>

    //Ragahd-: Saves a generated challenge and returns its database ID.
    suspend fun saveGeneratedChallenge(
        challenge: GeneratedChallenge
    ): Int
}
