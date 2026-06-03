package com.example.engflash.data.local.dao

import androidx.room.*
import com.example.engflash.data.local.entity.VocabularyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VocabularyDao {

    @Query("SELECT * FROM vocabularies WHERE topic = :topic ORDER BY id ASC")
    fun getByTopic(topic: String): Flow<List<VocabularyEntity>>

    @Query("UPDATE vocabularies SET isLearned = 1 WHERE id = :id")
    suspend fun markAsLearned(id: Int)

    @Query("UPDATE vocabularies SET isLearned = :isLearned WHERE id = :id")
    suspend fun updateLearnedStatus(id: Int, isLearned: Boolean)

    @Query("UPDATE vocabularies SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun toggleFavorite(id: Int, isFavorite: Boolean)

    @Query("SELECT * FROM vocabularies ORDER BY id ASC")
    fun getAll(): Flow<List<VocabularyEntity>>

    @Query("SELECT * FROM vocabularies WHERE isFavorite = 1 ORDER BY id ASC")
    fun getFavorites(): Flow<List<VocabularyEntity>>

    @Query("SELECT COUNT(*) FROM vocabularies WHERE isLearned = 1")
    fun getLearnedCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM vocabularies")
    fun getTotalCount(): Flow<Int>

    @Query("SELECT DISTINCT topic FROM vocabularies")
    fun getAllTopics(): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM vocabularies WHERE topic = :topic")
    fun getCountByTopic(topic: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM vocabularies WHERE isFavorite = 1")
    fun getFavoriteCount(): Flow<Int>

    @Query("SELECT * FROM vocabularies ORDER BY id DESC LIMIT 5")
    fun getRecentlyAdded(): Flow<List<VocabularyEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vocabulary: VocabularyEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<VocabularyEntity>)
}
