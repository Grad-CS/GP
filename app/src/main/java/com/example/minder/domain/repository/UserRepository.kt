package com.example.minder.domain.repository

import com.example.minder.domain.model.User

interface UserRepository {

    suspend fun addUser(user: User): Long

    suspend fun getUserById(userId: Int): User?

    suspend fun getUser(): User?

    suspend fun updateUser(user: User)

    suspend fun deleteUser(user: User)
}