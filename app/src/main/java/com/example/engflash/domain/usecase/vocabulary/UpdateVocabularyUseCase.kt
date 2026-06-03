package com.example.engflash.domain.usecase.vocabulary

import com.example.engflash.domain.model.Vocabulary
import com.example.engflash.domain.repository.VocabularyRepository

class UpdateVocabularyUseCase(
    private val repository: VocabularyRepository
) {
    suspend operator fun invoke(vocabulary: Vocabulary) {
        repository.updateVocabulary(vocabulary)
    }
}
