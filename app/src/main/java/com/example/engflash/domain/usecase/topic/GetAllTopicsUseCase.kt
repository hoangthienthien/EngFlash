package com.example.engflash.domain.usecase.topic

import com.example.engflash.domain.model.Topic
import com.example.engflash.domain.repository.TopicRepository
import kotlinx.coroutines.flow.Flow

/**
 * UseCase: Lấy danh sách tất cả chủ đề học tập từ local Room DB.
 */
class GetAllTopicsUseCase(
    private val topicRepository: TopicRepository
) {
    operator fun invoke(): Flow<List<Topic>> {
        return topicRepository.getAllTopics()
    }
}
