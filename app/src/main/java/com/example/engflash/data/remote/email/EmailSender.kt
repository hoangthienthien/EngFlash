package com.example.engflash.data.remote.email

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Properties

class EmailSender {

    private val username = "hieusanta456@gmail.com"
    private val password = "ivay dxur oyah cjsl"

    suspend fun sendOtpEmail(toEmail: String, otpCode: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Log.d("EmailSender", "Bắt đầu gửi OTP tới: $toEmail")

            val props = Properties().apply {
                put("mail.smtp.host", "smtp.gmail.com")
                put("mail.smtp.port", "587")
                put("mail.smtp.auth", "true")
                put("mail.smtp.starttls.enable", "true")
                put("mail.smtp.starttls.required", "true")
                put("mail.smtp.ssl.protocols", "TLSv1.2")
                put("mail.smtp.connectiontimeout", "10000")
                put("mail.smtp.timeout", "10000")
                put("mail.smtp.writetimeout", "10000")
            }

            val session = javax.mail.Session.getInstance(props, object : javax.mail.Authenticator() {
                override fun getPasswordAuthentication(): javax.mail.PasswordAuthentication {
                    return javax.mail.PasswordAuthentication(username, password)
                }
            })

            val message = javax.mail.internet.MimeMessage(session).apply {
                setFrom(javax.mail.internet.InternetAddress(username, "EngFlash Support"))
                setRecipients(
                    javax.mail.Message.RecipientType.TO,
                    javax.mail.internet.InternetAddress.parse(toEmail)
                )
                subject = "Ma xac thuc OTP dat lai mat khau - EngFlash"

                val htmlContent = """
                    <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 8px;">
                        <h2 style="color: #6200EE; text-align: center;">Khoi phuc mat khau EngFlash</h2>
                        <p>Chao ban,</p>
                        <p>Chung toi nhan duoc yeu cau khoi phuc mat khau cho tai khoan EngFlash cua ban. Vui long su dung ma xac thuc (OTP) duoi day:</p>
                        <div style="text-align: center; margin: 30px 0;">
                            <span style="font-size: 32px; font-weight: bold; letter-spacing: 5px; color: #6200EE; background-color: #f3e5f5; padding: 10px 20px; border-radius: 4px; border: 1px dashed #6200EE;">
                                $otpCode
                            </span>
                        </div>
                        <p style="color: #666; font-size: 14px;">Ma OTP nay co hieu luc trong vong <b>5 phut</b>. Vui long khong chia se ma nay voi bat ky ai.</p>
                        <p>Neu ban khong gui yeu cau nay, vui long bo qua email nay.</p>
                        <hr style="border: 0; border-top: 1px solid #eee; margin: 20px 0;">
                        <p style="text-align: center; color: #999; font-size: 12px;">Doi ngu EngFlash 2026</p>
                    </div>
                """.trimIndent()

                setContent(htmlContent, "text/html; charset=utf-8")
            }

            javax.mail.Transport.send(message)
            Log.d("EmailSender", "Gui OTP thanh cong toi: $toEmail")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("EmailSender", "Loi gui email OTP: ${e.message}", e)
            Result.failure(e)
        }
    }
}
