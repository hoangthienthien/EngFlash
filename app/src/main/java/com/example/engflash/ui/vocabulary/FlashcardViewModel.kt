package com.example.engflash.ui.vocabulary

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
                    rating.lowercase() == "giỏi"
                } else {
                    val nextReview = prefs.getLong("next_review_${vocab.id}", 0L)
                    // If next review is more than 2 days in the future, it was likely rated "Giỏi"
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
                _uiState.value = _uiState.value.copy(
                    vocabularies = list,
                    isLoading = false,
                    currentIndex = if (_uiState.value.currentIndex >= list.size) 0 else _uiState.value.currentIndex
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
     * SRS interval is still recorded. The word is marked as "reviewed" visually.
     */
    fun reviewCardPersistent(vocabId: Int, rating: String) {
        // Ghi nhận ngày học
        app.streakManager.recordStudyDay()

        val prefs = app.getSharedPreferences("engflash_prefs", android.content.Context.MODE_PRIVATE)
        val now = System.currentTimeMillis()
        val delayMs = when (rating.lowercase()) {
            "yếu" -> {
                soundManager.playWrongSound()
                1000L * 90 // 1.5 minutes
            }
            "được" -> {
                soundManager.playCorrectSound()
                1000L * 60 * 15 // 15 minutes
            }
            "giỏi" -> {
                soundManager.playCorrectSound()
                1000L * 60 * 60 * 24 * 4 // 4 days
            }
            else -> 0L
        }
        val nextReview = now + delayMs
        prefs.edit()
            .putLong("next_review_$vocabId", nextReview)
            .putString("rating_$vocabId", rating.lowercase())
            .apply()

        // Update database: yếu = false (unlearned/retry), được/giỏi = true (learned)
        val isLearned = rating.lowercase() == "được" || rating.lowercase() == "giỏi"
        viewModelScope.launch {
            updateVocabularyLearnedStatusUseCase(vocabId, isLearned, nextReview, rating.lowercase())
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
     */
    fun reviewCard(vocabId: Int, rating: String) {
        // Ghi nhận ngày học
        app.streakManager.recordStudyDay()

        val prefs = app.getSharedPreferences("engflash_prefs", android.content.Context.MODE_PRIVATE)
        val now = System.currentTimeMillis()
        val delayMs = when (rating.lowercase()) {
            "yếu" -> {
                soundManager.playWrongSound()
                1000L * 90 // 1.5 minutes
            }
            "được" -> {
                soundManager.playCorrectSound()
                1000L * 60 * 15 // 15 minutes
            }
            "giỏi" -> {
                soundManager.playCorrectSound()
                1000L * 60 * 60 * 24 * 4 // 4 days (3-5 days range)
            }
            else -> 0L
        }
        val nextReview = now + delayMs
        prefs.edit()
            .putLong("next_review_$vocabId", nextReview)
            .putString("rating_$vocabId", rating.lowercase())
            .apply()

        // Update database: yếu = false (unlearned/retry), được/giỏi = true (learned)
        val isLearned = rating.lowercase() == "được" || rating.lowercase() == "giỏi"
        viewModelScope.launch {
            updateVocabularyLearnedStatusUseCase(vocabId, isLearned, nextReview, rating.lowercase())
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
