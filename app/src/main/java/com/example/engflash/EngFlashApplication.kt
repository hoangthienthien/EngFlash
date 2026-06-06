package com.example.engflash

import android.app.Application
import com.example.engflash.data.local.database.AppDatabase
import com.example.engflash.data.local.database.DataSeeder
import com.example.engflash.data.local.database.DatabaseProvider
import com.example.engflash.data.remote.auth.AuthRepositoryImpl
import com.example.engflash.data.repository.GrammarRepositoryImpl
import com.example.engflash.data.repository.TopicRepositoryImpl
import com.example.engflash.data.repository.VocabularyRepositoryImpl
import com.example.engflash.data.repository.UserRepositoryImpl
import com.example.engflash.domain.repository.AuthRepository
import com.example.engflash.domain.repository.GrammarRepository
import com.example.engflash.domain.repository.TopicRepository
import com.example.engflash.domain.repository.VocabularyRepository
import com.example.engflash.domain.repository.UserRepository
import com.example.engflash.domain.usecase.AddVocabularyUseCase
import com.example.engflash.domain.usecase.vocabulary.GetAllVocabularyListUseCase
import com.example.engflash.domain.usecase.vocabulary.AddVocabularyListUseCase
import com.example.engflash.domain.usecase.vocabulary.DeleteDuplicateVocabulariesUseCase
import com.example.engflash.domain.usecase.GetUniqueVocabTopicsUseCase
import com.example.engflash.domain.usecase.GetFavoriteVocabulariesUseCase
import com.example.engflash.domain.usecase.GetVocabulariesByTopicUseCase
import com.example.engflash.domain.usecase.MarkVocabularyAsLearnedUseCase
import com.example.engflash.domain.usecase.ToggleVocabularyFavoriteUseCase
import com.example.engflash.domain.usecase.GetUserProfileUseCase
import com.example.engflash.domain.usecase.UpdateUserProfileUseCase
import com.example.engflash.domain.usecase.auth.GetCurrentUserUseCase
import com.example.engflash.domain.usecase.auth.IsLoggedInUseCase
import com.example.engflash.domain.usecase.auth.LoginUseCase
import com.example.engflash.domain.usecase.auth.LogoutUseCase
import com.example.engflash.domain.usecase.auth.RegisterUseCase
import com.example.engflash.domain.usecase.auth.SendOtpEmailUseCase
import com.example.engflash.domain.usecase.auth.SendPasswordResetEmailUseCase
import com.example.engflash.domain.usecase.grammar.GetGrammarByIdUseCase
import com.example.engflash.domain.usecase.grammar.GetGrammarByTopicUseCase
import com.example.engflash.domain.usecase.topic.GetAllTopicsUseCase
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Application class — cung cấp các dependency (thay thế DI framework).
 * Seed dữ liệu từ data.json vào Room DB khi lần đầu mở app.
 *
 * Kiến trúc:
 *   Repositories → chỉ dùng nội bộ để xây dựng UseCase.
 *   UseCases     → được expose ra ngoài để ViewModel sử dụng.
 */
class EngFlashApplication : Application() {

    // ─── Database ────────────────────────────────────────
    val database: AppDatabase by lazy {
        DatabaseProvider.getDatabase(this)
    }

