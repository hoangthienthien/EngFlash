package com.example.engflash.domain.usecase.auth

import com.example.engflash.domain.repository.AuthRepository

/**
 * UseCase: Gửi email đặt lại mật khẩu.
 */
class SendPasswordResetEmailUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String): Result<Unit> {
        if (email.isBlank()) {
            return Result.failure(Exception("Email không được để trống"))
        }
        return authRepository.sendPasswordResetEmail(email)
    }
}
