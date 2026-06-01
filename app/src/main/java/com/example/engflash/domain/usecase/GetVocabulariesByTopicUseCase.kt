package com.example.engflash.domain.usecase

import com.example.engflash.domain.model.Vocabulary
import com.example.engflash.domain.repository.VocabularyRepository
import kotlinx.coroutines.flow.Flow

class GetVocabulariesByTopicUseCase(
    private val repository: VocabularyRepository
) {
    operator fun invoke(topic: String): Flow<List<Vocabulary>> {
        return repository.getVocabulariesByTopic(topic)
    }
}
