package com.example.engflash.domain.usecase.grammar

import com.example.engflash.domain.model.GrammarRule
import com.example.engflash.domain.repository.GrammarRepository
import kotlinx.coroutines.flow.Flow

class SearchGrammarUseCase(
    private val repository: GrammarRepository
) {
    operator fun invoke(query: String): Flow<List<GrammarRule>> {
        return repository.searchGrammar(query)
    }
}
