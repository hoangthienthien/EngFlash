package com.example.engflash.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.engflash.data.local.entity.GrammarEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GrammarDao {

    @Query("SELECT * FROM grammar_rules WHERE topicId = :topicId ORDER BY orderIndex ASC")
    fun getGrammarByTopic(topicId: String): Flow<List<GrammarEntity>>

    @Query("SELECT * FROM grammar_rules WHERE id = :id LIMIT 1")
    fun getGrammarById(id: String): Flow<GrammarEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(grammars: List<GrammarEntity>)

    @Query("SELECT COUNT(*) FROM grammar_rules")
    suspend fun getCount(): Int
}
