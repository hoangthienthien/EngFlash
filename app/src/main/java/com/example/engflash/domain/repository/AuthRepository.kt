package com.example.engflash.domain.repository

import com.example.engflash.domain.model.User

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<User>
    suspend fun register(email: String, password: String, displayName: String): Result<User>
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
    suspend fun sendOtpEmail(email: String, otp: String): Result<Unit>
    fun logout()
    fun getCurrentUser(): User?
    fun isLoggedIn(): Boolean
}
