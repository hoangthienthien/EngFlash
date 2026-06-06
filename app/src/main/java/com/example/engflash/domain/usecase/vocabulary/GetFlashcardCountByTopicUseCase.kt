package com.example.engflash.domain.usecase.vocabulary

import com.example.engflash.domain.repository.VocabularyRepository
import kotlinx.coroutines.flow.Flow

class GetFlashcardCountByTopicUseCase(
    private val repository: VocabularyRepository
) {
    operator fun invoke(topic: String): Flow<Int> {
        return repository.getFlashcardCountByTopic(topic)
    }
}
