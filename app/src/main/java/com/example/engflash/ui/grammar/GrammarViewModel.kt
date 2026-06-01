package com.example.engflash.ui.grammar

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.engflash.EngFlashApplication
import com.example.engflash.domain.model.GrammarRule
import com.example.engflash.domain.usecase.grammar.GetGrammarByIdUseCase
import com.example.engflash.domain.usecase.grammar.GetGrammarByTopicUseCase
import kotlinx.coroutines.flow.Flow

import kotlinx.coroutines.flow.map
import com.example.engflash.data.mapper.toDomain

class GrammarViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as EngFlashApplication

    val allGrammarRules: Flow<List<GrammarRule>> = app.database.grammarDao().getAllGrammarRules().map { entities ->
        entities.map { it.toDomain() }
    }

    // ─── UseCases ────────────────────────────────────────
    private val getGrammarByTopicUseCase: GetGrammarByTopicUseCase = app.getGrammarByTopicUseCase
    private val getGrammarByIdUseCase: GetGrammarByIdUseCase = app.getGrammarByIdUseCase

    fun getCurrentUser() = app.getCurrentUserUseCase()

    /** Lấy danh sách bài ngữ pháp theo chủ đề. */
    fun getGrammarByTopic(topicId: String): Flow<List<GrammarRule>> {
        return getGrammarByTopicUseCase(topicId)
    }

    /** Lấy chi tiết 1 bài ngữ pháp theo ID. */
    fun getGrammarById(grammarId: String): Flow<GrammarRule?> {
        return getGrammarByIdUseCase(grammarId)
    }
}
