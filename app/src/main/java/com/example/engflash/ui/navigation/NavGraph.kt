package com.example.engflash.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.engflash.ui.auth.AuthViewModel
import com.example.engflash.ui.auth.ForgotPasswordScreen
import com.example.engflash.ui.auth.LoginScreen
import com.example.engflash.ui.auth.RegisterScreen
import com.example.engflash.ui.grammar.GrammarDetailScreen
import com.example.engflash.ui.grammar.GrammarListScreen
import com.example.engflash.ui.grammar.GrammarTopicListScreen
import com.example.engflash.ui.grammar.GrammarQuizScreen
import com.example.engflash.ui.grammar.GrammarViewModel
import com.example.engflash.ui.home.HomeScreen
import com.example.engflash.ui.home.HomeViewModel
import com.example.engflash.ui.vocabulary.FlashcardScreen
import com.example.engflash.ui.vocabulary.FlashcardViewModel
import com.example.engflash.ui.vocabulary.VocabularyLibraryScreen
import com.example.engflash.ui.vocabulary.VocabularyListScreen
import com.example.engflash.ui.vocabulary.AddWordScreen
import com.example.engflash.ui.profile.ProfileScreen
import com.example.engflash.ui.profile.ProfileViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String
) {
    // Chia sẻ ViewModel giữa Login & Register (cùng 1 scope)
    val authViewModel: AuthViewModel = viewModel()
    val homeViewModel: HomeViewModel = viewModel()
    val grammarViewModel: GrammarViewModel = viewModel()
    val flashcardViewModel: FlashcardViewModel = viewModel()
    val profileViewModel: ProfileViewModel = viewModel()

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
                onNavigateToForgotPassword = {
                    navController.navigate(Routes.FORGOT_PASSWORD)
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

        composable(Routes.FORGOT_PASSWORD) {
            ForgotPasswordScreen(
                viewModel = authViewModel,
                onBackToLogin = {
                    navController.popBackStack()
                }
            )
        }

        // ─── Home ────────────────────────────────────────
        composable(Routes.HOME) {
            HomeScreen(
                navController = navController,
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

        // ─── Vocabulary 3D Flashcard / Library ──────────
        composable(Routes.VOCABULARY_PLACEHOLDER) {
            VocabularyLibraryScreen(
                navController = navController,
                viewModel = flashcardViewModel
            )
        }

        composable(
            route = Routes.VOCABULARY_LIST,
            arguments = listOf(navArgument("topicName") { type = NavType.StringType })
        ) { backStackEntry ->
            val topicName = backStackEntry.arguments?.getString("topicName") ?: ""
            VocabularyListScreen(
                topicName = topicName,
                viewModel = flashcardViewModel,
                navController = navController
            )
        }

        // ─── Flashcard Practice ──────────────────────────
        composable(Routes.FLASHCARD_PLACEHOLDER) {
            FlashcardScreen(
                navController = navController,
                viewModel = flashcardViewModel
            )
        }

        composable(Routes.ADD_WORD) {
            AddWordScreen(
                navController = navController,
                viewModel = flashcardViewModel
            )
        }

        // ─── Profile ─────────────────────────────────────
        composable(Routes.PROFILE) {
            ProfileScreen(
                navController = navController,
                viewModel = profileViewModel,
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // ─── Grammar Topic List ──────────────────────────
        composable(Routes.GRAMMAR_TOPIC_LIST) {
            GrammarTopicListScreen(
                viewModel = grammarViewModel,
                navController = navController,
                onStartQuiz = { grammarRuleId ->
                    navController.navigate(Routes.grammarQuiz(grammarRuleId))
                }
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
