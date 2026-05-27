package com.example.engflash.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "grammar_rules")
data class GrammarEntity(
    @PrimaryKey val id: String,
    val topicId: String,
    val title: String,
    val explanation: String,
    val structure: String,
    val usage: String,        // JSON array of strings — parsed in Mapper
    val examples: String,    // JSON array of {english, vietnamese} — parsed in Mapper
    val notes: String?,
    val orderIndex: Int,
    val questions: String    // JSON array of GrammarQuestion — parsed in Mapper
)
