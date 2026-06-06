package com.example.engflash.domain.usecase.vocabulary

import com.example.engflash.domain.repository.VocabularyRepository

class DeleteDuplicateVocabulariesUseCase(
    private val repository: VocabularyRepository
) {
    suspend operator fun invoke(): Int {
        return repository.deleteDuplicateVocabularies()
    }
}
