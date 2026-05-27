package com.example.engflash.domain.usecase.auth

import com.example.engflash.domain.model.User
import com.example.engflash.domain.repository.AuthRepository

/**
 * UseCase: Đăng ký tài khoản mới.
 * Kiểm tra mật khẩu hợp lệ và khớp nhau trước khi gọi Repository.
 */
class RegisterUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        email: String,
        password: String,
        confirmPassword: String,
        displayName: String
    ): Result<User> {
        if (email.isBlank()) {
            return Result.failure(Exception("Email không được để trống"))
        }
        if (displayName.isBlank()) {
            return Result.failure(Exception("Tên hiển thị không được để trống"))
        }
        if (password.length < 6) {
            return Result.failure(Exception("Mật khẩu phải có ít nhất 6 ký tự"))
        }
        if (password != confirmPassword) {
            return Result.failure(Exception("Mật khẩu xác nhận không khớp"))
        }
        return authRepository.register(email, password, displayName)
    }
}
