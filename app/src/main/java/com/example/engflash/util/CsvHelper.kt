package com.example.engflash.util

import com.example.engflash.domain.model.Vocabulary

object CsvHelper {

    fun generateCsv(vocabularies: List<Vocabulary>): String {
        val sb = java.lang.StringBuilder()
        sb.append("Word,Phonetic,Meaning,Example,PartOfSpeech,Topic,IsFavorite,IsLearned,Difficulty\n")
        for (v in vocabularies) {
            val word = escapeCsv(v.word)
            val phonetic = escapeCsv(v.phonetic)
            val meaning = escapeCsv(v.meaning)
            val example = escapeCsv(v.example)
            val pos = escapeCsv(v.partOfSpeech)
            val topic = escapeCsv(v.topic)
            val isFav = v.isFavorite
            val isLearned = v.isLearned
            val diff = escapeCsv(v.difficulty)
            sb.append("\"$word\",\"$phonetic\",\"$meaning\",\"$example\",\"$pos\",\"$topic\",$isFav,$isLearned,\"$diff\"\n")
        }
        return sb.toString()
    }

    private fun escapeCsv(text: String): String {
        return text.replace("\"", "\"\"")
    }

    fun parseCsv(csvContent: String): List<Vocabulary> {
        val list = mutableListOf<Vocabulary>()
        val lines = csvContent.lines()
        if (lines.isEmpty()) return list

        for (i in 1 until lines.size) { // skip header
            val line = lines[i].trim()
            if (line.isEmpty()) continue

            val fields = parseCsvLine(line)
            if (fields.size < 3) continue // Word, Phonetic, Meaning are required fields

            val word = fields[0]
            val phonetic = fields.getOrNull(1) ?: ""
            val meaning = fields.getOrNull(2) ?: ""
            val example = fields.getOrNull(3) ?: ""
            val pos = fields.getOrNull(4) ?: "NOUN"
            val topic = fields.getOrNull(5) ?: "General"
            val isFav = fields.getOrNull(6)?.toBoolean() ?: false
            val isLearned = fields.getOrNull(7)?.toBoolean() ?: false
            val diff = fields.getOrNull(8) ?: "ADVANCED"

            // Word, Meaning, and Topic cannot be empty
            if (word.isBlank() || meaning.isBlank() || topic.isBlank()) {
                continue
            }

            list.add(
                Vocabulary(
                    id = 0, // auto-generated
                    word = word.trim(),
                    phonetic = phonetic.trim(),
                    meaning = meaning.trim(),
                    example = example.trim(),
                    partOfSpeech = pos.trim().uppercase(),
                    topic = topic.trim().replaceFirstChar { it.uppercase() },
                    isFavorite = isFav,
                    isLearned = isLearned,
                    difficulty = diff.trim().uppercase()
                )
            )
        }
        return list
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        var curVal = java.lang.StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val ch = line[i]
            if (inQuotes) {
                if (ch == '\"') {
                    if (i + 1 < line.length && line[i + 1] == '\"') {
                        curVal.append('\"')
                        i++
                    } else {
                        inQuotes = false
                    }
                } else {
                    curVal.append(ch)
                }
            } else {
                if (ch == '\"') {
                    inQuotes = true
                } else if (ch == ',') {
                    result.add(curVal.toString().trim())
                    curVal = java.lang.StringBuilder()
                } else {
                    curVal.append(ch)
                }
            }
            i++
        }
        result.add(curVal.toString().trim())
        return result
    }
}
