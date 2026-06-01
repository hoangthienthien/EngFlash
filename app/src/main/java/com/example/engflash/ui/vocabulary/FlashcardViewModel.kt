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
    private val addVocabularyUseCase = app.addVocabularyUseCase
    private val getUniqueVocabTopicsUseCase = app.getUniqueVocabTopicsUseCase

    val allTopics: StateFlow<List<String>> = getUniqueVocabTopicsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun getTopicWordCount(topic: String): kotlinx.coroutines.flow.Flow<Int> {
        return app.database.vocabularyDao().getCountByTopic(topic)
    }

    private val _uiState = MutableStateFlow(FlashcardUiState())
    val uiState: StateFlow<FlashcardUiState> = _uiState.asStateFlow()

    private var loadJob: kotlinx.coroutines.Job? = null

    fun loadTopicOrFilter(filter: String) {
        loadJob?.cancel()
        _uiState.value = _uiState.value.copy(isLoading = true, selectedFilter = filter)
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

    // Keep compatibility for VocabularyListScreen loading
    fun loadTopic(topic: String) {
        loadTopicOrFilter(topic)
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

    fun reviewCard(vocabId: Int, rating: String) {
        val prefs = app.getSharedPreferences("engflash_prefs", android.content.Context.MODE_PRIVATE)
        val now = System.currentTimeMillis()
        val delayMs = when (rating.lowercase()) {
            "yếu" -> 1000L * 90 // 1.5 minutes
            "được" -> 1000L * 60 * 15 // 15 minutes
            "giỏi" -> 1000L * 60 * 60 * 24 * 4 // 4 days (3-5 days range)
            else -> 0L
        }
        prefs.edit().putLong("next_review_$vocabId", now + delayMs).apply()

        // Update database: yếu = false (unlearned/retry), được/giỏi = true (learned)
        val isLearned = rating.lowercase() == "được" || rating.lowercase() == "giỏi"
        viewModelScope.launch {
            app.database.vocabularyDao().updateLearnedStatus(vocabId, isLearned)
        }

        // To make it feel instantaneous, remove it from the local state list immediately
        val currentList = _uiState.value.vocabularies.toMutableList()
        val currentIndex = _uiState.value.currentIndex
        val indexToRemove = currentList.indexOfFirst { it.id == vocabId }
        if (indexToRemove != -1) {
            currentList.removeAt(indexToRemove)
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
}
