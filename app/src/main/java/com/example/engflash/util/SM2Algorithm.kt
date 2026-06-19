package com.example.engflash.util

/**
 * Triển khai thuật toán Spaced Repetition SM-2 (SuperMemo-2).
 *
 * Thuật toán SM-2 tối ưu hóa khoảng thời gian ôn tập từ vựng dựa trên
 * lịch sử ghi nhớ của người dùng. Mỗi từ vựng có 3 thông số:
 * - Ease Factor (EF): Hệ số dễ, khởi tạo 2.5, tối thiểu 1.3
 * - Repetitions (n): Số lần trả lời đúng liên tiếp
 * - Interval (I): Khoảng cách ngày ôn tập tiếp theo
 */
object SM2Algorithm {

    /** Hệ số dễ mặc định ban đầu */
    const val DEFAULT_EASE_FACTOR = 2.5

    /** Hệ số dễ tối thiểu */
    const val MIN_EASE_FACTOR = 1.3

    /** Số lần lặp mặc định ban đầu */
    const val DEFAULT_REPETITIONS = 0

    /** Khoảng cách ôn tập mặc định ban đầu (ngày) */
    const val DEFAULT_INTERVAL = 0

    /**
     * Các mức đánh giá chất lượng ghi nhớ.
     * Mapping theo chuẩn SM-2: Again=1, Hard=3, Good=4, Easy=5
     */
    enum class Rating(val quality: Int, val label: String, val labelVi: String) {
        AGAIN(1, "Again", "Chưa thuộc"),
        HARD(3, "Hard", "Còn khó"),
        GOOD(4, "Good", "Đã nhớ"),
        EASY(5, "Easy", "Rất dễ");

        companion object {
            fun fromString(value: String): Rating {
                return when (value.lowercase()) {
                    "again" -> AGAIN
                    "hard" -> HARD
                    "good" -> GOOD
                    "easy" -> EASY
                    // Hỗ trợ giá trị cũ để backward-compatible
                    "yếu" -> AGAIN
                    "được" -> GOOD
                    "giỏi" -> EASY
                    else -> GOOD
                }
            }
        }
    }

    /**
     * Kết quả tính toán SM-2 sau khi người dùng đánh giá một thẻ.
     
     */
    data class SM2Result(
        val easeFactor: Double,
        val repetitions: Int,
        val interval: Int,
        val nextReviewMs: Long
    )

    /**
     * Tính toán SM-2 dựa trên đánh giá của người dùng.
     */
    fun calculate(
        rating: Rating,
        currentEaseFactor: Double = DEFAULT_EASE_FACTOR,
        currentRepetitions: Int = DEFAULT_REPETITIONS,
        currentInterval: Int = DEFAULT_INTERVAL
    ): SM2Result {
        val q = rating.quality
        val now = System.currentTimeMillis()

        // Tính Ease Factor mới theo công thức SM-2
        // EF' = EF + (0.1 - (5 - q) * (0.08 + (5 - q) * 0.02))
        val newEaseFactor = maxOf(
            MIN_EASE_FACTOR,
            currentEaseFactor + (0.1 - (5 - q) * (0.08 + (5 - q) * 0.02))
        )

        val newRepetitions: Int
        val newInterval: Int

        if (q < 3) {
            // Người dùng chọn Again → reset
            newRepetitions = 0
            newInterval = 1
        } else {
            // Người dùng trả lời đúng (Hard/Good/Easy)
            newRepetitions = currentRepetitions + 1

            val baseInterval = when (newRepetitions) {
                1 -> 1
                2 -> 6
                else -> Math.round(currentInterval * newEaseFactor).toInt()
            }

            // Áp dụng hệ số điều chỉnh theo mức đánh giá
            newInterval = when (rating) {
                Rating.HARD -> maxOf(1, Math.round(baseInterval * 0.8).toInt())
                Rating.GOOD -> baseInterval
                Rating.EASY -> Math.round(baseInterval * 1.3).toInt()
                else -> baseInterval
            }
        }

        // Tính thời điểm ôn tập tiếp theo
        val delayMs = if (rating == Rating.AGAIN) {
            // Ôn lại ngay sau 1.5 phút trong cùng phiên học
            1000L * 90
        } else {
            newInterval.toLong() * 24 * 60 * 60 * 1000L
        }
        val nextReviewMs = now + delayMs

        return SM2Result(
            easeFactor = newEaseFactor,
            repetitions = newRepetitions,
            interval = newInterval,
            nextReviewMs = nextReviewMs
        )
    }

    /**
     * Hiển thị thời gian ôn tập tiếp theo dưới dạng chuỗi dễ đọc.
     * Ví dụ: "1.5 phút", "1 ngày", "6 ngày", "15 ngày"
     */
    fun formatNextReview(rating: Rating, interval: Int): String {
        return if (rating == Rating.AGAIN) {
            "1.5 phút"
        } else {
            "$interval ngày"
        }
    }

    /**
     * Hiển thị thời gian ôn tập tiếp theo dưới dạng chuỗi dễ đọc từ nextReviewMs.
     */
    fun formatNextReviewFromMs(nextReviewMs: Long): String {
        val now = System.currentTimeMillis()
        val diffMs = nextReviewMs - now
        if (diffMs <= 0) return "Ngay bây giờ"

        val minutes = diffMs / (1000 * 60)
        val hours = diffMs / (1000 * 60 * 60)
        val days = diffMs / (1000 * 60 * 60 * 24)

        return when {
            minutes < 3 -> "1.5 phút"
            minutes < 60 -> "$minutes phút"
            hours < 24 -> "$hours giờ"
            else -> "$days ngày"
        }
    }
}
