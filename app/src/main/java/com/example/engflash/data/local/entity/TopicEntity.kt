package com.example.engflash.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "topics")
data class TopicEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val iconName: String,
    val orderIndex: Int,
    val totalLessons: Int
)
