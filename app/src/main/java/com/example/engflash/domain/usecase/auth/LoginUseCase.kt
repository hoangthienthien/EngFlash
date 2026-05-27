package com.example.engflash.domain.usecase.auth

import com.example.engflash.domain.model.User
import com.example.engflash.domain.repository.AuthRepository

/**
 * UseCase: Đăng nhập bằng email và mật khẩu.
 * Thực hiện kiểm tra đầu vào cơ bản trước khi gọi xuống Repository.
 */
class LoginUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<User> {
        if (email.isBlank()) {
            return Result.failure(Exception("Email không được để trống"))
        }
        if (password.isBlank()) {
            return Result.failure(Exception("Mật khẩu không được để trống"))
        }
        return authRepository.login(email, password)
    }
}
