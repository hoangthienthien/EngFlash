package com.example.engflash.data.local.database

import android.content.Context
import com.example.engflash.data.local.entity.GrammarEntity
import com.example.engflash.data.local.entity.TopicEntity
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private data class GrammarExampleSeed(
    val english: String,
    val vietnamese: String
)

private data class GrammarQuestionSeed(
    val id: String,
    val questionText: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val explanation: String
)

private data class GrammarRuleSeed(
    val id: String,
    val topicId: String,
    val title: String,
    val explanation: String,
    val structure: String,
    val usage: List<String>,
    val examples: List<GrammarExampleSeed>,
    val notes: String?,
    val orderIndex: Int,
    val questions: List<GrammarQuestionSeed>?
) {
    fun toEntity(gson: Gson): GrammarEntity {
        return GrammarEntity(
            id = id,
            topicId = topicId,
            title = title,
            explanation = explanation,
            structure = structure,
            usage = gson.toJson(usage),
            examples = gson.toJson(examples),
            notes = notes,
            orderIndex = orderIndex,
            questions = gson.toJson(questions ?: emptyList<GrammarQuestionSeed>())
        )
    }
}

/**
 * Dữ liệu gốc trong file JSON (chứa cả topics & grammarRules).
 */
private data class SeedData(
    val topics: List<TopicEntity>,
    val grammarRules: List<GrammarRuleSeed>
)

class DataSeeder(
    private val context: Context,
    private val database: AppDatabase
) {
    /**
     * Đọc assets/data.json và insert vào Room DB nếu bảng còn trống.
     * Chỉ chạy 1 lần (lần đầu mở app).
     */
    suspend fun seedIfNeeded() = withContext(Dispatchers.IO) {
        val topicCount = database.topicDao().getCount()
        if (topicCount > 0) return@withContext // Đã seed rồi, bỏ qua

        val jsonString = context.assets
            .open("data.json")
            .bufferedReader()
            .use { it.readText() }

        val gson = Gson()
        val seedData = gson.fromJson(jsonString, SeedData::class.java)

        val grammarEntities = seedData.grammarRules.map { it.toEntity(gson) }

        database.topicDao().insertAll(seedData.topics)
        database.grammarDao().insertAll(grammarEntities)
    }
}
