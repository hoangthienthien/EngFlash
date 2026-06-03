package com.example.engflash.domain.repository

import com.example.engflash.domain.model.UserProfile

interface UserRepository {
    suspend fun getUserProfile(uid: String): UserProfile?
    suspend fun updateUserProfile(profile: UserProfile)
}
