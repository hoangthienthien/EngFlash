package com.example.engflash.util

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log

class SoundManager(private val context: Context) {
    private var soundPool: SoundPool? = null

    // IDs của âm thanh sau khi load vào SoundPool
    private var correctSoundId: Int = 0
    private var wrongSoundId: Int = 0
    private var achievementSoundId: Int = 0
    private var completeSoundId: Int = 0

    // Cờ bật/tắt âm thanh (lưu trong SharedPreferences)
    private val prefs = context.getSharedPreferences("engflash_prefs", Context.MODE_PRIVATE)
    var isSoundEnabled: Boolean
        get() = prefs.getBoolean("is_sound_enabled", true)
        set(value) = prefs.edit().putBoolean("is_sound_enabled", value).apply()

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()

        loadSounds()
    }

    private fun loadSounds() {
        // Lưu ý: User cần thêm các file âm thanh (.ogg hoặc .mp3) vào res/raw/
        // Ví dụ: res/raw/correct.ogg
        try {
            val correctResId = context.resources.getIdentifier("correct", "raw", context.packageName)
            val wrongResId = context.resources.getIdentifier("wrong", "raw", context.packageName)
            val achieveResId = context.resources.getIdentifier("achievement", "raw", context.packageName)
            val completeResId = context.resources.getIdentifier("complete", "raw", context.packageName)

            if (correctResId != 0) correctSoundId = soundPool?.load(context, correctResId, 1) ?: 0
            if (wrongResId != 0) wrongSoundId = soundPool?.load(context, wrongResId, 1) ?: 0
            if (achieveResId != 0) achievementSoundId = soundPool?.load(context, achieveResId, 1) ?: 0
            if (completeResId != 0) completeSoundId = soundPool?.load(context, completeResId, 1) ?: 0
        } catch (e: Exception) {
            Log.e("SoundManager", "Lỗi khi load âm thanh: ${e.message}")
        }
    }

    fun playCorrectSound() {
        if (!isSoundEnabled || correctSoundId == 0) return
        soundPool?.play(correctSoundId, 1f, 1f, 1, 0, 1f)
    }

    fun playWrongSound() {
        if (!isSoundEnabled || wrongSoundId == 0) return
        soundPool?.play(wrongSoundId, 1f, 1f, 1, 0, 1f)
    }

    fun playAchievementSound() {
        if (!isSoundEnabled || achievementSoundId == 0) return
        soundPool?.play(achievementSoundId, 1f, 1f, 1, 0, 1f)
    }

    fun playCompleteSound() {
        if (!isSoundEnabled || completeSoundId == 0) return
        soundPool?.play(completeSoundId, 1f, 1f, 1, 0, 1f)
    }

    fun release() {
        soundPool?.release()
        soundPool = null
    }
}
