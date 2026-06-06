package com.example.engflash.domain.usecase.vocabulary

import com.example.engflash.domain.model.Vocabulary
import com.example.engflash.domain.repository.VocabularyRepository
import kotlinx.coroutines.flow.Flow

class SearchVocabularyUseCase(
    private val repository: VocabularyRepository
) {
    operator fun invoke(query: String): Flow<List<Vocabulary>> {
        return repository.searchVocabularies(query)
    }
}
