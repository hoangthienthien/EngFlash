package com.example.engflash

import android.app.Application
import com.example.engflash.data.local.database.AppDatabase
import com.example.engflash.data.local.database.DataSeeder
import com.example.engflash.data.local.database.DatabaseProvider
import com.example.engflash.data.remote.auth.AuthRepositoryImpl
import com.example.engflash.data.repository.GrammarRepositoryImpl
import com.example.engflash.data.repository.TopicRepositoryImpl
import com.example.engflash.domain.repository.AuthRepository
import com.example.engflash.domain.repository.GrammarRepository
import com.example.engflash.domain.repository.TopicRepository
import com.example.engflash.domain.usecase.auth.GetCurrentUserUseCase
import com.example.engflash.domain.usecase.auth.IsLoggedInUseCase
import com.example.engflash.domain.usecase.auth.LoginUseCase
import com.example.engflash.domain.usecase.auth.LogoutUseCase
import com.example.engflash.domain.usecase.auth.RegisterUseCase
import com.example.engflash.domain.usecase.auth.SendPasswordResetEmailUseCase
import com.example.engflash.domain.usecase.grammar.GetGrammarByIdUseCase
import com.example.engflash.domain.usecase.grammar.GetGrammarByTopicUseCase
import com.example.engflash.domain.usecase.topic.GetAllTopicsUseCase
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

    val logoutUseCase: LogoutUseCase by lazy {
        LogoutUseCase(authRepository)
    }

    val getCurrentUserUseCase: GetCurrentUserUseCase by lazy {
        GetCurrentUserUseCase(authRepository)
    }

    val isLoggedInUseCase: IsLoggedInUseCase by lazy {
        IsLoggedInUseCase(authRepository)
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

    override fun onCreate() {
        super.onCreate()
        // Seed dữ liệu mẫu vào Room DB (chỉ chạy lần đầu)
        CoroutineScope(Dispatchers.IO).launch {
            DataSeeder(this@EngFlashApplication, database).seedIfNeeded()
        }
    }
}
