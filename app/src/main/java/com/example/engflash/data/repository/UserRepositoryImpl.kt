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
        val localEntity = userProfileDao.getByUid(uid)

        // Thử fetch remote (bất đồng bộ, bỏ qua nếu offline)
        val remoteProfile: UserProfile? = try {
            val doc = firestore.collection("users").document(uid).get().await()
            if (doc.exists()) {
                UserProfile(
                    uid         = uid,
                    displayName = doc.getString("displayName") ?: "",
                    email       = doc.getString("email") ?: "",
                    avatarUrl   = doc.getString("avatarUrl") ?: "",
                    bio         = doc.getString("bio") ?: "",
                    updatedAt   = doc.getLong("updatedAt") ?: 0L
                )
            } else null
        } catch (e: Exception) {
            null  // Offline → bỏ qua lỗi mạng
        }

        return when {
            // Không có gì cả
            localEntity == null && remoteProfile == null -> null

            // Chỉ có remote → cache local và trả về
            localEntity == null -> {
                userProfileDao.upsert(remoteProfile!!.toEntity())
                remoteProfile
            }

            // Offline → dùng local
            remoteProfile == null -> localEntity.toDomain()

            // Remote mới hơn → cập nhật local, trả về remote
            remoteProfile.updatedAt > localEntity.updatedAt -> {
                userProfileDao.upsert(remoteProfile.toEntity())
                remoteProfile
            }

            // Local mới hơn hoặc bằng → dùng local
            else -> localEntity.toDomain()
        }
    }

    override suspend fun updateUserProfile(profile: UserProfile) {
        // Gắn timestamp hiện tại khi user lưu
        val updated = profile.copy(updatedAt = System.currentTimeMillis())

        // Lưu local trước
        userProfileDao.upsert(updated.toEntity())

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
        } catch (e: Exception) {
            e.printStackTrace()
            // Offline → local đã lưu, Firestore sẽ sync tự động khi có mạng
        }
    }
}
