package com.example.engflash.ui.vocabulary

import com.example.engflash.domain.model.Vocabulary

data class FlashcardUiState(
    val vocabularies: List<Vocabulary> = emptyList(),
    val currentIndex: Int = 0,
    val isFlipped: Boolean = false,
    val isLoading: Boolean = false,
    val selectedFilter: String = "All"
)
