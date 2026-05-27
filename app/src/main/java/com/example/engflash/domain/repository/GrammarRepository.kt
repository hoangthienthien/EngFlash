package com.example.engflash.domain.repository

import com.example.engflash.domain.model.GrammarRule
import kotlinx.coroutines.flow.Flow

interface GrammarRepository {
    fun getGrammarByTopic(topicId: String): Flow<List<GrammarRule>>
    fun getGrammarById(id: String): Flow<GrammarRule?>
}
