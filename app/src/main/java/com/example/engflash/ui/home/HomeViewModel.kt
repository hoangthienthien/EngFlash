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

data class VocabularyStats(
    val totalCount: Int = 0,
    val masteredCount: Int = 0,
    val reviewingCount: Int = 0,
    val unlearnedCount: Int = 0,
    val retentionRate: Int = 0
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as EngFlashApplication

    // ─── UseCases ────────────────────────────────────────
    private val getAllTopicsUseCase: GetAllTopicsUseCase = app.getAllTopicsUseCase
    private val getCurrentUserUseCase: GetCurrentUserUseCase = app.getCurrentUserUseCase
    private val logoutUseCase: LogoutUseCase = app.logoutUseCase

    // ─── Streak ─────────────────────────────────────────
    private val streakManager = app.streakManager
    val currentStreak: StateFlow<Int> = streakManager.currentStreak
    val longestStreak: StateFlow<Int> = streakManager.longestStreak

    /** Danh sách chủ đề từ Room DB (reactive Flow). */
    val topics: Flow<List<Topic>> = getAllTopicsUseCase()

    // ─── Vocabulary stats (direct DAO access for home) ───
    private val vocabDao = app.database.vocabularyDao()

    val vocabStats: StateFlow<VocabularyStats> = vocabDao.getAll().map { list ->
        val prefs = app.getSharedPreferences("engflash_prefs", android.content.Context.MODE_PRIVATE)
        val now = System.currentTimeMillis()
        var mastered = 0
        var reviewing = 0
        var unlearned = 0

        for (vocab in list) {
            val rating = prefs.getString("rating_${vocab.id}", null)
            val level = if (rating != null) {
                rating.lowercase()
            } else {
                val nextReview = prefs.getLong("next_review_${vocab.id}", 0L)
                if (nextReview == 0L) {
                    "unlearned"
                } else {
                    val diff = nextReview - now
                    if (diff > 2 * 24 * 60 * 60 * 1000L) {
                        "easy"
                    } else if (diff > 5 * 60 * 1000L) {
                        "good"
                    } else {
                        "again"
                    }
                }
            }

            when (level) {
                "giỏi", "easy", "good", "được" -> mastered++
                "hard" -> {
                    reviewing++
                    unlearned++
                }
                else -> unlearned++
            }
        }

        val total = list.size
        val retention = if (total > 0) ((mastered + reviewing) * 100 / total) else 0

        VocabularyStats(
            totalCount = total,
            masteredCount = mastered,
            reviewingCount = reviewing,
            unlearnedCount = unlearned,
            retentionRate = retention
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = VocabularyStats()
    )

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
