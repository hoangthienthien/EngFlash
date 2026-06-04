package com.example.engflash.domain.usecase.vocabulary

import com.example.engflash.domain.repository.VocabularyRepository

class UpdateVocabularyLearnedStatusUseCase(
    private val repository: VocabularyRepository
) {
    suspend operator fun invoke(id: Int, isLearned: Boolean, nextReview: Long, rating: String) {
        repository.updateLearnedStatus(id, isLearned, nextReview, rating)
    }
}
