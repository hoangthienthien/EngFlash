package com.example.engflash.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.engflash.data.local.dao.GrammarDao
import com.example.engflash.data.local.dao.TopicDao
import com.example.engflash.data.local.entity.GrammarEntity
import com.example.engflash.data.local.entity.TopicEntity

@Database(
    entities = [TopicEntity::class, GrammarEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun topicDao(): TopicDao
    abstract fun grammarDao(): GrammarDao
}
