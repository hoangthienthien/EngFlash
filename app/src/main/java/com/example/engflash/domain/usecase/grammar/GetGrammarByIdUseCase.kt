package com.example.engflash.domain.usecase.grammar

import com.example.engflash.domain.model.GrammarRule
import com.example.engflash.domain.repository.GrammarRepository
import kotlinx.coroutines.flow.Flow

/**
 * UseCase: Lấy chi tiết 1 bài ngữ pháp theo ID.
 */
class GetGrammarByIdUseCase(
    private val grammarRepository: GrammarRepository
) {
    operator fun invoke(grammarId: String): Flow<GrammarRule?> {
        return grammarRepository.getGrammarById(grammarId)
    }
}
