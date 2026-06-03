package com.example.engflash.domain.repository

import com.example.engflash.domain.model.Vocabulary
import kotlinx.coroutines.flow.Flow

interface VocabularyRepository {
    fun getVocabulariesByTopic(topic: String): Flow<List<Vocabulary>>
    fun getFavoriteVocabularies(): Flow<List<Vocabulary>>
    suspend fun markAsLearned(id: Int)
    suspend fun toggleFavorite(id: Int, isFavorite: Boolean)
    suspend fun addVocabulary(vocabulary: Vocabulary)
    suspend fun deleteVocabulary(id: Int)
    suspend fun updateVocabulary(vocabulary: Vocabulary)
    suspend fun getVocabularyById(id: Int): Vocabulary?
    fun searchVocabularies(query: String): Flow<List<Vocabulary>>
    fun getAllTopics(): Flow<List<String>>
}
