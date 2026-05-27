package com.example.engflash.data.repository

import com.example.engflash.data.local.dao.GrammarDao
import com.example.engflash.data.mapper.toDomain
import com.example.engflash.domain.model.GrammarRule
import com.example.engflash.domain.repository.GrammarRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GrammarRepositoryImpl(
    private val grammarDao: GrammarDao
) : GrammarRepository {

    override fun getGrammarByTopic(topicId: String): Flow<List<GrammarRule>> {
        return grammarDao.getGrammarByTopic(topicId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getGrammarById(id: String): Flow<GrammarRule?> {
        return grammarDao.getGrammarById(id).map { entity ->
            entity?.toDomain()
        }
    }
}
