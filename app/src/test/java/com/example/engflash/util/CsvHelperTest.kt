package com.example.engflash.util

import com.example.engflash.domain.model.Vocabulary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CsvHelperTest {

    @Test
    fun testGenerateAndParseCsv() {
        val vocabList = listOf(
            Vocabulary(
                id = 1,
                word = "Apple",
                phonetic = "/ˈæp.əl/",
                meaning = "Quả táo",
                example = "An apple a day keeps the doctor away.",
                partOfSpeech = "NOUN",
                topic = "General",
                isFavorite = true,
                isLearned = false,
                difficulty = "BASIC"
            ),
            Vocabulary(
                id = 2,
                word = "Run",
                phonetic = "/rʌn/",
                meaning = "Chạy",
                example = "He can run fast.",
                partOfSpeech = "VERB",
                topic = "Activity",
                isFavorite = false,
                isLearned = true,
                difficulty = "INTERMEDIATE"
            )
        )

        // Generate CSV string
        val csvString = CsvHelper.generateCsv(vocabList)

        // Verify CSV header and content
        assertTrue(csvString.contains("Word,Phonetic,Meaning,Example,PartOfSpeech,Topic,IsFavorite,IsLearned,Difficulty"))
        assertTrue(csvString.contains("\"Apple\",\"/ˈæp.əl/\",\"Quả táo\",\"An apple a day keeps the doctor away.\",\"NOUN\",\"General\",true,false,\"BASIC\""))
        assertTrue(csvString.contains("\"Run\",\"/rʌn/\",\"Chạy\",\"He can run fast.\",\"VERB\",\"Activity\",false,true,\"INTERMEDIATE\""))

        // Parse CSV string back to objects
        val parsedList = CsvHelper.parseCsv(csvString)

        // Verify size and values
        assertEquals(2, parsedList.size)
        
        val parsedApple = parsedList[0]
        assertEquals("Apple", parsedApple.word)
        assertEquals("/ˈæp.əl/", parsedApple.phonetic)
        assertEquals("Quả táo", parsedApple.meaning)
        assertEquals("An apple a day keeps the doctor away.", parsedApple.example)
        assertEquals("NOUN", parsedApple.partOfSpeech)
        assertEquals("General", parsedApple.topic)
        assertTrue(parsedApple.isFavorite)
        assertEquals(false, parsedApple.isLearned)
        assertEquals("BASIC", parsedApple.difficulty)

        val parsedRun = parsedList[1]
        assertEquals("Run", parsedRun.word)
        assertEquals("/rʌn/", parsedRun.phonetic)
        assertEquals("Chạy", parsedRun.meaning)
        assertEquals("He can run fast.", parsedRun.example)
        assertEquals("VERB", parsedRun.partOfSpeech)
        assertEquals("Activity", parsedRun.topic)
        assertEquals(false, parsedRun.isFavorite)
        assertTrue(parsedRun.isLearned)
        assertEquals("INTERMEDIATE", parsedRun.difficulty)
    }

    @Test
    fun testParseCsvWithEscapedQuotesAndCommas() {
        val csvContent = "Word,Phonetic,Meaning,Example,PartOfSpeech,Topic,IsFavorite,IsLearned,Difficulty\n" +
                "\"Test \"\"quotes\"\"\",\"\",\"Meaning, with comma\",\"Example sentence\",NOUN,General,true,false,ADVANCED"

        val parsedList = CsvHelper.parseCsv(csvContent)
        assertEquals(1, parsedList.size)

        val item = parsedList[0]
        assertEquals("Test \"quotes\"", item.word)
        assertEquals("", item.phonetic)
        assertEquals("Meaning, with comma", item.meaning)
        assertEquals("Example sentence", item.example)
        assertEquals("NOUN", item.partOfSpeech)
        assertEquals("General", item.topic)
        assertTrue(item.isFavorite)
        assertEquals(false, item.isLearned)
        assertEquals("ADVANCED", item.difficulty)
    }
}
