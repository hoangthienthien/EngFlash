package com.example.engflash.data.repository

import com.example.engflash.data.local.dao.TopicDao
import com.example.engflash.data.mapper.toDomain
import com.example.engflash.domain.model.Topic
import com.example.engflash.domain.repository.TopicRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TopicRepositoryImpl(
    private val topicDao: TopicDao
) : TopicRepository {

    override fun getAllTopics(): Flow<List<Topic>> {
        return topicDao.getAllTopics().map { entities ->
            entities.map { it.toDomain() }
        }
    }
}
