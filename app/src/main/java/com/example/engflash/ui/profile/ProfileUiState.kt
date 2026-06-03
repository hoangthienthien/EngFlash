package com.example.engflash.ui.profile

import com.example.engflash.domain.model.UserProfile

data class ProfileUiState(
    val profile: UserProfile? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null,
    val learnedCount: Int = 0,
    val totalCount: Int = 0,
    val favoriteCount: Int = 0
)
