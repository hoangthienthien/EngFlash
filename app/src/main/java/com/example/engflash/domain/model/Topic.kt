package com.example.engflash.domain.model

data class Topic(
    val id: String,
    val name: String,
    val description: String,
    val iconName: String,
    val orderIndex: Int,
    val totalLessons: Int
)
