package com.example.engflash.domain.usecase.vocabulary

import com.example.engflash.domain.repository.VocabularyRepository

class DeleteVocabularyUseCase(
    private val repository: VocabularyRepository
) {
    suspend operator fun invoke(id: Int) {
        repository.deleteVocabulary(id)
    }
}
