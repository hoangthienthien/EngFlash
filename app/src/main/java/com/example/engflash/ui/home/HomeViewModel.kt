package com.example.engflash.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.engflash.EngFlashApplication
import com.example.engflash.domain.model.Topic
import com.example.engflash.domain.model.User
import com.example.engflash.domain.model.Vocabulary
import com.example.engflash.domain.usecase.auth.GetCurrentUserUseCase
import com.example.engflash.domain.usecase.auth.LogoutUseCase
import com.example.engflash.domain.usecase.topic.GetAllTopicsUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.map

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as EngFlashApplication

    // ─── UseCases ────────────────────────────────────────
    private val getAllTopicsUseCase: GetAllTopicsUseCase = app.getAllTopicsUseCase
    private val getCurrentUserUseCase: GetCurrentUserUseCase = app.getCurrentUserUseCase
    private val logoutUseCase: LogoutUseCase = app.logoutUseCase

    /** Danh sách chủ đề từ Room DB (reactive Flow). */
    val topics: Flow<List<Topic>> = getAllTopicsUseCase()

    // ─── Vocabulary stats (direct DAO access for home) ───
    private val vocabDao = app.database.vocabularyDao()

    val totalWords: StateFlow<Int> = vocabDao.getTotalCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val learnedWords: StateFlow<Int> = vocabDao.getLearnedCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val favoriteCount: StateFlow<Int> = vocabDao.getFavoriteCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val vocabTopics: StateFlow<List<String>> = vocabDao.getAllTopics()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentlyAdded: StateFlow<List<Vocabulary>> = vocabDao.getRecentlyAdded()
        .map { list -> list.map { e ->
            Vocabulary(
                id = e.id, word = e.word, meaning = e.meaning, example = e.example,
                phonetic = e.phonetic, topic = e.topic, isFavorite = e.isFavorite,
                isLearned = e.isLearned, partOfSpeech = e.partOfSpeech,
                difficulty = e.difficulty, imageUrl = e.imageUrl
            )
        }}
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Get word count for a specific topic. */
    fun getTopicWordCount(topic: String): Flow<Int> = vocabDao.getCountByTopic(topic)

    /** Lấy thông tin user hiện tại. */
    fun getCurrentUser(): User? = getCurrentUserUseCase()

    /** Đăng xuất. */
    fun logout() {
        logoutUseCase()
    }
}
