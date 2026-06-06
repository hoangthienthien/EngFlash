package com.example.engflash.domain.usecase

import com.example.engflash.domain.model.UserProfile
import com.example.engflash.domain.repository.UserRepository

class UpdateUserProfileUseCase(
    private val repository: UserRepository
) {
    suspend operator fun invoke(profile: UserProfile) {
        repository.updateUserProfile(profile)
    }
}
