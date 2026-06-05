package com.example.engflash.util

import android.content.Context
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Quản lý streak học tập dựa trên SharedPreferences.
 * Mỗi ngày user hoàn thành flashcard/quiz → gọi recordStudyDay().
 */
class StreakManager(private val context: Context) {
    private val prefs = context.getSharedPreferences("engflash_streak", Context.MODE_PRIVATE)
    private val fmt = DateTimeFormatter.ISO_LOCAL_DATE

    private val _currentStreak = MutableStateFlow(calculateCurrentStreak())
    val currentStreak: StateFlow<Int> = _currentStreak.asStateFlow()

    private val _longestStreak = MutableStateFlow(prefs.getInt("longest_streak", 0))
    val longestStreak: StateFlow<Int> = _longestStreak.asStateFlow()

    /** Ghi nhận user đã học hôm nay */
    fun recordStudyDay() {
        val today = LocalDate.now().format(fmt)
        val lastDate = prefs.getString("last_study_date", null)
        val current = prefs.getInt("current_streak", 0)
        val longest = prefs.getInt("longest_streak", 0)

        val newStreak = when {
            lastDate == null -> 1  // Lần đầu
            lastDate == today -> current  // Đã ghi hôm nay rồi
            lastDate == LocalDate.now().minusDays(1).format(fmt) -> current + 1 // Liên tục
            else -> 1  // Bị gián đoạn → reset
        }

        val maxLongest = maxOf(longest, newStreak)

        prefs.edit()
            .putString("last_study_date", today)
            .putInt("current_streak", newStreak)
            .putInt("longest_streak", maxLongest)
            .apply()

        _currentStreak.value = newStreak
        _longestStreak.value = maxLongest

        // Trigger Cloud Sync if user is logged in
        val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            CloudSyncManager.pushStats(context, uid)
        }
    }

    /** Streak hiện tại (đã kiểm tra gián đoạn) */
    private fun calculateCurrentStreak(): Int {
        val lastDate = prefs.getString("last_study_date", null) ?: return 0
        val last = LocalDate.parse(lastDate, fmt)
        val today = LocalDate.now()
        return when {
            last == today || last == today.minusDays(1) -> prefs.getInt("current_streak", 0)
            else -> 0  // Đã quá 1 ngày không học → streak = 0
        }
    }

    /** Refresh flows from updated shared preferences */
    fun refresh() {
        _currentStreak.value = calculateCurrentStreak()
        _longestStreak.value = prefs.getInt("longest_streak", 0)
    }
    
    // For backward compatibility or immediate reads if needed
    fun getCurrentStreak(): Int = currentStreak.value
    fun getLongestStreak(): Int = longestStreak.value
}
