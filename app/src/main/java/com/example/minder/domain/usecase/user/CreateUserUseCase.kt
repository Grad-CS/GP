package com.example.minder.domain.usecase.user

import com.example.minder.domain.model.User
import com.example.minder.domain.repository.UserRepository

class CreateUserUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): Long {
        val user = User(
            userId = 0,
            createdAt = System.currentTimeMillis()
        )

        return userRepository.addUser(user)
    }
}