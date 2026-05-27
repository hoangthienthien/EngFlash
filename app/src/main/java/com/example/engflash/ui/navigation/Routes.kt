package com.example.engflash.ui.navigation

/**
 * Định nghĩa tất cả route (đường dẫn) cho Navigation.
 */
object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val GRAMMAR_TOPIC_LIST = "grammar_topic_list"
    const val GRAMMAR_LIST = "grammar_list/{topicId}"
    const val GRAMMAR_DETAIL = "grammar_detail/{grammarId}"
    const val GRAMMAR_QUIZ = "grammar_quiz/{grammarRuleId}"
    const val VOCABULARY_PLACEHOLDER = "vocabulary_placeholder"
    const val FLASHCARD_PLACEHOLDER = "flashcard_placeholder"

    // Helper functions tạo route với argument
    fun grammarList(topicId: String) = "grammar_list/$topicId"
    fun grammarDetail(grammarId: String) = "grammar_detail/$grammarId"
    fun grammarQuiz(grammarRuleId: String) = "grammar_quiz/$grammarRuleId"
}
