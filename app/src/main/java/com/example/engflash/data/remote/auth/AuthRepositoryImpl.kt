package com.example.engflash.data.remote.auth

import com.example.engflash.data.remote.email.EmailSender
import com.example.engflash.domain.model.User
import com.example.engflash.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.tasks.await

class AuthRepositoryImpl : AuthRepository {
    //FirebaseAuth là cổng giao tiếp chính giúp ứng dụng Android của bạn gửi yêu cầu và nhận phản hồi từ hệ thống quản lý tài khoản trên máy chỉ Firebase
    //getInstance() dựa trên mẫu thiết kế Singleton pattern, khi gọi thì firebase sẽ kiểm tra xem đã có kết nối nào được tạo ra trước đó chưa, nếu chưa thì khởi tạo một đối tượng mới, nếu rồi thì tái sử dụng đối tượng cũ
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    
    private val emailSender: EmailSender = EmailSender()

    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            //hàm await là một hàm vô cùng quan trong trong lập trình bất đồng bộ sử dụng Kotlin Coroutines
            //signInWithEmailAndPassword là một tác vụ mạng, tốn thời gian để xử lý và chờ phản hồi => không thể trả về kết quả đăng nhập ngay lập tức
            //mặc định firebase sẽ trả về một đối tượng gọi là: Task<AuthResult>. Task là một lớp trừu tượng đại diện cho một tác vụ bất đồng bộ trong Android dev

            val firebaseUser = result.user ?: throw Exception("Đăng nhập thất bại")
            Result.success(
                User(
                    uid = firebaseUser.uid,
                    email = firebaseUser.email ?: "",
                    displayName = firebaseUser.displayName ?: ""
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(
        email: String,
        password: String,
        displayName: String
    ): Result<User> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user ?: throw Exception("Đăng ký thất bại")

            // Cập nhật tên hiển thị sau khi tạo tài khoản
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build()
            firebaseUser.updateProfile(profileUpdates).await()

            Result.success(
                User(
                    uid = firebaseUser.uid,
                    email = firebaseUser.email ?: "",
                    displayName = displayName
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendOtpEmail(email: String, otp: String): Result<Unit> {
        return emailSender.sendOtpEmail(email, otp)
    }

    override fun logout() {
        firebaseAuth.signOut()
    }

    override fun getCurrentUser(): User? {
        val firebaseUser = firebaseAuth.currentUser ?: return null
        return User(
            uid = firebaseUser.uid,
            email = firebaseUser.email ?: "",
            displayName = firebaseUser.displayName ?: ""
        )
    }

    override fun isLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }

    override suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> {
        return try {
            val user = firebaseAuth.currentUser ?: throw Exception("Chưa đăng nhập")
            val email = user.email ?: throw Exception("Không tìm thấy email")

            // Re-authenticate trước khi đổi mật khẩu (Firebase yêu cầu)
            val credential = com.google.firebase.auth.EmailAuthProvider
                .getCredential(email, currentPassword)
            user.reauthenticate(credential).await()

            // Sau khi xác thực thành công, cập nhật mật khẩu mới
            user.updatePassword(newPassword).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
