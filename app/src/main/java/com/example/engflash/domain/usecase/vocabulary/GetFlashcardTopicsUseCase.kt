package com.example.engflash.domain.usecase.vocabulary

import com.example.engflash.domain.repository.VocabularyRepository
import kotlinx.coroutines.flow.Flow

class GetFlashcardTopicsUseCase(
    private val repository: VocabularyRepository
) {
    operator fun invoke(): Flow<List<String>> {
        return repository.getFlashcardTopics()
    }
}
