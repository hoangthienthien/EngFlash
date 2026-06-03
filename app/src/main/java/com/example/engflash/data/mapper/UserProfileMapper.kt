package com.example.engflash.data.mapper

import com.example.engflash.data.local.entity.UserProfileEntity
import com.example.engflash.domain.model.UserProfile

fun UserProfileEntity.toDomain(): UserProfile {
    return UserProfile(
        uid = uid,
        displayName = displayName,
        email = email,
        avatarUrl = avatarUrl,
        bio = bio,
        updatedAt = updatedAt
    )
}

fun UserProfile.toEntity(): UserProfileEntity {
    return UserProfileEntity(
        uid = uid,
        displayName = displayName,
        email = email,
        avatarUrl = avatarUrl,
        bio = bio,
        updatedAt = updatedAt
    )
}
