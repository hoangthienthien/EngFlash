package com.example.engflash.domain.usecase.auth

import com.example.engflash.domain.repository.AuthRepository

/**
 * UseCase: Đăng xuất khỏi ứng dụng.
 */
class LogoutUseCase(
    private val authRepository: AuthRepository
) {
    operator fun invoke() {
        authRepository.logout()
    }
}
