package com.example.engflash.domain.model

data class Vocabulary(
    val id: Int,
    val word: String,
    val meaning: String,
    val example: String,
    val phonetic: String,
    val topic: String,
    val isFavorite: Boolean,
    val isLearned: Boolean,
    val partOfSpeech: String,
    val difficulty: String,
    val imageUrl: String? = null
)
