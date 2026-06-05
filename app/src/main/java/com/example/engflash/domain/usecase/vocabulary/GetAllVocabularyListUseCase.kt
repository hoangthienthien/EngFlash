package com.example.engflash.domain.usecase.vocabulary

import com.example.engflash.domain.model.Vocabulary
import com.example.engflash.domain.repository.VocabularyRepository

class GetAllVocabularyListUseCase(
    private val repository: VocabularyRepository
) {
    suspend operator fun invoke(): List<Vocabulary> {
        return repository.getAllList()
    }
}
