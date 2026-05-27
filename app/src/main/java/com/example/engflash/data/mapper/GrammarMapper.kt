package com.example.engflash.data.mapper

import com.example.engflash.data.local.entity.GrammarEntity
import com.example.engflash.domain.model.GrammarExample
import com.example.engflash.domain.model.GrammarQuestion
import com.example.engflash.domain.model.GrammarRule
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

private val gson = Gson()

/**
 * Chuyển đổi GrammarEntity (Data layer) → GrammarRule (Domain layer).
 * Parse JSON strings (usage, examples, questions) thành List.
 */
fun GrammarEntity.toDomain(): GrammarRule {
    val usageList: List<String> = try {
        gson.fromJson(usage, object : TypeToken<List<String>>() {}.type)
    } catch (e: Exception) {
        emptyList()
    }

    val exampleList: List<GrammarExample> = try {
        gson.fromJson(examples, object : TypeToken<List<GrammarExample>>() {}.type)
    } catch (e: Exception) {
        emptyList()
    }

    val questionList: List<GrammarQuestion> = try {
        gson.fromJson(questions, object : TypeToken<List<GrammarQuestion>>() {}.type)
    } catch (e: Exception) {
        emptyList()
    }

    return GrammarRule(
        id = id,
        topicId = topicId,
        title = title,
        explanation = explanation,
        structure = structure,
        usage = usageList,
        examples = exampleList,
        notes = notes,
        orderIndex = orderIndex,
        questions = questionList
    )
}

/**
 * Chuyển đổi GrammarRule (Domain layer) → GrammarEntity (Data layer).
 * Serialize List thành JSON strings.
 */
fun GrammarRule.toEntity(): GrammarEntity {
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
        questions = gson.toJson(questions)
    )
}

