package com.example.engflash.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Stars
import androidx.compose.ui.graphics.Color
import com.example.engflash.domain.model.Achievement

// Icon colors
private val AccentYellow = Color(0xFFFFC107)
private val AccentGreen  = Color(0xFF4CAF50)
private val PurpleLight  = Color(0xFF8B5CF6)

class AchievementManager {

    /**
     * Tạo danh sách thành tựu dựa trên dữ liệu thực của user.
     */
    fun evaluate(
        learnedCount: Int,
        totalCount: Int,
        currentStreak: Int,
        quizzesCompleted: Int,
        favoriteCount: Int
    ): List<Achievement> = listOf(

        // 1. Huyền thoại Từ vựng — thuộc >= 50 từ
        Achievement(
            id          = "vocab_master",
            label       = "Huyền thoại\nTừ vựng",
            description = "Thuộc ít nhất 50 từ vựng",
            icon        = Icons.Default.EmojiEvents,
            tint        = AccentYellow,
            isUnlocked  = learnedCount >= 50,
            progress    = (learnedCount / 50f).coerceAtMost(1f)
        ),

        // 2. Siêu tốc — streak >= 7 ngày
        Achievement(
            id          = "streak_7",
            label       = "Chuỗi học\n7 ngày",
            description = "Duy trì chuỗi học 7 ngày liên tục",
            icon        = Icons.Default.Stars,
            tint        = PurpleLight,
            isUnlocked  = currentStreak >= 7,
            progress    = (currentStreak / 7f).coerceAtMost(1f)
        ),

        // 3. Chuyên gia Ngữ pháp — hoàn thành >= 5 quiz
        Achievement(
            id          = "grammar_expert",
            label       = "Chuyên gia\nNgữ pháp",
            description = "Hoàn thành ít nhất 5 bài trắc nghiệm ngữ pháp",
            icon        = Icons.Default.EmojiEvents,
            tint        = AccentGreen,
            isUnlocked  = quizzesCompleted >= 5,
            progress    = (quizzesCompleted / 5f).coerceAtMost(1f)
        ),

        // 4. Sưu tầm gia — có >= 20 từ yêu thích
        Achievement(
            id          = "collector",
            label       = "Sưu tầm gia\nTừ vựng",
            description = "Lưu ít nhất 20 từ yêu thích",
            icon        = Icons.Default.Favorite,
            tint        = Color(0xFFE91E63),
            isUnlocked  = favoriteCount >= 20,
            progress    = (favoriteCount / 20f).coerceAtMost(1f)
        ),

        // 5. Người học Trung thành — streak >= 30 ngày
        Achievement(
            id          = "streak_30",
            label       = "Người học\nTrung thành",
            description = "Duy trì chuỗi học 30 ngày liên tục",
            icon        = Icons.Default.Stars,
            tint        = Color(0xFFFF6B35),
            isUnlocked  = currentStreak >= 30,
            progress    = (currentStreak / 30f).coerceAtMost(1f)
        ),

        // 6. Hoàn hảo Tuyệt đối — thuộc 100% từ vựng
        Achievement(
            id          = "perfectionist",
            label       = "Hoàn hảo\nTuyệt đối",
            description = "Thuộc 100% từ vựng trong thư viện",
            icon        = Icons.Default.EmojiEvents,
            tint        = Color(0xFF9C27B0),
            isUnlocked  = totalCount > 0 && learnedCount >= totalCount,
            progress    = if (totalCount > 0) (learnedCount.toFloat() / totalCount).coerceAtMost(1f) else 0f
        )
    )
}
