package com.example.engflash.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.engflash.data.local.dao.GrammarDao
import com.example.engflash.data.local.dao.TopicDao
import com.example.engflash.data.local.dao.VocabularyDao
import com.example.engflash.data.local.dao.UserProfileDao
import com.example.engflash.data.local.entity.GrammarEntity
import com.example.engflash.data.local.entity.TopicEntity
import com.example.engflash.data.local.entity.VocabularyEntity
import com.example.engflash.data.local.entity.UserProfileEntity

@Database(
    entities = [TopicEntity::class, GrammarEntity::class, VocabularyEntity::class, UserProfileEntity::class],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun topicDao(): TopicDao
    abstract fun grammarDao(): GrammarDao
    abstract fun vocabularyDao(): VocabularyDao
    abstract fun userProfileDao(): UserProfileDao
}
