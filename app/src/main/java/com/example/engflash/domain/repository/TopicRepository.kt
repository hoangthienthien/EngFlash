package com.example.engflash.domain.repository

import com.example.engflash.domain.model.Topic
import kotlinx.coroutines.flow.Flow

interface TopicRepository {
    fun getAllTopics(): Flow<List<Topic>>
}
