package com.example.engflash.domain.usecase.auth

import com.example.engflash.domain.repository.AuthRepository

/**
 * UseCase: Kiểm tra xem người dùng đã đăng nhập chưa.
 */
class IsLoggedInUseCase(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): Boolean {
        return authRepository.isLoggedIn()
    }
}
