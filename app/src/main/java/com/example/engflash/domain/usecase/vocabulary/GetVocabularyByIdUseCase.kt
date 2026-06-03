package com.example.engflash.domain.usecase.vocabulary

import com.example.engflash.domain.model.Vocabulary
import com.example.engflash.domain.repository.VocabularyRepository

class GetVocabularyByIdUseCase(
    private val repository: VocabularyRepository
) {
    suspend operator fun invoke(id: Int): Vocabulary? {
        return repository.getVocabularyById(id)
    }
}
