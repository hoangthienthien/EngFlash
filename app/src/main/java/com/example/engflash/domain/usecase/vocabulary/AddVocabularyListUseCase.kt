package com.example.engflash.domain.usecase.vocabulary

import com.example.engflash.domain.model.Vocabulary
import com.example.engflash.domain.repository.VocabularyRepository

class AddVocabularyListUseCase(
    private val repository: VocabularyRepository
) {
    suspend operator fun invoke(list: List<Vocabulary>): Int {
        return repository.addVocabularyList(list)
    }
}
