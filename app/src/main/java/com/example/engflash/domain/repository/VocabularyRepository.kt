package com.example.engflash.domain.repository

import com.example.engflash.domain.model.Vocabulary
import kotlinx.coroutines.flow.Flow

interface VocabularyRepository {
    fun getVocabulariesByTopic(topic: String): Flow<List<Vocabulary>>
    fun getFavoriteVocabularies(): Flow<List<Vocabulary>>
    suspend fun markAsLearned(id: Int)
    suspend fun toggleFavorite(id: Int, isFavorite: Boolean)
    suspend fun updateLearnedStatus(id: Int, isLearned: Boolean, nextReview: Long, rating: String)
    suspend fun addVocabulary(vocabulary: Vocabulary)
    suspend fun deleteVocabulary(id: Int)
    suspend fun updateVocabulary(vocabulary: Vocabulary)
    suspend fun getVocabularyById(id: Int): Vocabulary?
    fun searchVocabularies(query: String): Flow<List<Vocabulary>>
    fun getAllTopics(): Flow<List<String>>

    suspend fun getAllList(): List<Vocabulary>
    suspend fun addVocabularyList(list: List<Vocabulary>): Int
    suspend fun deleteDuplicateVocabularies(): Int

    // ─── Flashcard (Practice) ───
    fun getFlashcardByTopic(topic: String): Flow<List<Vocabulary>>
    fun getFlashcardTopics(): Flow<List<String>>
    fun getFlashcardCountByTopic(topic: String): Flow<Int>
    fun getAllFlashcards(): Flow<List<Vocabulary>>
}
