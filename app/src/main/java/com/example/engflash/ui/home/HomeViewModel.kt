package com.example.engflash.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.engflash.EngFlashApplication
import com.example.engflash.domain.model.Topic
import com.example.engflash.domain.model.User
import com.example.engflash.domain.usecase.auth.GetCurrentUserUseCase
import com.example.engflash.domain.usecase.auth.LogoutUseCase
import com.example.engflash.domain.usecase.topic.GetAllTopicsUseCase
import kotlinx.coroutines.flow.Flow

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as EngFlashApplication

    // ─── UseCases ────────────────────────────────────────
    private val getAllTopicsUseCase: GetAllTopicsUseCase = app.getAllTopicsUseCase
    private val getCurrentUserUseCase: GetCurrentUserUseCase = app.getCurrentUserUseCase
    private val logoutUseCase: LogoutUseCase = app.logoutUseCase

    /** Danh sách chủ đề từ Room DB (reactive Flow). */
    val topics: Flow<List<Topic>> = getAllTopicsUseCase()

    /** Lấy thông tin user hiện tại. */
    fun getCurrentUser(): User? = getCurrentUserUseCase()

    /** Đăng xuất. */
    fun logout() {
        logoutUseCase()
    }
}
