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
import android.content.Context
import android.net.Uri
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as EngFlashApplication

    private val getUserProfileUseCase  = app.getUserProfileUseCase
    private val updateUserProfileUseCase = app.updateUserProfileUseCase
    private val getAllVocabularyListUseCase = app.getAllVocabularyListUseCase
    private val addVocabularyListUseCase = app.addVocabularyListUseCase

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
            app.database.vocabularyDao().getAll().collect { list ->
                val prefs = app.getSharedPreferences("engflash_prefs", android.content.Context.MODE_PRIVATE)
                val now = System.currentTimeMillis()
                var learnedCount = 0
                var favoriteCount = 0
                val totalCount = list.size

                for (vocab in list) {
                    if (vocab.isFavorite) {
                        favoriteCount++
                        
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
                                    "giỏi"
                                } else if (diff > 5 * 60 * 1000L) {
                                    "được"
                                } else {
                                    "yếu"
                                }
                            }
                        }

                        if (level == "giỏi") {
                            learnedCount++
                        }
                    }
                }

                _uiState.value = _uiState.value.copy(
                    learnedCount = learnedCount,
                    totalCount = totalCount,
                    favoriteCount = favoriteCount
                )
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

    fun exportVocabulariesToUri(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val list = getAllVocabularyListUseCase()
                val csvContent = com.example.engflash.util.CsvHelper.generateCsv(list)
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(csvContent.toByteArray())
                }
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Xuất thành công ${list.size} từ vựng!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Lỗi khi xuất file: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun importVocabulariesFromUri(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri) ?: return@launch
                val csvContent = inputStream.bufferedReader().use { it.readText() }
                val list = com.example.engflash.util.CsvHelper.parseCsv(csvContent)
                if (list.isNotEmpty()) {
                    addVocabularyListUseCase(list)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Nhập thành công ${list.size} từ vựng mới!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "File trống hoặc sai định dạng CSV!", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Lỗi khi nhập file: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun cleanUpDuplicates(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val deletedCount = app.deleteDuplicateVocabulariesUseCase()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Đã dọn dẹp xong $deletedCount từ trùng lặp!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Lỗi khi dọn dẹp: ${e.message}", Toast.LENGTH_SHORT).show()
                }
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
