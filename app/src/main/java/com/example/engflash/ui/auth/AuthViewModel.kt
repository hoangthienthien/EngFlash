package com.example.engflash.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.engflash.EngFlashApplication
import com.example.engflash.domain.model.User
import com.example.engflash.domain.usecase.auth.GetCurrentUserUseCase
import com.example.engflash.domain.usecase.auth.IsLoggedInUseCase
import com.example.engflash.domain.usecase.auth.LoginUseCase
import com.example.engflash.domain.usecase.auth.LogoutUseCase
import com.example.engflash.domain.usecase.auth.RegisterUseCase
import com.example.engflash.domain.usecase.auth.SendPasswordResetEmailUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Trạng thái xác thực chung cho Login/Register.
 */
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
}

/**
 * Trạng thái riêng cho chức năng Reset Password.
 */
sealed class ResetPasswordState {
    object Idle : ResetPasswordState()
    object Loading : ResetPasswordState()
    object Success : ResetPasswordState()
    data class Error(val message: String) : ResetPasswordState()
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    // ─── UseCases (nhận qua Application thay vì Repository trực tiếp) ─
    private val app = application as EngFlashApplication
    private val loginUseCase: LoginUseCase = app.loginUseCase
    private val registerUseCase: RegisterUseCase = app.registerUseCase
    private val sendPasswordResetEmailUseCase: SendPasswordResetEmailUseCase = app.sendPasswordResetEmailUseCase
    private val logoutUseCase: LogoutUseCase = app.logoutUseCase
    private val getCurrentUserUseCase: GetCurrentUserUseCase = app.getCurrentUserUseCase
    private val isLoggedInUseCase: IsLoggedInUseCase = app.isLoggedInUseCase

    // ─── Login State ─────────────────────────────────────
    private val _loginState = MutableStateFlow<AuthState>(AuthState.Idle)
    val loginState: StateFlow<AuthState> = _loginState.asStateFlow()

    // ─── Register State ──────────────────────────────────
    private val _registerState = MutableStateFlow<AuthState>(AuthState.Idle)
    val registerState: StateFlow<AuthState> = _registerState.asStateFlow()

    // ─── Reset Password State ────────────────────────────
    private val _resetPasswordState = MutableStateFlow<ResetPasswordState>(ResetPasswordState.Idle)
    val resetPasswordState: StateFlow<ResetPasswordState> = _resetPasswordState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = AuthState.Loading
            val result = loginUseCase(email, password)
            _loginState.value = result.fold(
                onSuccess = { AuthState.Success(it) },
                onFailure = { AuthState.Error(mapFirebaseError(it)) }
            )
        }
    }

    fun register(email: String, password: String, confirmPassword: String, displayName: String) {
        viewModelScope.launch {
            _registerState.value = AuthState.Loading
            val result = registerUseCase(email, password, confirmPassword, displayName)
            _registerState.value = result.fold(
                onSuccess = { AuthState.Success(it) },
                onFailure = { AuthState.Error(mapFirebaseError(it)) }
            )
        }
    }

    fun sendPasswordResetEmail(email: String) {
        viewModelScope.launch {
            _resetPasswordState.value = ResetPasswordState.Loading
            val result = sendPasswordResetEmailUseCase(email)
            _resetPasswordState.value = result.fold(
                onSuccess = { ResetPasswordState.Success },
                onFailure = { ResetPasswordState.Error(mapFirebaseError(it)) }
            )
        }
    }

    fun resetResetPasswordState() {
        _resetPasswordState.value = ResetPasswordState.Idle
    }

    fun logout() {
        logoutUseCase()
    }

    fun getCurrentUser(): User? = getCurrentUserUseCase()

    fun isLoggedIn(): Boolean = isLoggedInUseCase()

    /**
     * Map lỗi Firebase sang thông báo tiếng Việt thân thiện.
     */
    private fun mapFirebaseError(throwable: Throwable): String {
        val message = throwable.message ?: "Đã xảy ra lỗi không xác định"
        return when {
            message.contains("INVALID_LOGIN_CREDENTIALS") ||
            message.contains("INVALID_EMAIL") ||
            message.contains("invalid-credential") ->
                "Email hoặc mật khẩu không đúng"

            message.contains("USER_NOT_FOUND") ||
            message.contains("user-not-found") ->
                "Không tìm thấy tài khoản với email này"

            message.contains("WRONG_PASSWORD") ||
            message.contains("wrong-password") ->
                "Mật khẩu không đúng"

            message.contains("EMAIL_EXISTS") ||
            message.contains("email-already-in-use") ->
                "Email này đã được đăng ký"

            message.contains("WEAK_PASSWORD") ||
            message.contains("weak-password") ->
                "Mật khẩu quá yếu (cần ít nhất 6 ký tự)"

            message.contains("NETWORK") ||
            message.contains("network") ->
                "Lỗi kết nối mạng. Vui lòng kiểm tra internet"

            message.contains("TOO_MANY_REQUESTS") ||
            message.contains("too-many-requests") ->
                "Quá nhiều lần thử. Vui lòng thử lại sau"

            else -> message
        }
    }
}

