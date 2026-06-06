package com.example.engflash.domain.usecase

import com.example.engflash.domain.model.Vocabulary
import com.example.engflash.domain.repository.VocabularyRepository
import kotlinx.coroutines.flow.Flow

class GetFavoriteVocabulariesUseCase(
    private val repository: VocabularyRepository
) {
    operator fun invoke(): Flow<List<Vocabulary>> {
        return repository.getFavoriteVocabularies()
    }
}
