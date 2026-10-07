package com.example.minder.domain.usecase.user

import com.example.minder.domain.model.User
import com.example.minder.domain.repository.UserRepository

class InitializeLocalUserUseCase(
    private val userRepository: UserRepository,
    private val createUserUseCase: CreateUserUseCase
) {

    suspend operator fun invoke(): User {
        val existingUser = userRepository.getUser()

        if (existingUser != null) {
            return existingUser
        }

        val userId = createUserUseCase()

        return userRepository.getUserById(
            userId.toInt()
        )!!
    }
}