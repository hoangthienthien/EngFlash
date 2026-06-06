package com.example.engflash.domain.usecase

import com.example.engflash.domain.repository.VocabularyRepository
import kotlinx.coroutines.flow.Flow

class GetUniqueVocabTopicsUseCase(
    private val repository: VocabularyRepository
) {
    operator fun invoke(): Flow<List<String>> {
        return repository.getAllTopics()
    }
}
