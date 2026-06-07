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
import com.example.engflash.domain.usecase.auth.SendOtpEmailUseCase
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

sealed class ChangePasswordState {
    object Idle : ChangePasswordState()
    object Loading : ChangePasswordState()
    object Success : ChangePasswordState()
    data class Error(val message: String) : ChangePasswordState()
}

/**
 * Trạng thái riêng cho chức năng gửi và xác thực OTP.
 */
sealed class OtpState {
    object Idle : OtpState()
    object Loading : OtpState()
    object Sent : OtpState()
    object Verified : OtpState()
    object EnterNewPassword : OtpState()
    object SuccessResetLink : OtpState()
    data class Error(val message: String) : OtpState()
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    // ─── UseCases (nhận qua Application thay vì Repository trực tiếp) ─
    // Manual Dependency injection 
    private val app = application as EngFlashApplication
    private val loginUseCase: LoginUseCase = app.loginUseCase
    private val registerUseCase: RegisterUseCase = app.registerUseCase
    private val sendPasswordResetEmailUseCase: SendPasswordResetEmailUseCase = app.sendPasswordResetEmailUseCase
    private val sendOtpEmailUseCase: SendOtpEmailUseCase = app.sendOtpEmailUseCase
    private val logoutUseCase: LogoutUseCase = app.logoutUseCase
    private val getCurrentUserUseCase: GetCurrentUserUseCase = app.getCurrentUserUseCase
    private val isLoggedInUseCase: IsLoggedInUseCase = app.isLoggedInUseCase
    private val changePasswordUseCase: com.example.engflash.domain.usecase.auth.ChangePasswordUseCase = app.changePasswordUseCase

    // ─── Login State ─────────────────────────────────────
    private val _loginState = MutableStateFlow<AuthState>(AuthState.Idle)
    val loginState: StateFlow<AuthState> = _loginState.asStateFlow()

    // ─── Register State ──────────────────────────────────
    private val _registerState = MutableStateFlow<AuthState>(AuthState.Idle)
    val registerState: StateFlow<AuthState> = _registerState.asStateFlow()

    // ─── Reset Password State ────────────────────────────
    private val _resetPasswordState = MutableStateFlow<ResetPasswordState>(ResetPasswordState.Idle)
    val resetPasswordState: StateFlow<ResetPasswordState> = _resetPasswordState.asStateFlow()

    // ─── Change Password State ───────────────────────────
    private val _changePasswordState = MutableStateFlow<ChangePasswordState>(ChangePasswordState.Idle)
    val changePasswordState: StateFlow<ChangePasswordState> = _changePasswordState.asStateFlow()

    // ─── OTP State ───────────────────────────────────────
    private val _otpState = MutableStateFlow<OtpState>(OtpState.Idle)
    val otpState: StateFlow<OtpState> = _otpState.asStateFlow()

    private val _countdown = MutableStateFlow(0)
    val countdown: StateFlow<Int> = _countdown.asStateFlow()

    private var generatedOtp: String? = null
    var otpEmail: String = ""
        private set

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

    fun changePassword(currentPassword: String, newPassword: String, confirmPassword: String) {
        if (newPassword.length < 6) {
            _changePasswordState.value = ChangePasswordState.Error("Mật khẩu mới phải có ít nhất 6 ký tự")
            return
        }
        if (newPassword != confirmPassword) {
            _changePasswordState.value = ChangePasswordState.Error("Mật khẩu xác nhận không khớp")
            return
        }

        viewModelScope.launch {
            _changePasswordState.value = ChangePasswordState.Loading
            val result = changePasswordUseCase(currentPassword, newPassword)
            _changePasswordState.value = result.fold(
                onSuccess = { ChangePasswordState.Success },
                onFailure = { ChangePasswordState.Error(mapFirebaseError(it)) }
            )
        }
    }

    fun resetChangePasswordState() {
        _changePasswordState.value = ChangePasswordState.Idle
    }

    // ─── OTP Business Logic ────────────────────────────────
    fun sendOtp(email: String) {
        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _otpState.value = OtpState.Error("Email không đúng định dạng")
            return
        }
        
        viewModelScope.launch {
            _otpState.value = OtpState.Loading
            
            try {
                // Sinh mã OTP 6 số ngẫu nhiên
                val otp = (100000..999999).random().toString()
                android.util.Log.d("AuthViewModel", "DEBUG: Generated OTP for $email is $otp")
                
                val result = sendOtpEmailUseCase(email, otp)
                result.fold(
                    onSuccess = {
                        generatedOtp = otp
                        otpEmail = email
                        _otpState.value = OtpState.Sent
                        startCountdown()
                    },
                    onFailure = { e ->
                        android.util.Log.e("AuthViewModel", "Lỗi gửi OTP: ${e.message}", e)
                        _otpState.value = OtpState.Error("Không thể gửi email OTP. Vui lòng thử lại.")
                    }
                )
            } catch (e: Exception) {
                android.util.Log.e("AuthViewModel", "Exception gửi OTP: ${e.message}", e)
                _otpState.value = OtpState.Error("Đã xảy ra lỗi: ${e.localizedMessage}")
            }
        }
    }

    fun verifyOtp(enteredOtp: String) {
        if (enteredOtp.length != 6) {
            _otpState.value = OtpState.Error("Mã OTP phải có 6 chữ số")
            return
        }

        if (generatedOtp == null || otpEmail.isBlank()) {
            _otpState.value = OtpState.Error("Phiên xác thực đã hết hạn, vui lòng gửi lại mã")
            return
        }

        if (enteredOtp == generatedOtp) {
            // OTP đúng → chuyển sang bước nhập mật khẩu mới
            _otpState.value = OtpState.EnterNewPassword
        } else {
            _otpState.value = OtpState.Error("Mã xác thực OTP không chính xác")
        }
    }

    fun submitNewPassword(newPassword: String, confirmPassword: String) {
        if (newPassword.length < 6) {
            _otpState.value = OtpState.Error("Mật khẩu phải có ít nhất 6 ký tự")
            return
        }
        if (newPassword != confirmPassword) {
            _otpState.value = OtpState.Error("Mật khẩu xác nhận không khớp")
            return
        }

        viewModelScope.launch {
            _otpState.value = OtpState.Loading
            try {
                val result = sendPasswordResetEmailUseCase(otpEmail)
                result.fold(
                    onSuccess = {
                        _otpState.value = OtpState.SuccessResetLink
                    },
                    onFailure = {
                        _otpState.value = OtpState.Error("Lỗi: ${mapFirebaseError(it)}")
                    }
                )
            } catch (e: Exception) {
                _otpState.value = OtpState.Error("Đã xảy ra lỗi: ${e.localizedMessage}")
            }
        }
    }

    fun resendOtp() {
        if (otpEmail.isNotBlank() && _countdown.value == 0) {
            sendOtp(otpEmail)
        }
    }

    fun resetOtpState() {
        _otpState.value = OtpState.Idle
        _countdown.value = 0
        generatedOtp = null
        otpEmail = ""
    }

    private fun startCountdown() {
        _countdown.value = 60
        viewModelScope.launch {
            while (_countdown.value > 0) {
                kotlinx.coroutines.delay(1000)
                _countdown.value -= 1
            }
        }
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

