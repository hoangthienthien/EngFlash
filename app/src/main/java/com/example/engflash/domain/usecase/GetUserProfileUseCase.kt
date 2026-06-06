package com.example.engflash.domain.usecase

import com.example.engflash.domain.model.UserProfile
import com.example.engflash.domain.repository.UserRepository

class GetUserProfileUseCase(
    private val repository: UserRepository
) {
    suspend operator fun invoke(uid: String): UserProfile? {
        return repository.getUserProfile(uid)
    }
}
