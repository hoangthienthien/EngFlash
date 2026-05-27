package com.example.engflash.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.engflash.ui.auth.AuthViewModel
import com.example.engflash.ui.auth.LoginScreen
import com.example.engflash.ui.auth.RegisterScreen
import com.example.engflash.ui.grammar.GrammarDetailScreen
import com.example.engflash.ui.grammar.GrammarListScreen
import com.example.engflash.ui.grammar.GrammarTopicListScreen
import com.example.engflash.ui.grammar.GrammarQuizScreen
import com.example.engflash.ui.grammar.GrammarViewModel
import com.example.engflash.ui.home.HomeScreen
import com.example.engflash.ui.home.HomeViewModel
import com.example.engflash.ui.home.VocabularyPlaceholderScreen
import com.example.engflash.ui.home.FlashcardPlaceholderScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String
) {
    // Chia sẻ ViewModel giữa Login & Register (cùng 1 scope)
    val authViewModel: AuthViewModel = viewModel()
    val homeViewModel: HomeViewModel = viewModel()
    val grammarViewModel: GrammarViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // ─── Auth ────────────────────────────────────────
        composable(Routes.LOGIN) {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToRegister = {
                    navController.navigate(Routes.REGISTER)
                },
                onLoginSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                viewModel = authViewModel,
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onRegisterSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        // ─── Home ────────────────────────────────────────
        composable(Routes.HOME) {
            HomeScreen(
                viewModel = homeViewModel,
                onNavigateToVocabulary = {
                    navController.navigate(Routes.VOCABULARY_PLACEHOLDER)
                },
                onNavigateToGrammar = {
                    navController.navigate(Routes.GRAMMAR_TOPIC_LIST)
                },
                onNavigateToFlashcard = {
                    navController.navigate(Routes.FLASHCARD_PLACEHOLDER)
                },
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // ─── Vocabulary Placeholder ──────────────────────
        composable(Routes.VOCABULARY_PLACEHOLDER) {
            VocabularyPlaceholderScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // ─── Flashcard Placeholder ───────────────────────
        composable(Routes.FLASHCARD_PLACEHOLDER) {
            FlashcardPlaceholderScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // ─── Grammar Topic List ──────────────────────────
        composable(Routes.GRAMMAR_TOPIC_LIST) {
            GrammarTopicListScreen(
                viewModel = homeViewModel,
                onTopicClick = { topicId ->
                    navController.navigate(Routes.grammarList(topicId))
                },
                onBack = { navController.popBackStack() }
            )
        }

        // ─── Grammar List ────────────────────────────────
        composable(
            route = Routes.GRAMMAR_LIST,
            arguments = listOf(navArgument("topicId") { type = NavType.StringType })
        ) { backStackEntry ->
            val topicId = backStackEntry.arguments?.getString("topicId") ?: ""
            GrammarListScreen(
                topicId = topicId,
                viewModel = grammarViewModel,
                onGrammarClick = { grammarId ->
                    navController.navigate(Routes.grammarDetail(grammarId))
                },
                onBack = { navController.popBackStack() }
            )
        }

        // ─── Grammar Detail ──────────────────────────────
        composable(
            route = Routes.GRAMMAR_DETAIL,
            arguments = listOf(navArgument("grammarId") { type = NavType.StringType })
        ) { backStackEntry ->
            val grammarId = backStackEntry.arguments?.getString("grammarId") ?: ""
            GrammarDetailScreen(
                grammarId = grammarId,
                viewModel = grammarViewModel,
                onStartQuiz = { grammarRuleId ->
                    navController.navigate(Routes.grammarQuiz(grammarRuleId))
                },
                onBack = { navController.popBackStack() }
            )
        }

        // ─── Grammar Quiz ────────────────────────────────
        composable(
            route = Routes.GRAMMAR_QUIZ,
            arguments = listOf(navArgument("grammarRuleId") { type = NavType.StringType })
        ) { backStackEntry ->
            val grammarRuleId = backStackEntry.arguments?.getString("grammarRuleId") ?: ""
            GrammarQuizScreen(
                grammarRuleId = grammarRuleId,
                viewModel = grammarViewModel,
                onFinish = { navController.popBackStack() }
            )
        }
    }
}
