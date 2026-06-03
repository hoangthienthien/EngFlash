package com.example.engflash.domain.model

data class UserProfile(
    val uid: String,
    val displayName: String,
    val email: String,
    val avatarUrl: String,
    val bio: String
)
