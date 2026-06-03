package com.example.engflash.domain.usecase

import com.example.engflash.domain.repository.VocabularyRepository

class MarkVocabularyAsLearnedUseCase(
    private val repository: VocabularyRepository
) {
    suspend operator fun invoke(id: Int) {
        repository.markAsLearned(id)
    }
}
