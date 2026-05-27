package com.example.engflash.data.mapper

import com.example.engflash.data.local.entity.TopicEntity
import com.example.engflash.domain.model.Topic

fun TopicEntity.toDomain(): Topic {
    return Topic(
        id = id,
        name = name,
        description = description,
        iconName = iconName,
        orderIndex = orderIndex,
        totalLessons = totalLessons
    )
}

fun Topic.toEntity(): TopicEntity {
    return TopicEntity(
        id = id,
        name = name,
        description = description,
        iconName = iconName,
        orderIndex = orderIndex,
        totalLessons = totalLessons
    )
}
