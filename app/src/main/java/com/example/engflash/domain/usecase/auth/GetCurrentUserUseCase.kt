package com.example.engflash.domain.usecase.auth

import com.example.engflash.domain.model.User
import com.example.engflash.domain.repository.AuthRepository

/**
 * UseCase: Lấy thông tin user đang đăng nhập hiện tại.
 * Trả về null nếu chưa đăng nhập.
 */
class GetCurrentUserUseCase(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): User? {
        return authRepository.getCurrentUser()
    }
}
