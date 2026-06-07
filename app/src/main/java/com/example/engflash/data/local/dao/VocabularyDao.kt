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

    @Query("SELECT * FROM vocabularies ORDER BY id ASC")
    suspend fun getAllList(): List<VocabularyEntity>

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
    suspend fun insert(vocabulary: VocabularyEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<VocabularyEntity>)

    @Query("DELETE FROM vocabularies WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Update
    suspend fun update(vocabulary: VocabularyEntity)

    @Query("SELECT * FROM vocabularies WHERE id = :id")
    suspend fun getById(id: Int): VocabularyEntity?

    @Query("SELECT * FROM vocabularies WHERE word = :word LIMIT 1")
    suspend fun getByWord(word: String): VocabularyEntity?

    @Query("SELECT * FROM vocabularies WHERE LOWER(word) = LOWER(:word) LIMIT 1")
    suspend fun getByWordIgnoreCase(word: String): VocabularyEntity?

    @Query("SELECT * FROM vocabularies WHERE word LIKE '%' || :query || '%' OR meaning LIKE '%' || :query || '%' ORDER BY word ASC")
    fun search(query: String): Flow<List<VocabularyEntity>>

    // ─── Flashcard (isFavorite) queries for Practice screen ───

    @Query("SELECT * FROM vocabularies WHERE isFavorite = 1 AND topic = :topic ORDER BY id ASC")
    fun getFlashcardByTopic(topic: String): Flow<List<VocabularyEntity>>

    @Query("SELECT DISTINCT topic FROM vocabularies WHERE isFavorite = 1")
    fun getFlashcardTopics(): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM vocabularies WHERE isFavorite = 1 AND topic = :topic")
    fun getFlashcardCountByTopic(topic: String): Flow<Int>

    @Query("SELECT * FROM vocabularies WHERE isFavorite = 1 ORDER BY id ASC")
    fun getAllFlashcards(): Flow<List<VocabularyEntity>>

    @Query("UPDATE vocabularies SET isLearned = 0, isFavorite = 0")
    suspend fun resetAllVocabularyProgress()
}
