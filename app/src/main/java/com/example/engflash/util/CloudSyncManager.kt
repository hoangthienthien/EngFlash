package com.example.engflash.util

import android.content.Context
import com.example.engflash.data.local.database.AppDatabase
import com.example.engflash.data.local.entity.VocabularyEntity
import com.example.engflash.EngFlashApplication
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * CloudSyncManager handles background synchronization of user statistics,
 * quiz progress, and vocabulary status (learned/favorites/custom words)
 * with Firebase Firestore.
 */
object CloudSyncManager {

    /**
     * Pushes current streak and quiz stats to Firestore.
     */
    fun pushStats(context: Context, uid: String) {
        val streakPrefs = context.getSharedPreferences("engflash_streak", Context.MODE_PRIVATE)
        val generalPrefs = context.getSharedPreferences("engflash_prefs", Context.MODE_PRIVATE)

        val currentStreak = streakPrefs.getInt("current_streak", 0)
        val longestStreak = streakPrefs.getInt("longest_streak", 0)
        val lastStudyDate = streakPrefs.getString("last_study_date", "") ?: ""
        val quizzesCompleted = generalPrefs.getInt("quizzes_completed", 0)

        // Parse individual quiz scores dynamically from SharedPreferences
        val grammarScores = mutableMapOf<String, Int>()
        val grammarTotals = mutableMapOf<String, Int>()

        for ((key, value) in generalPrefs.all) {
            if (value is Int) {
                if (key.startsWith("grammar_score_")) {
                    val ruleId = key.substringAfter("grammar_score_")
                    grammarScores[ruleId] = value
                } else if (key.startsWith("grammar_total_")) {
                    val ruleId = key.substringAfter("grammar_total_")
                    grammarTotals[ruleId] = value
                }
            }
        }

        val data = hashMapOf(
            "currentStreak" to currentStreak,
            "longestStreak" to longestStreak,
            "lastStudyDate" to lastStudyDate,
            "quizzesCompleted" to quizzesCompleted,
            "grammarScores" to grammarScores,
            "grammarTotals" to grammarTotals,
            "updatedAt" to System.currentTimeMillis()
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(uid)
                    .collection("progress")
                    .document("stats")
                    .set(data)
                    .await()
                android.util.Log.d("CloudSyncManager", "Successfully pushed stats to cloud for user $uid")
            } catch (e: Exception) {
                android.util.Log.e("CloudSyncManager", "Failed to push stats to cloud", e)
            }
        }
    }

    /**
     * Pushes a specific vocabulary item's progress (favorites, learned status, next review date) to Firestore.
     */
    fun pushVocabProgress(uid: String, vocab: VocabularyEntity, nextReview: Long, rating: String) {
        val data = hashMapOf(
            "word" to vocab.word,
            "meaning" to vocab.meaning,
            "example" to vocab.example,
            "phonetic" to vocab.phonetic,
            "topic" to vocab.topic,
            "isFavorite" to vocab.isFavorite,
            "isLearned" to vocab.isLearned,
            "partOfSpeech" to vocab.partOfSpeech,
            "difficulty" to vocab.difficulty,
            "imageUrl" to vocab.imageUrl,
            "nextReview" to nextReview,
            "rating" to rating,
            "updatedAt" to System.currentTimeMillis()
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(uid)
                    .collection("vocab_progress")
                    .document(vocab.word)
                    .set(data)
                    .await()
                android.util.Log.d("CloudSyncManager", "Pushed vocab progress for: ${vocab.word} with rating: $rating")
            } catch (e: Exception) {
                android.util.Log.e("CloudSyncManager", "Failed to push vocab progress", e)
            }
        }
    }

    /**
     * Deletes a vocabulary item from cloud database (used when deleting custom words).
     */
    fun deleteVocabProgress(uid: String, word: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(uid)
                    .collection("vocab_progress")
                    .document(word)
                    .delete()
                    .await()
                android.util.Log.d("CloudSyncManager", "Deleted vocab progress for: $word")
            } catch (e: Exception) {
                android.util.Log.e("CloudSyncManager", "Failed to delete vocab progress", e)
            }
        }
    }

    /**
     * Pulls stats and vocabulary progress from the cloud, merging them into local storage.
     */
    fun syncAllProgress(context: Context, uid: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = (context.applicationContext as EngFlashApplication).database
                val firestore = FirebaseFirestore.getInstance()

                android.util.Log.d("CloudSyncManager", "Starting cloud pull sync for user $uid")

                // 1. Pull User Stats (Streak and Quiz scores)
                val statsDoc = firestore.collection("users")
                    .document(uid)
                    .collection("progress")
                    .document("stats")
                    .get()
                    .await()

                if (statsDoc.exists()) {
                    val currentStreak = statsDoc.getLong("currentStreak")?.toInt() ?: 0
                    val longestStreak = statsDoc.getLong("longestStreak")?.toInt() ?: 0
                    val lastStudyDate = statsDoc.getString("lastStudyDate") ?: ""
                    val quizzesCompleted = statsDoc.getLong("quizzesCompleted")?.toInt() ?: 0

                    val streakPrefs = context.getSharedPreferences("engflash_streak", Context.MODE_PRIVATE)
                    streakPrefs.edit()
                        .putInt("current_streak", currentStreak)
                        .putInt("longest_streak", longestStreak)
                        .putString("last_study_date", lastStudyDate)
                        .apply()

                    val generalPrefs = context.getSharedPreferences("engflash_prefs", Context.MODE_PRIVATE)
                    val editor = generalPrefs.edit()
                    editor.putInt("quizzes_completed", quizzesCompleted)

                    // Pull grammar scores map
                    @Suppress("UNCHECKED_CAST")
                    val grammarScores = statsDoc.get("grammarScores") as? Map<String, Long>
                    @Suppress("UNCHECKED_CAST")
                    val grammarTotals = statsDoc.get("grammarTotals") as? Map<String, Long>

                    grammarScores?.forEach { (ruleId, score) ->
                        editor.putInt("grammar_score_$ruleId", score.toInt())
                    }
                    grammarTotals?.forEach { (ruleId, total) ->
                        editor.putInt("grammar_total_$ruleId", total.toInt())
                    }
                    editor.apply()

                    // Update StreakManager flows in application instance
                    (context.applicationContext as EngFlashApplication).streakManager.refresh()
                    android.util.Log.d("CloudSyncManager", "Local stats updated from cloud.")
                }

                // 2. Pull Vocabulary Progress
                val vocabProgressQuery = firestore.collection("users")
                    .document(uid)
                    .collection("vocab_progress")
                    .get()
                    .await()

                val generalPrefs = context.getSharedPreferences("engflash_prefs", Context.MODE_PRIVATE)
                val editor = generalPrefs.edit()

                for (doc in vocabProgressQuery.documents) {
                    val word = doc.getString("word") ?: continue
                    val meaning = doc.getString("meaning") ?: ""
                    val example = doc.getString("example") ?: ""
                    val phonetic = doc.getString("phonetic") ?: ""
                    val topic = doc.getString("topic") ?: ""
                    val isFavorite = doc.getBoolean("isFavorite") ?: false
                    val isLearned = doc.getBoolean("isLearned") ?: false
                    val partOfSpeech = doc.getString("partOfSpeech") ?: "NOUN"
                    val difficulty = doc.getString("difficulty") ?: "ADVANCED"
                    val imageUrl = doc.getString("imageUrl")
                    val nextReview = doc.getLong("nextReview") ?: 0L
                    val rating = doc.getString("rating") ?: "yếu"

                    // Check if word exists in local DB
                    val existing = db.vocabularyDao().getByWord(word)
                    if (existing != null) {
                        // Word exists locally (either seeded or already added).
                        // Update its status with cloud values
                        val updated = existing.copy(
                            isFavorite = isFavorite,
                            isLearned = isLearned
                        )
                        db.vocabularyDao().update(updated)
                        editor.putLong("next_review_${existing.id}", nextReview)
                        editor.putString("rating_${existing.id}", rating)
                    } else {
                        // Word does not exist locally (it's a custom-added word from cloud)
                        val customVocab = VocabularyEntity(
                            word = word,
                            meaning = meaning,
                            example = example,
                            phonetic = phonetic,
                            topic = topic,
                            isFavorite = isFavorite,
                            isLearned = isLearned,
                            partOfSpeech = partOfSpeech,
                            difficulty = difficulty,
                            imageUrl = imageUrl
                        )
                        val newId = db.vocabularyDao().insert(customVocab)
                        editor.putLong("next_review_$newId", nextReview)
                        editor.putString("rating_$newId", rating)
                    }
                }
                editor.apply()
                android.util.Log.d("CloudSyncManager", "Vocabulary progress synced successfully from cloud.")

            } catch (e: Exception) {
                android.util.Log.e("CloudSyncManager", "Error synchronizing progress from cloud", e)
            }
        }
    }
}
