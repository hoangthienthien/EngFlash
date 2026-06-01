package com.example.engflash.data.repository

import com.example.engflash.data.local.dao.UserProfileDao
import com.example.engflash.data.mapper.toDomain
import com.example.engflash.data.mapper.toEntity
import com.example.engflash.domain.model.UserProfile
import com.example.engflash.domain.repository.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepositoryImpl(
    private val userProfileDao: UserProfileDao,
    private val firestore: FirebaseFirestore
) : UserRepository {

    override suspend fun getUserProfile(uid: String): UserProfile? {
        // Read from Room (offline-first)
        val localEntity = userProfileDao.getByUid(uid)
        if (localEntity != null) {
            return localEntity.toDomain()
        }

        // Pull from Firestore if not found in Room
        return try {
            val doc = firestore.collection("users").document(uid).get().await()
            if (doc.exists()) {
                val displayName = doc.getString("displayName") ?: ""
                val email = doc.getString("email") ?: ""
                val avatarUrl = doc.getString("avatarUrl") ?: ""
                val bio = doc.getString("bio") ?: ""
                
                val profile = UserProfile(
                    uid = uid,
                    displayName = displayName,
                    email = email,
                    avatarUrl = avatarUrl,
                    bio = bio
                )
                // Cache to Room
                userProfileDao.upsert(profile.toEntity())
                profile
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun updateUserProfile(profile: UserProfile) {
        // Save to Room first
        userProfileDao.upsert(profile.toEntity())

        // Push to Firestore
        val userMap = hashMapOf(
            "uid" to profile.uid,
            "displayName" to profile.displayName,
            "email" to profile.email,
            "avatarUrl" to profile.avatarUrl,
            "bio" to profile.bio
        )
        try {
            firestore.collection("users").document(profile.uid).set(userMap).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
