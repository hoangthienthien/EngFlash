package com.example.engflash.domain.usecase.auth

import com.example.engflash.domain.repository.AuthRepository

class ChangePasswordUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(currentPassword: String, newPassword: String): Result<Unit> {
        return repository.changePassword(currentPassword, newPassword)
    }
}
