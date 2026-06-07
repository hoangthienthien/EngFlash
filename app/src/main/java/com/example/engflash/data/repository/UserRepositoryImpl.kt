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
        android.util.Log.d("UserRepositoryImpl", "getUserProfile called for uid: $uid")
        val localEntity = userProfileDao.getByUid(uid)
        android.util.Log.d("UserRepositoryImpl", "localEntity fetched: $localEntity")

        // Thử fetch remote (bất đồng bộ, bỏ qua nếu offline)
        val remoteProfile: UserProfile? = try {
            val doc = firestore.collection("users").document(uid).get().await()
            android.util.Log.d("UserRepositoryImpl", "Firestore doc exists: ${doc.exists()}")
            if (doc.exists()) {
                val profile = UserProfile(
                    uid         = uid,
                    displayName = doc.getString("displayName") ?: "",
                    email       = doc.getString("email") ?: "",
                    avatarUrl   = doc.getString("avatarUrl") ?: "",
                    bio         = doc.getString("bio") ?: "",
                    updatedAt   = doc.getLong("updatedAt") ?: 0L
                )
                android.util.Log.d("UserRepositoryImpl", "remoteProfile fetched: $profile")
                profile
            } else {
                android.util.Log.d("UserRepositoryImpl", "remoteProfile doc does not exist")
                null
            }
        } catch (e: Exception) {
            android.util.Log.e("UserRepositoryImpl", "Error fetching remote profile", e)
            null  // Offline → bỏ qua lỗi mạng
        }

        val result = when {
            // Không có gì cả
            localEntity == null && remoteProfile == null -> {
                android.util.Log.d("UserRepositoryImpl", "Both local and remote are null")
                null
            }

            // Chỉ có remote → cache local và trả về
            localEntity == null -> {
                android.util.Log.d("UserRepositoryImpl", "localEntity is null, caching remote")
                userProfileDao.upsert(remoteProfile!!.toEntity())
                remoteProfile
            }

            // Offline → dùng local
            remoteProfile == null -> {
                android.util.Log.d("UserRepositoryImpl", "remoteProfile is null, returning local")
                localEntity.toDomain()
            }

            // Remote mới hơn → cập nhật local, trả về remote
            remoteProfile.updatedAt > localEntity.updatedAt -> {
                android.util.Log.d("UserRepositoryImpl", "remoteProfile is newer: ${remoteProfile.updatedAt} > ${localEntity.updatedAt}, updating local")
                userProfileDao.upsert(remoteProfile.toEntity())
                remoteProfile
            }

            // Local mới hơn hoặc bằng → dùng local
            else -> {
                android.util.Log.d("UserRepositoryImpl", "localEntity is newer or equal: ${localEntity.updatedAt} >= ${remoteProfile.updatedAt}, returning local")
                localEntity.toDomain()
            }
        }
        android.util.Log.d("UserRepositoryImpl", "getUserProfile returning: $result")
        return result
    }

    override suspend fun updateUserProfile(profile: UserProfile) {
        // Gắn timestamp hiện tại khi user lưu
        val updated = profile.copy(updatedAt = System.currentTimeMillis())
        android.util.Log.d("UserRepositoryImpl", "updateUserProfile called with: $updated")

        // Lưu local trước
        userProfileDao.upsert(updated.toEntity())
        android.util.Log.d("UserRepositoryImpl", "Saved to local Room DB")

        // Push lên Firestore (bỏ qua nếu offline)
        val userMap = hashMapOf(
            "uid"         to updated.uid,
            "displayName" to updated.displayName,
            "email"       to updated.email,
            "avatarUrl"   to updated.avatarUrl,
            "bio"         to updated.bio,
            "updatedAt"   to updated.updatedAt   // timestamp để conflict-resolve sau
        )
        try {
            firestore.collection("users").document(updated.uid).set(userMap).await()
            android.util.Log.d("UserRepositoryImpl", "Successfully saved to Firestore: $userMap")
        } catch (e: Exception) {
            android.util.Log.e("UserRepositoryImpl", "Failed to save to Firestore", e)
        }
    }

}
