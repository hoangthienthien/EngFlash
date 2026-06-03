package com.example.engflash.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val uid: String,
    val displayName: String,
    val email: String,
    val avatarUrl: String,
    val bio: String,
    val updatedAt: Long = 0L
)
