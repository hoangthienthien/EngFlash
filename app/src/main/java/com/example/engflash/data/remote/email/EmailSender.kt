package com.example.engflash.data.remote.email

import android.util.Base64
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.Socket
import javax.net.ssl.SSLSocketFactory

/**
 * Gửi email OTP qua Gmail SMTP bằng Raw Socket.
 * Không dùng JavaMail (vì JavaMail không tương thích Android — thiếu java.awt).
 * Chỉ dùng Socket + SSLSocket thuần Java/Kotlin mà Android hỗ trợ 100%.
 *
 * Luồng SMTP:
 *   1. Kết nối TCP tới smtp.gmail.com:587
 *   2. EHLO → STARTTLS → Nâng cấp kết nối lên SSL
 *   3. AUTH LOGIN (Base64)
 *   4. MAIL FROM → RCPT TO → DATA → Nội dung email HTML
 *   5. QUIT
 */
class EmailSender {

    companion object {
        private const val TAG = "EmailSender"
        private const val SMTP_HOST = "smtp.gmail.com"
        private const val SMTP_PORT = 587
    }

    private val username = "hieusanta456@gmail.com"
    private val password = "ivay dxur oyah cjsl"

    suspend fun sendOtpEmail(toEmail: String, otpCode: String): Result<Unit> = withContext(Dispatchers.IO) {
        var socket: Socket? = null
        try {
            Log.d(TAG, "Bat dau gui OTP toi: $toEmail")

            // ── Bước 1: Kết nối TCP tới Gmail SMTP port 587 ──
            socket = Socket(SMTP_HOST, SMTP_PORT)
            socket.soTimeout = 15000 // 15s timeout
            var reader = BufferedReader(InputStreamReader(socket.getInputStream(), "UTF-8"))
            var writer = BufferedWriter(OutputStreamWriter(socket.getOutputStream(), "UTF-8"))

            // Đọc lời chào từ server (220 smtp.gmail.com ...)
            val greeting = readResponse(reader)
            Log.d(TAG, "Greeting: $greeting")

            // ── Bước 2: EHLO ──
            sendCommand(writer, "EHLO localhost")
            readResponse(reader)

            // ── Bước 3: STARTTLS — yêu cầu nâng cấp kết nối lên SSL/TLS ──
            sendCommand(writer, "STARTTLS")
            val starttlsResp = readResponse(reader)
            if (!starttlsResp.startsWith("220")) {
                return@withContext Result.failure(Exception("STARTTLS thất bại: $starttlsResp"))
            }

            // Nâng cấp Socket sang SSLSocket
            val sslSocketFactory = SSLSocketFactory.getDefault() as SSLSocketFactory
            val sslSocket = sslSocketFactory.createSocket(
                socket, SMTP_HOST, SMTP_PORT, true
            ) as javax.net.ssl.SSLSocket
            sslSocket.startHandshake()

            // Đổi reader/writer sang kết nối SSL mới
            reader = BufferedReader(InputStreamReader(sslSocket.getInputStream(), "UTF-8"))
            writer = BufferedWriter(OutputStreamWriter(sslSocket.getOutputStream(), "UTF-8"))

            // ── Bước 4: EHLO lần 2 (bắt buộc sau TLS) ──
            sendCommand(writer, "EHLO localhost")
            readResponse(reader)

            // ── Bước 5: AUTH LOGIN ──
            sendCommand(writer, "AUTH LOGIN")
            val authResp = readResponse(reader)
            if (!authResp.startsWith("334")) {
                return@withContext Result.failure(Exception("AUTH LOGIN thất bại: $authResp"))
            }

            // Gửi username (Base64)
            sendCommand(writer, Base64.encodeToString(username.toByteArray(), Base64.NO_WRAP))
            readResponse(reader)

            // Gửi password (Base64)
            sendCommand(writer, Base64.encodeToString(password.toByteArray(), Base64.NO_WRAP))
            val loginResp = readResponse(reader)
            if (!loginResp.startsWith("235")) {
                return@withContext Result.failure(Exception("Đăng nhập SMTP thất bại: $loginResp"))
            }
            Log.d(TAG, "SMTP Auth thanh cong!")

            // ── Bước 6: MAIL FROM ──
            sendCommand(writer, "MAIL FROM:<$username>")
            readResponse(reader)

            // ── Bước 7: RCPT TO ──
            sendCommand(writer, "RCPT TO:<$toEmail>")
            readResponse(reader)

            // ── Bước 8: DATA — Gửi nội dung email ──
            sendCommand(writer, "DATA")
            val dataResp = readResponse(reader)
            if (!dataResp.startsWith("354")) {
                return@withContext Result.failure(Exception("DATA thất bại: $dataResp"))
            }

            // Xây dựng nội dung email HTML
            val subject = "Ma xac thuc OTP - EngFlash"
            val encodedSubject = "=?UTF-8?B?${Base64.encodeToString(subject.toByteArray(), Base64.NO_WRAP)}?="

            val htmlBody = """
                <div style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto;padding:20px;border:1px solid #e0e0e0;border-radius:8px;">
                    <h2 style="color:#6200EE;text-align:center;">Khoi phuc mat khau EngFlash</h2>
                    <p>Chao ban,</p>
                    <p>Ma xac thuc OTP cua ban la:</p>
                    <div style="text-align:center;margin:30px 0;">
                        <span style="font-size:32px;font-weight:bold;letter-spacing:5px;color:#6200EE;background-color:#f3e5f5;padding:10px 20px;border-radius:4px;border:1px dashed #6200EE;">
                            $otpCode
                        </span>
                    </div>
                    <p style="color:#666;font-size:14px;">Ma OTP co hieu luc trong 5 phut.</p>
                    <p style="text-align:center;color:#999;font-size:12px;">EngFlash 2026</p>
                </div>
            """.trimIndent()

            // Ghi header + body theo chuẩn RFC 5321
            val emailData = buildString {
                append("From: EngFlash Support <$username>\r\n")
                append("To: $toEmail\r\n")
                append("Subject: $encodedSubject\r\n")
                append("MIME-Version: 1.0\r\n")
                append("Content-Type: text/html; charset=UTF-8\r\n")
                append("\r\n") // Dòng trống ngăn cách header và body
                append(htmlBody)
                append("\r\n.\r\n") // Kết thúc DATA bằng dấu chấm đơn trên 1 dòng
            }

            writer.write(emailData)
            writer.flush()

            val sendResp = readResponse(reader)
            if (!sendResp.startsWith("250")) {
                return@withContext Result.failure(Exception("Gửi email thất bại: $sendResp"))
            }

            // ── Bước 9: QUIT ──
            sendCommand(writer, "QUIT")

            sslSocket.close()
            Log.d(TAG, "Gui OTP thanh cong toi: $toEmail")
            Result.success(Unit)

        } catch (e: Exception) {
            Log.e(TAG, "Loi gui email OTP: ${e.message}", e)
            try { socket?.close() } catch (_: Exception) {}
            Result.failure(e)
        }
    }

    /**
     * Đọc phản hồi SMTP (hỗ trợ multi-line response).
     * Multi-line: "250-..." (có dấu gạch ngang), Last line: "250 ..." (có khoảng trắng).
     */
    private fun readResponse(reader: BufferedReader): String {
        val response = StringBuilder()
        var line: String?
        do {
            line = reader.readLine() ?: break
            response.appendLine(line)
        } while (line.length >= 4 && line[3] == '-')
        return response.toString().trim()
    }

    /**
     * Gửi lệnh SMTP kèm CRLF.
     */
    private fun sendCommand(writer: BufferedWriter, command: String) {
        writer.write(command + "\r\n")
        writer.flush()
    }
}
