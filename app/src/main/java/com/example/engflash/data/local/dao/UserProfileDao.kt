package com.example.engflash.data.local.dao

import androidx.room.*
import com.example.engflash.data.local.entity.UserProfileEntity

@Dao
interface UserProfileDao {

    @Query("SELECT * FROM user_profile WHERE uid = :uid")
    suspend fun getByUid(uid: String): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: UserProfileEntity)

    @Query("DELETE FROM user_profile")
    suspend fun deleteAll()
}
