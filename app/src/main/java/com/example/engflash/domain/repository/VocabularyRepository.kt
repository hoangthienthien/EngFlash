package com.example.engflash.domain.repository

import com.example.engflash.domain.model.Vocabulary
import kotlinx.coroutines.flow.Flow

interface VocabularyRepository {
    fun getVocabulariesByTopic(topic: String): Flow<List<Vocabulary>>
    fun getFavoriteVocabularies(): Flow<List<Vocabulary>>
    suspend fun markAsLearned(id: Int)
    suspend fun toggleFavorite(id: Int, isFavorite: Boolean)
    suspend fun addVocabulary(vocabulary: Vocabulary)
    fun getAllTopics(): Flow<List<String>>
}
