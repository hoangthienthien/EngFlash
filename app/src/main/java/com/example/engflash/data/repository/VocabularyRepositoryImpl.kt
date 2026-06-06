package com.example.engflash.data.repository

import android.content.Context
import com.example.engflash.data.local.dao.VocabularyDao
import com.example.engflash.data.local.entity.VocabularyEntity
import com.example.engflash.data.mapper.toDomain
import com.example.engflash.data.mapper.toEntity
import com.example.engflash.domain.model.Vocabulary
import com.example.engflash.domain.repository.VocabularyRepository
import com.example.engflash.util.CloudSyncManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class VocabularyRepositoryImpl(
    private val context: Context,
    private val vocabularyDao: VocabularyDao
) : VocabularyRepository {

    override fun getVocabulariesByTopic(topic: String): Flow<List<Vocabulary>> {
        return vocabularyDao.getByTopic(topic).map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getFavoriteVocabularies(): Flow<List<Vocabulary>> {
        return vocabularyDao.getFavorites().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun markAsLearned(id: Int) {
        vocabularyDao.markAsLearned(id)
        syncVocabId(id)
    }

    override suspend fun toggleFavorite(id: Int, isFavorite: Boolean) {
        vocabularyDao.toggleFavorite(id, isFavorite)
        syncVocabId(id)
    }

    override suspend fun updateLearnedStatus(id: Int, isLearned: Boolean, nextReview: Long, rating: String) {
        vocabularyDao.updateLearnedStatus(id, isLearned)
        
        // Save nextReview & rating to SharedPreferences here synchronously
        val prefs = context.getSharedPreferences("engflash_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putLong("next_review_$id", nextReview)
            .putString("rating_$id", rating)
            .apply()
        
        val vocab = vocabularyDao.getById(id)
        if (vocab != null) {
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            if (uid != null) {
                CloudSyncManager.pushVocabProgress(uid, vocab, nextReview, rating)
            }
        }
    }

    override suspend fun addVocabulary(vocabulary: Vocabulary) {
        val entity = vocabulary.toEntity()
        val generatedId = vocabularyDao.insert(entity)
        syncVocabEntity(entity.copy(id = generatedId.toInt()))
    }

    override suspend fun deleteVocabulary(id: Int) {
        val vocab = vocabularyDao.getById(id)
        vocabularyDao.deleteById(id)
        if (vocab != null) {
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            if (uid != null) {
                CloudSyncManager.deleteVocabProgress(uid, vocab.word)
            }
        }
    }

    override suspend fun updateVocabulary(vocabulary: Vocabulary) {
        val entity = vocabulary.toEntity()
        vocabularyDao.update(entity)
        syncVocabEntity(entity)
    }

    override suspend fun getVocabularyById(id: Int): Vocabulary? {
        return vocabularyDao.getById(id)?.toDomain()
    }

    override fun searchVocabularies(query: String): Flow<List<Vocabulary>> {
        return vocabularyDao.search(query).map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getAllTopics(): Flow<List<String>> {
        return vocabularyDao.getAllTopics()
    }

    // ─── Flashcard (Practice) ───
    override fun getFlashcardByTopic(topic: String): Flow<List<Vocabulary>> {
        return vocabularyDao.getFlashcardByTopic(topic).map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getFlashcardTopics(): Flow<List<String>> {
        return vocabularyDao.getFlashcardTopics()
    }

    override fun getFlashcardCountByTopic(topic: String): Flow<Int> {
        return vocabularyDao.getFlashcardCountByTopic(topic)
    }

    override fun getAllFlashcards(): Flow<List<Vocabulary>> {
        return vocabularyDao.getAllFlashcards().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getAllList(): List<Vocabulary> {
        return vocabularyDao.getAllList().map { it.toDomain() }
    }

    override suspend fun addVocabularyList(list: List<Vocabulary>) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        val prefs = context.getSharedPreferences("engflash_prefs", Context.MODE_PRIVATE)
        for (vocab in list) {
            val existing = vocabularyDao.getByWordIgnoreCase(vocab.word.trim())
            if (existing != null) {
                continue
            }
            val entity = vocab.toEntity()
            val newId = vocabularyDao.insert(entity).toInt()
            if (uid != null) {
                val updatedEntity = entity.copy(id = newId)
                val nextReview = prefs.getLong("next_review_$newId", 0L)
                val rating = prefs.getString("rating_$newId", "yếu") ?: "yếu"
                CloudSyncManager.pushVocabProgress(uid, updatedEntity, nextReview, rating)
            }
        }
    }

    override suspend fun deleteDuplicateVocabularies(): Int {
        val allVocabs = vocabularyDao.getAllList()
        val seenWords = mutableSetOf<String>()
        var deletedCount = 0
        val prefs = context.getSharedPreferences("engflash_prefs", Context.MODE_PRIVATE)
        val editor = prefs.edit()

        for (vocab in allVocabs) {
            val normalizedWord = vocab.word.trim().lowercase()
            if (seenWords.contains(normalizedWord)) {
                vocabularyDao.deleteById(vocab.id)
                editor.remove("next_review_${vocab.id}")
                editor.remove("rating_${vocab.id}")
                deletedCount++
            } else {
                seenWords.add(normalizedWord)
            }
        }
        editor.apply()
        return deletedCount
    }

    // --- Sync Helpers ---
    private suspend fun syncVocabId(id: Int) {
        val vocab = vocabularyDao.getById(id)
        if (vocab != null) {
            syncVocabEntity(vocab)
        }
    }

    private fun syncVocabEntity(entity: VocabularyEntity) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            val prefs = context.getSharedPreferences("engflash_prefs", Context.MODE_PRIVATE)
            val nextReview = prefs.getLong("next_review_${entity.id}", 0L)
            val rating = prefs.getString("rating_${entity.id}", "yếu") ?: "yếu"
            CloudSyncManager.pushVocabProgress(uid, entity, nextReview, rating)
        }
    }
}
