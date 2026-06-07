package com.example.engflash.ui.vocabulary

import com.example.engflash.util.SM2Algorithm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.engflash.EngFlashApplication
import com.example.engflash.data.mapper.toDomain
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class FlashcardViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as EngFlashApplication

    private val getVocabulariesByTopicUseCase = app.getVocabulariesByTopicUseCase
    private val getFavoriteVocabulariesUseCase = app.getFavoriteVocabulariesUseCase
    private val markVocabularyAsLearnedUseCase = app.markVocabularyAsLearnedUseCase
    private val toggleVocabularyFavoriteUseCase = app.toggleVocabularyFavoriteUseCase
    private val updateVocabularyLearnedStatusUseCase = app.updateVocabularyLearnedStatusUseCase
    private val addVocabularyUseCase = app.addVocabularyUseCase
    private val getUniqueVocabTopicsUseCase = app.getUniqueVocabTopicsUseCase
    private val getFlashcardByTopicUseCase = app.getFlashcardByTopicUseCase
    private val getFlashcardTopicsUseCase = app.getFlashcardTopicsUseCase
    private val soundManager = app.soundManager
    val streakManager = app.streakManager

    val allTopics: StateFlow<List<String>> = getUniqueVocabTopicsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // ─── Flashcard Practice Topics (topics that have isFavorite words) ───
    val flashcardTopics: StateFlow<List<String>> = getFlashcardTopicsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun getTopicWordCount(topic: String): kotlinx.coroutines.flow.Flow<Int> {
        return app.database.vocabularyDao().getCountByTopic(topic)
    }

    fun getFlashcardCountByTopic(topic: String): kotlinx.coroutines.flow.Flow<Int> {
        return app.getFlashcardCountByTopicUseCase(topic)
    }

    fun getTopicFlashcardProgress(topic: String): kotlinx.coroutines.flow.Flow<Pair<Int, Int>> {
        return getFlashcardByTopicUseCase(topic).map { list ->
            val prefs = app.getSharedPreferences("engflash_prefs", android.content.Context.MODE_PRIVATE)
            val now = System.currentTimeMillis()
            val mastered = list.count { vocab ->
                val rating = prefs.getString("rating_${vocab.id}", null)
                if (rating != null) {
                    rating.lowercase() in listOf("good", "easy", "giỏi")
                } else {
                    val nextReview = prefs.getLong("next_review_${vocab.id}", 0L)
                    nextReview - now > 2 * 24 * 60 * 60 * 1000L
                }
            }
            Pair(mastered, list.size)
        }
    }

    private val _uiState = MutableStateFlow(FlashcardUiState())
    val uiState: StateFlow<FlashcardUiState> = _uiState.asStateFlow()

    // Track which vocab IDs have been reviewed in current session
    private val _reviewedIds = MutableStateFlow<Set<Int>>(emptySet())
    val reviewedIds: StateFlow<Set<Int>> = _reviewedIds.asStateFlow()

    private var loadJob: kotlinx.coroutines.Job? = null

    fun loadTopicOrFilter(filter: String) {
        loadJob?.cancel()
        _uiState.value = _uiState.value.copy(isLoading = true, selectedFilter = filter)
        _reviewedIds.value = emptySet()
        loadJob = viewModelScope.launch {
            val flow = when (filter) {
                "All" -> app.database.vocabularyDao().getAll().map { list -> list.map { it.toDomain() } }
                "Favorites" -> getFavoriteVocabulariesUseCase()
                else -> getVocabulariesByTopicUseCase(filter)
            }
            // Load data ONCE only — no continuous collection so removals stick
            val list = flow.first()
            val prefs = app.getSharedPreferences("engflash_prefs", android.content.Context.MODE_PRIVATE)
            val now = System.currentTimeMillis()
            val dueList = list.filter { vocab ->
                val nextReview = prefs.getLong("next_review_${vocab.id}", 0L)
                now >= nextReview
            }
            _uiState.value = _uiState.value.copy(
                vocabularies = dueList,
                isLoading = false,
                currentIndex = 0
            )
        }
    }

    // Used by VocabularyListScreen — loads ALL words in topic, no spaced repetition filter
    fun loadTopic(topic: String) {
        loadJob?.cancel()
        _uiState.value = _uiState.value.copy(isLoading = true, selectedFilter = topic)
        loadJob = viewModelScope.launch {
            val flow = getVocabulariesByTopicUseCase(topic)
            flow.collect { list ->
                _uiState.value = _uiState.value.copy(
                    vocabularies = list,
                    isLoading = false,
                    currentIndex = 0
                )
            }
        }
    }

    /**
     * Load flashcard words for a specific topic (only isFavorite = true).
     * Used by the new Practice flow — words persist and are NOT removed after review.
     */
    fun loadFlashcardByTopic(topic: String) {
        loadJob?.cancel()
        _uiState.value = _uiState.value.copy(isLoading = true, selectedFilter = topic)
        _reviewedIds.value = emptySet()
        loadJob = viewModelScope.launch {
            val flow = getFlashcardByTopicUseCase(topic)
            flow.collect { list ->
                val prefs = app.getSharedPreferences("engflash_prefs", android.content.Context.MODE_PRIVATE)
                val now = System.currentTimeMillis()
                val filteredList = list.filter { vocab ->
                    val rating = prefs.getString("rating_${vocab.id}", null)
                    val nextReview = prefs.getLong("next_review_${vocab.id}", 0L)
                    if (rating?.lowercase() == "again") {
                        now >= nextReview // Chỉ hiển thị lại khi đã đến hoặc quá hạn 90 giây ôn tập
                    } else {
                        true // Các nút khác (Hard/Good/Easy hoặc chưa học) thì luôn luôn hiển thị
                    }
                }
                _uiState.value = _uiState.value.copy(
                    vocabularies = filteredList,
                    isLoading = false,
                    currentIndex = if (_uiState.value.currentIndex >= filteredList.size) 0 else _uiState.value.currentIndex
                )
            }
        }
    }

    fun nextCard() {
        val list = _uiState.value.vocabularies
        if (list.isEmpty()) return
        val nextIndex = (_uiState.value.currentIndex + 1) % list.size
        _uiState.value = _uiState.value.copy(
            currentIndex = nextIndex,
            isFlipped = false
        )
    }

    fun previousCard() {
        val list = _uiState.value.vocabularies
        if (list.isEmpty()) return
        val prevIndex = if (_uiState.value.currentIndex - 1 < 0) {
            list.size - 1
        } else {
            _uiState.value.currentIndex - 1
        }
        _uiState.value = _uiState.value.copy(
            currentIndex = prevIndex,
            isFlipped = false
        )
    }

    fun flipCard() {
        _uiState.value = _uiState.value.copy(
            isFlipped = !_uiState.value.isFlipped
        )
    }

    /**
     * Review card in PRACTICE mode — words stay in the list (persistent).
     * Sử dụng thuật toán SM-2 để tính toán khoảng cách ôn tập.
     */
    fun reviewCardPersistent(vocabId: Int, rating: String) {
        // Ghi nhận ngày học
        app.streakManager.recordStudyDay()

        val prefs = app.getSharedPreferences("engflash_prefs", android.content.Context.MODE_PRIVATE)

        // Đọc thông số SM-2 hiện tại từ SharedPreferences
        val currentEF = prefs.getFloat("ease_factor_$vocabId", SM2Algorithm.DEFAULT_EASE_FACTOR.toFloat()).toDouble()
        val currentReps = prefs.getInt("repetitions_$vocabId", SM2Algorithm.DEFAULT_REPETITIONS)
        val currentInterval = prefs.getInt("interval_$vocabId", SM2Algorithm.DEFAULT_INTERVAL)

        // Tính toán SM-2
        val sm2Rating = SM2Algorithm.Rating.fromString(rating)
        val result = SM2Algorithm.calculate(sm2Rating, currentEF, currentReps, currentInterval)

        // Phát âm thanh phản hồi
        if (sm2Rating == SM2Algorithm.Rating.AGAIN) {
            soundManager.playWrongSound()
        } else {
            soundManager.playCorrectSound()
        }

        // Lưu kết quả SM-2 vào SharedPreferences
        prefs.edit()
            .putLong("next_review_$vocabId", result.nextReviewMs)
            .putString("rating_$vocabId", rating.lowercase())
            .putFloat("ease_factor_$vocabId", result.easeFactor.toFloat())
            .putInt("repetitions_$vocabId", result.repetitions)
            .putInt("interval_$vocabId", result.interval)
            .apply()

        // Update database: Again = false (unlearned/retry), Hard/Good/Easy = true (learned)
        val isLearned = sm2Rating != SM2Algorithm.Rating.AGAIN
        viewModelScope.launch {
            updateVocabularyLearnedStatusUseCase(vocabId, isLearned, result.nextReviewMs, rating.lowercase())
        }

        // Mark as reviewed (for visual indicator) but DO NOT remove from list
        _reviewedIds.value = _reviewedIds.value + vocabId

        // Check if all reviewed
        val allReviewed = _uiState.value.vocabularies.all { it.id in (_reviewedIds.value + vocabId) }
        if (allReviewed) {
            soundManager.playCompleteSound()
        }

        // Move to next card
        val list = _uiState.value.vocabularies
        if (list.isNotEmpty()) {
            val nextIndex = (_uiState.value.currentIndex + 1) % list.size
            _uiState.value = _uiState.value.copy(
                currentIndex = nextIndex,
                isFlipped = false
            )
        }
    }

    /**
     * Legacy reviewCard — used by the old flow. Removes word from the list after review.
     * Cũng sử dụng thuật toán SM-2.
     */
    fun reviewCard(vocabId: Int, rating: String) {
        // Ghi nhận ngày học
        app.streakManager.recordStudyDay()

        val prefs = app.getSharedPreferences("engflash_prefs", android.content.Context.MODE_PRIVATE)

        // Đọc thông số SM-2 hiện tại
        val currentEF = prefs.getFloat("ease_factor_$vocabId", SM2Algorithm.DEFAULT_EASE_FACTOR.toFloat()).toDouble()
        val currentReps = prefs.getInt("repetitions_$vocabId", SM2Algorithm.DEFAULT_REPETITIONS)
        val currentInterval = prefs.getInt("interval_$vocabId", SM2Algorithm.DEFAULT_INTERVAL)

        // Tính toán SM-2
        val sm2Rating = SM2Algorithm.Rating.fromString(rating)
        val result = SM2Algorithm.calculate(sm2Rating, currentEF, currentReps, currentInterval)

        // Phát âm thanh phản hồi
        if (sm2Rating == SM2Algorithm.Rating.AGAIN) {
            soundManager.playWrongSound()
        } else {
            soundManager.playCorrectSound()
        }

        // Lưu kết quả SM-2
        prefs.edit()
            .putLong("next_review_$vocabId", result.nextReviewMs)
            .putString("rating_$vocabId", rating.lowercase())
            .putFloat("ease_factor_$vocabId", result.easeFactor.toFloat())
            .putInt("repetitions_$vocabId", result.repetitions)
            .putInt("interval_$vocabId", result.interval)
            .apply()

        // Update database: Again = false, others = true
        val isLearned = sm2Rating != SM2Algorithm.Rating.AGAIN
        viewModelScope.launch {
            updateVocabularyLearnedStatusUseCase(vocabId, isLearned, result.nextReviewMs, rating.lowercase())
        }

        // To make it feel instantaneous, remove it from the local state list immediately
        val currentList = _uiState.value.vocabularies.toMutableList()
        val currentIndex = _uiState.value.currentIndex
        val indexToRemove = currentList.indexOfFirst { it.id == vocabId }
        if (indexToRemove != -1) {
            currentList.removeAt(indexToRemove)
            
            if (currentList.isEmpty()) {
                soundManager.playCompleteSound()
            }
            
            val nextIndex = if (currentList.isEmpty()) 0 else currentIndex % currentList.size
            _uiState.value = _uiState.value.copy(
                vocabularies = currentList,
                currentIndex = nextIndex,
                isFlipped = false
            )
        }
    }

    fun markCurrentAsLearned() {
        val list = _uiState.value.vocabularies
        val index = _uiState.value.currentIndex
        if (index in list.indices) {
            val vocab = list[index]
            viewModelScope.launch {
                markVocabularyAsLearnedUseCase(vocab.id)
            }
        }
    }

    fun removeFromFlashcard() {
        val list = _uiState.value.vocabularies
        val index = _uiState.value.currentIndex
        if (index in list.indices) {
            val vocab = list[index]
            
            // Persist the change to DB (set isFavorite = false)
            viewModelScope.launch {
                toggleVocabularyFavoriteUseCase(vocab.id, false)
            }
            
            // Immediately remove the card from the flashcard list in UI
            val currentList = list.toMutableList()
            currentList.removeAt(index)
            val nextIndex = if (currentList.isEmpty()) 0 else index % currentList.size
            _uiState.value = _uiState.value.copy(
                vocabularies = currentList,
                currentIndex = nextIndex,
                isFlipped = false
            )
        }
    }

    fun loadFavorites() {
        loadTopicOrFilter("Favorites")
    }

    fun toggleVocabularyFavorite(id: Int, isFavorite: Boolean) {
        viewModelScope.launch {
            toggleVocabularyFavoriteUseCase(id, isFavorite)
        }
    }

    fun resetTopicProgress(topic: String) {
        viewModelScope.launch {
            val flow = getFlashcardByTopicUseCase(topic)
            val list = flow.first()
            val prefs = app.getSharedPreferences("engflash_prefs", android.content.Context.MODE_PRIVATE)
            val editor = prefs.edit()
            for (vocab in list) {
                editor.remove("ease_factor_${vocab.id}")
                editor.remove("repetitions_${vocab.id}")
                editor.remove("interval_${vocab.id}")
                editor.remove("next_review_${vocab.id}")
                editor.remove("rating_${vocab.id}")
            }
            editor.commit()
            for (vocab in list) {
                updateVocabularyLearnedStatusUseCase(vocab.id, false, 0L, "")
            }
        }
    }

    fun addNewVocabulary(
        word: String,
        meaning: String,
        example: String,
        phonetic: String,
        partOfSpeech: String,
        topic: String,
        imageUrl: String
    ) {
        viewModelScope.launch {
            val newVocab = com.example.engflash.domain.model.Vocabulary(
                id = 0,
                word = word,
                meaning = meaning,
                example = example,
                phonetic = phonetic,
                partOfSpeech = partOfSpeech,
                topic = topic,
                isFavorite = false,
                isLearned = false,
                difficulty = "Medium",
                imageUrl = imageUrl.ifBlank { null }
            )
            addVocabularyUseCase(newVocab)
        }
    }

    fun deleteVocabulary(id: Int) {
        viewModelScope.launch {
            app.deleteVocabularyUseCase(id)
        }
    }

    fun updateVocabulary(
        id: Int,
        word: String,
        meaning: String,
        example: String,
        phonetic: String,
        partOfSpeech: String,
        topic: String,
        imageUrl: String
    ) {
        viewModelScope.launch {
            // Lấy vocab hiện tại để giữ lại các cờ isFavorite, isLearned, v.v.
            val currentVocab = app.getVocabularyByIdUseCase(id) ?: return@launch
            
            val updatedVocab = currentVocab.copy(
                word = word,
                meaning = meaning,
                example = example,
                phonetic = phonetic,
                partOfSpeech = partOfSpeech,
                topic = topic,
                imageUrl = imageUrl.ifBlank { null }
            )
            app.updateVocabularyUseCase(updatedVocab)
        }
    }
}
