package com.example.engflash.data.remote.email

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class EmailSender {

    private val username = "hieusanta456@gmail.com"
    private val password = "ivaydxuroyahcjsl" // Xóa khoảng trắng trong mật khẩu ứng dụng Gmail

    suspend fun sendOtpEmail(toEmail: String, otpCode: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val props = Properties().apply {
                put("mail.smtp.host", "smtp.gmail.com")
                put("mail.smtp.socketFactory.port", "465")
                put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
                put("mail.smtp.auth", "true")
                put("mail.smtp.port", "465")
            }

            val session = Session.getInstance(props, object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(username, password)
                }
            })

            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(username, "EngFlash Support"))
                setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail))
                subject = "Mã xác thực OTP đặt lại mật khẩu - EngFlash"
                
                val htmlContent = """
                    <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 8px;">
                        <h2 style="color: #6200EE; text-align: center;">Khôi phục mật khẩu EngFlash</h2>
                        <p>Chào bạn,</p>
                        <p>Chúng tôi nhận được yêu cầu khôi phục mật khẩu cho tài khoản EngFlash của bạn. Vui lòng sử dụng mã xác thực (OTP) dưới đây để tiếp tục:</p>
                        <div style="text-align: center; margin: 30px 0;">
                            <span style="font-size: 32px; font-weight: bold; letter-spacing: 5px; color: #6200EE; background-color: #f3e5f5; padding: 10px 20px; border-radius: 4px; border: 1px dashed #6200EE;">
                                $otpCode
                            </span>
                        </div>
                        <p style="color: #666; font-size: 14px;">Mã OTP này có hiệu lực trong vòng <b>5 phút</b>. Vui lòng không chia sẻ mã này với bất kỳ ai để bảo vệ tài khoản của bạn.</p>
                        <p>Nếu bạn không gửi yêu cầu này, vui lòng bỏ qua email này.</p>
                        <hr style="border: 0; border-top: 1px solid #eee; margin: 20px 0;">
                        <p style="text-align: center; color: #999; font-size: 12px;">Đội ngũ EngFlash &copy; 2026</p>
                    </div>
                """.trimIndent()
                
                setContent(htmlContent, "text/html; charset=utf-8")
            }

            Transport.send(message)
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