    // ─── Repositories (internal — không expose trực tiếp ra ViewModel) ───
    private val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl()
    }

    private val topicRepository: TopicRepository by lazy {
        TopicRepositoryImpl(database.topicDao())
    }

    private val grammarRepository: GrammarRepository by lazy {
        GrammarRepositoryImpl(database.grammarDao())
    }

    private val vocabularyRepository: VocabularyRepository by lazy {
        VocabularyRepositoryImpl(this, database.vocabularyDao())
    }

    private val userRepository: UserRepository by lazy {
        UserRepositoryImpl(database.userProfileDao(), FirebaseFirestore.getInstance())
    }

    // ─── Auth UseCases ───────────────────────────────────
    val loginUseCase: LoginUseCase by lazy {
        LoginUseCase(authRepository)
    }

    val registerUseCase: RegisterUseCase by lazy {
        RegisterUseCase(authRepository)
    }

    val sendPasswordResetEmailUseCase: SendPasswordResetEmailUseCase by lazy {
        SendPasswordResetEmailUseCase(authRepository)
    }

    val sendOtpEmailUseCase: SendOtpEmailUseCase by lazy {
        SendOtpEmailUseCase(authRepository)
    }

    val logoutUseCase: LogoutUseCase by lazy {
        LogoutUseCase(authRepository)
    }

    val getCurrentUserUseCase: GetCurrentUserUseCase by lazy {
        GetCurrentUserUseCase(authRepository)
    }

    val isLoggedInUseCase: IsLoggedInUseCase by lazy {
        IsLoggedInUseCase(authRepository)
    }

    val changePasswordUseCase: com.example.engflash.domain.usecase.auth.ChangePasswordUseCase by lazy {
        com.example.engflash.domain.usecase.auth.ChangePasswordUseCase(authRepository)
    }

    // ─── Topic UseCases ──────────────────────────────────
    val getAllTopicsUseCase: GetAllTopicsUseCase by lazy {
        GetAllTopicsUseCase(topicRepository)
    }

    // ─── Grammar UseCases ────────────────────────────────
    val getGrammarByTopicUseCase: GetGrammarByTopicUseCase by lazy {
        GetGrammarByTopicUseCase(grammarRepository)
    }

    val getGrammarByIdUseCase: GetGrammarByIdUseCase by lazy {
        GetGrammarByIdUseCase(grammarRepository)
    }

    // ─── Vocabulary UseCases ─────────────────────────────
    val getVocabulariesByTopicUseCase: GetVocabulariesByTopicUseCase by lazy {
        GetVocabulariesByTopicUseCase(vocabularyRepository)
    }

    val getFavoriteVocabulariesUseCase: GetFavoriteVocabulariesUseCase by lazy {
        GetFavoriteVocabulariesUseCase(vocabularyRepository)
    }

    val markVocabularyAsLearnedUseCase: MarkVocabularyAsLearnedUseCase by lazy {
        MarkVocabularyAsLearnedUseCase(vocabularyRepository)
    }

    val toggleVocabularyFavoriteUseCase: ToggleVocabularyFavoriteUseCase by lazy {
        ToggleVocabularyFavoriteUseCase(vocabularyRepository)
    }

    val addVocabularyUseCase: AddVocabularyUseCase by lazy {
        AddVocabularyUseCase(vocabularyRepository)
    }

    val getUniqueVocabTopicsUseCase: GetUniqueVocabTopicsUseCase by lazy {
        GetUniqueVocabTopicsUseCase(vocabularyRepository)
    }

    val deleteVocabularyUseCase: com.example.engflash.domain.usecase.vocabulary.DeleteVocabularyUseCase by lazy {
        com.example.engflash.domain.usecase.vocabulary.DeleteVocabularyUseCase(vocabularyRepository)
    }

    val updateVocabularyUseCase: com.example.engflash.domain.usecase.vocabulary.UpdateVocabularyUseCase by lazy {
        com.example.engflash.domain.usecase.vocabulary.UpdateVocabularyUseCase(vocabularyRepository)
    }

    val updateVocabularyLearnedStatusUseCase: com.example.engflash.domain.usecase.vocabulary.UpdateVocabularyLearnedStatusUseCase by lazy {
        com.example.engflash.domain.usecase.vocabulary.UpdateVocabularyLearnedStatusUseCase(vocabularyRepository)
    }

    val getVocabularyByIdUseCase: com.example.engflash.domain.usecase.vocabulary.GetVocabularyByIdUseCase by lazy {
        com.example.engflash.domain.usecase.vocabulary.GetVocabularyByIdUseCase(vocabularyRepository)
    }

    val searchVocabularyUseCase: com.example.engflash.domain.usecase.vocabulary.SearchVocabularyUseCase by lazy {
        com.example.engflash.domain.usecase.vocabulary.SearchVocabularyUseCase(vocabularyRepository)
    }

    val getAllVocabularyListUseCase: GetAllVocabularyListUseCase by lazy {
        GetAllVocabularyListUseCase(vocabularyRepository)
    }

    val addVocabularyListUseCase: AddVocabularyListUseCase by lazy {
        AddVocabularyListUseCase(vocabularyRepository)
    }

    val deleteDuplicateVocabulariesUseCase: DeleteDuplicateVocabulariesUseCase by lazy {
        DeleteDuplicateVocabulariesUseCase(vocabularyRepository)
    }

    // ─── Flashcard Practice UseCases ─────────────────────
    val getFlashcardByTopicUseCase: com.example.engflash.domain.usecase.vocabulary.GetFlashcardByTopicUseCase by lazy {
        com.example.engflash.domain.usecase.vocabulary.GetFlashcardByTopicUseCase(vocabularyRepository)
    }

    val getFlashcardTopicsUseCase: com.example.engflash.domain.usecase.vocabulary.GetFlashcardTopicsUseCase by lazy {
        com.example.engflash.domain.usecase.vocabulary.GetFlashcardTopicsUseCase(vocabularyRepository)
    }

    val getFlashcardCountByTopicUseCase: com.example.engflash.domain.usecase.vocabulary.GetFlashcardCountByTopicUseCase by lazy {
        com.example.engflash.domain.usecase.vocabulary.GetFlashcardCountByTopicUseCase(vocabularyRepository)
    }

    val searchGrammarUseCase: com.example.engflash.domain.usecase.grammar.SearchGrammarUseCase by lazy {
        com.example.engflash.domain.usecase.grammar.SearchGrammarUseCase(grammarRepository)
    }

    // ─── User Profile UseCases ───────────────────────────
    val getUserProfileUseCase: GetUserProfileUseCase by lazy {
        GetUserProfileUseCase(userRepository)
    }

    val updateUserProfileUseCase: UpdateUserProfileUseCase by lazy {
        UpdateUserProfileUseCase(userRepository)
    }

    val streakManager: com.example.engflash.util.StreakManager by lazy {
        com.example.engflash.util.StreakManager(this)
    }

    val themeManager: com.example.engflash.util.ThemeManager by lazy {
        com.example.engflash.util.ThemeManager(this)
    }

    val soundManager: com.example.engflash.util.SoundManager by lazy {
        com.example.engflash.util.SoundManager(this)
    }

    override fun onCreate() {
        super.onCreate()
        // Seed dữ liệu mẫu vào Room DB (chỉ chạy lần đầu)
        CoroutineScope(Dispatchers.IO).launch {
            DataSeeder(this@EngFlashApplication, database).seedIfNeeded()
        }
    }
}
