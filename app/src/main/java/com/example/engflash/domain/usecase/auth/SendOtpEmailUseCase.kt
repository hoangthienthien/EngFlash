package com.example.engflash.domain.usecase.auth

import com.example.engflash.domain.repository.AuthRepository

class SendOtpEmailUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, otp: String): Result<Unit> {
        return authRepository.sendOtpEmail(email, otp)
    }
}
