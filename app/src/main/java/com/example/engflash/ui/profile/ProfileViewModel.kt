package com.example.engflash.ui.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.engflash.EngFlashApplication
import com.example.engflash.domain.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as EngFlashApplication

    private val getUserProfileUseCase  = app.getUserProfileUseCase
    private val updateUserProfileUseCase = app.updateUserProfileUseCase

    // ─── Achievement & Theme support ─────────────────────────────
    private val achievementManager = com.example.engflash.util.AchievementManager()
    val streakManager              = app.streakManager
    val themeManager               = app.themeManager
    val soundManager               = app.soundManager

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
        loadStatistics()
    }

    private fun loadStatistics() {
        viewModelScope.launch {
            app.database.vocabularyDao().getLearnedCount().collect { count ->
                _uiState.value = _uiState.value.copy(learnedCount = count)
            }
        }
        viewModelScope.launch {
            app.database.vocabularyDao().getTotalCount().collect { count ->
                _uiState.value = _uiState.value.copy(totalCount = count)
            }
        }
        viewModelScope.launch {
            app.database.vocabularyDao().getFavorites().collect { list ->
                _uiState.value = _uiState.value.copy(favoriteCount = list.size)
            }
        }
    }

    fun loadProfile() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Chưa đăng nhập"
            )
            return
        }

        val uid = currentUser.uid
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            try {
                val profile = getUserProfileUseCase(uid)
                if (profile != null) {
                    _uiState.value = _uiState.value.copy(
                        profile = profile,
                        isLoading = false
                    )
                } else {
                    // Create a default profile based on Firebase user details if not exists
                    val defaultProfile = UserProfile(
                        uid = uid,
                        displayName = currentUser.displayName ?: "Người dùng EngFlash",
                        email = currentUser.email ?: "",
                        avatarUrl = "",
                        bio = "Học tiếng Anh mỗi ngày cùng EngFlash!"
                    )
                    // Save default profile to local Room
                    updateUserProfileUseCase(defaultProfile)
                    _uiState.value = _uiState.value.copy(
                        profile = defaultProfile,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Lỗi khi tải thông tin cá nhân"
                )
            }
        }
    }

    fun updateName(name: String) {
        val currentProfile = _uiState.value.profile ?: return
        _uiState.value = _uiState.value.copy(
            profile = currentProfile.copy(displayName = name),
            saveSuccess = false
        )
    }

    fun updateBio(bio: String) {
        val currentProfile = _uiState.value.profile ?: return
        _uiState.value = _uiState.value.copy(
            profile = currentProfile.copy(bio = bio),
            saveSuccess = false
        )
    }

    fun updateAvatarUrl(url: String) {
        val currentProfile = _uiState.value.profile ?: return
        _uiState.value = _uiState.value.copy(
            profile = currentProfile.copy(avatarUrl = url),
            saveSuccess = false
        )
    }

    fun saveProfile() {
        val currentProfile = _uiState.value.profile ?: return
        _uiState.value = _uiState.value.copy(isSaving = true, saveSuccess = false, errorMessage = null)

        viewModelScope.launch {
            try {
                updateUserProfileUseCase(currentProfile)

                // Sync with Firebase User profile too (displayName)
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser != null) {
                    val profileUpdates = userProfileChangeRequest {
                        displayName = currentProfile.displayName
                    }
                    currentUser.updateProfile(profileUpdates)
                }

                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    saveSuccess = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = e.message ?: "Lỗi khi lưu thông tin"
                )
            }
        }
    }

    /**
     * Trả về danh sách thành tựu đã được đánh giá dựa trên dữ liệu thực của user.
     * Đọc quizzesCompleted từ SharedPreferences.
     */
    fun getAchievements(currentStreak: Int): List<com.example.engflash.domain.model.Achievement> {
        val state = _uiState.value
        val prefs = app.getSharedPreferences("engflash_prefs", android.content.Context.MODE_PRIVATE)
        val quizzesCompleted = prefs.getInt("quizzes_completed", 0)
        return achievementManager.evaluate(
            learnedCount      = state.learnedCount,
            totalCount        = state.totalCount,
            currentStreak     = currentStreak,
            quizzesCompleted  = quizzesCompleted,
            favoriteCount     = state.favoriteCount
        )
    }
}
