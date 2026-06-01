package com.example.engflash.data.mapper

import com.example.engflash.data.local.entity.VocabularyEntity
import com.example.engflash.domain.model.Vocabulary

fun VocabularyEntity.toDomain(): Vocabulary {
    return Vocabulary(
        id = id,
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
}

fun Vocabulary.toEntity(): VocabularyEntity {
    return VocabularyEntity(
        id = id,
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
}
