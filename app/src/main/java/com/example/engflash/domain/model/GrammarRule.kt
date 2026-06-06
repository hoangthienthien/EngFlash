package com.example.engflash.domain.model

data class GrammarRule(
    val id: String,
    val topicId: String,
    val title: String,
    val explanation: String,
    val structure: String,
    val usage: List<String>,
    val examples: List<GrammarExample>,
    val notes: String?,
    val orderIndex: Int,
    val questions: List<GrammarQuestion> = emptyList()
)

data class GrammarExample(
    val english: String,
    val vietnamese: String
)

data class GrammarQuestion(
    val id: String,
    val questionText: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val explanation: String
)

