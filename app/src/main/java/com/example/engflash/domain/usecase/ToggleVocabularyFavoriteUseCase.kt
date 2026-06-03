package com.example.engflash.domain.usecase

import com.example.engflash.domain.repository.VocabularyRepository

class ToggleVocabularyFavoriteUseCase(
    private val repository: VocabularyRepository
) {
    suspend operator fun invoke(id: Int, isFavorite: Boolean) {
        repository.toggleFavorite(id, isFavorite)
    }
}
