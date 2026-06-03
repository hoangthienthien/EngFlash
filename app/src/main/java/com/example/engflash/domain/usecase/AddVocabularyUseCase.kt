package com.example.engflash.domain.usecase

import com.example.engflash.domain.model.Vocabulary
import com.example.engflash.domain.repository.VocabularyRepository

class AddVocabularyUseCase(
    private val repository: VocabularyRepository
) {
    suspend operator fun invoke(vocabulary: Vocabulary) {
        repository.addVocabulary(vocabulary)
    }
}
