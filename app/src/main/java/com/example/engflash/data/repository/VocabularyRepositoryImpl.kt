package com.example.engflash.data.repository

import com.example.engflash.data.local.dao.VocabularyDao
import com.example.engflash.data.mapper.toDomain
import com.example.engflash.data.mapper.toEntity
import com.example.engflash.domain.model.Vocabulary
import com.example.engflash.domain.repository.VocabularyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class VocabularyRepositoryImpl(
    private val vocabularyDao: VocabularyDao
) : VocabularyRepository {

    override fun getVocabulariesByTopic(topic: String): Flow<List<Vocabulary>> {
        return vocabularyDao.getByTopic(topic).map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getFavoriteVocabularies(): Flow<List<Vocabulary>> {
        return vocabularyDao.getFavorites().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun markAsLearned(id: Int) {
        vocabularyDao.markAsLearned(id)
    }

    override suspend fun toggleFavorite(id: Int, isFavorite: Boolean) {
        vocabularyDao.toggleFavorite(id, isFavorite)
    }

    override suspend fun addVocabulary(vocabulary: Vocabulary) {
        vocabularyDao.insert(vocabulary.toEntity())
    }

    override fun getAllTopics(): Flow<List<String>> {
        return vocabularyDao.getAllTopics()
    }
}
