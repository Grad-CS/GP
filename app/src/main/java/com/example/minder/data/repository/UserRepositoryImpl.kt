package com.example.minder.data.repository

import com.example.minder.data.local.dao.UserDao
import com.example.minder.data.local.entities.UserEntity
import com.example.minder.domain.model.User
import com.example.minder.domain.repository.UserRepository

class UserRepositoryImpl(
    private val userDao: UserDao
) : UserRepository {

    override suspend fun addUser(user: User): Long =
        userDao.insertUser(user.toEntity())

    override suspend fun getUserById(userId: Int): User? =
        userDao.getUserById(userId)?.toDomain()

    override suspend fun getUser(): User? =
        userDao.getUser()?.toDomain()

    override suspend fun updateUser(user: User) {
        userDao.updateUser(user.toEntity())
    }

    override suspend fun deleteUser(user: User) {
        userDao.deleteUser(user.toEntity())
    }
}

private fun UserEntity.toDomain(): User =
    User(
        userId = userId,
        createdAt = createdAt
    )

private fun User.toEntity(): UserEntity =
    UserEntity(
        userId = userId,
        createdAt = createdAt
    )