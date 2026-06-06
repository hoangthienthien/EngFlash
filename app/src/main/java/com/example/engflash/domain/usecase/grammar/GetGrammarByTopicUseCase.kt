package com.example.engflash.domain.usecase.grammar

import com.example.engflash.domain.model.GrammarRule
import com.example.engflash.domain.repository.GrammarRepository
import kotlinx.coroutines.flow.Flow

/**
 * UseCase: Lấy danh sách bài ngữ pháp theo chủ đề.
 */
class GetGrammarByTopicUseCase(
    private val grammarRepository: GrammarRepository
) {
    operator fun invoke(topicId: String): Flow<List<GrammarRule>> {
        return grammarRepository.getGrammarByTopic(topicId)
    }
}
