package com.example.engflash.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vocabularies")
data class VocabularyEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val word: String,
    val meaning: String,
    val example: String,
    val phonetic: String,
    val topic: String,
    val isFavorite: Boolean = false,
    val isLearned: Boolean = false,
    val partOfSpeech: String = "NOUN",
    val difficulty: String = "ADVANCED",
    val imageUrl: String? = null
)
