package com.example.engflash.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    viewModel: AuthViewModel,
    onBackToLogin: () -> Unit
) {
    val otpState by viewModel.otpState.collectAsState()
    val countdown by viewModel.countdown.collectAsState()
    val otpEmail = viewModel.otpEmail

    var emailInput by remember { mutableStateOf("") }
    var otpInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    // Quản lý Focus cho OTP TextField ẩn
    val focusRequester = remember { FocusRequester() }

    // Reset OTP state khi vào màn hình lần đầu
    DisposableEffect(Unit) {
        viewModel.resetOtpState()
        onDispose {
            viewModel.resetOtpState()
        }
    }

    // Lắng nghe trạng thái OTP để hiển thị lỗi hoặc dialog thành công
    LaunchedEffect(otpState) {
        when (otpState) {
            is OtpState.Error -> {
                errorMessage = (otpState as OtpState.Error).message
            }
            is OtpState.Sent -> {
                errorMessage = null
                otpInput = ""
                // Tự động focus vào ô nhập OTP khi vừa gửi xong
                try {
                    focusRequester.requestFocus()
                } catch (_: Exception) {}
            }
            is OtpState.SuccessResetLink -> {
                errorMessage = null
                showSuccessDialog = true
            }
            else -> {
                errorMessage = null
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quên mật khẩu", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackToLogin) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Quay lại"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                        )
                    )
                ),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                // Icon lớn
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LockReset,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = if (otpState is OtpState.Idle || otpState is OtpState.Loading && otpEmail.isBlank()) {
                        "Khôi phục tài khoản"
                    } else {
                        "Nhập mã xác thực"
                    },
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (otpState is OtpState.Idle || otpState is OtpState.Loading && otpEmail.isBlank()) {
                        "Nhập email đã đăng ký của bạn. Chúng tôi sẽ gửi mã OTP gồm 6 chữ số để xác minh danh tính."
                    } else {
                        "Mã xác thực đã được gửi thành công đến địa chỉ email:\n$otpEmail"
                    },
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Hộp hiển thị lỗi nếu có
                AnimatedVisibility(
                    visible = errorMessage != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    errorMessage?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // ─── GIAI ĐOẠN 1: NHẬP EMAIL ──────────────────────────────────────────
                if (otpState is OtpState.Idle || (otpState is OtpState.Loading && otpEmail.isBlank()) || (otpState is OtpState.Error && otpEmail.isBlank())) {
                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text("Địa chỉ Email") },
                        placeholder = { Text("example@gmail.com") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Email, contentDescription = null)
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { viewModel.sendOtp(emailInput.trim()) },
                        enabled = emailInput.isNotBlank() && otpState !is OtpState.Loading,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        if (otpState is OtpState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Gửi mã OTP", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // ─── GIAI ĐOẠN 2: NHẬP MÃ OTP 6 SỐ ─────────────────────────────────────
                else {
                    // Mẹo thiết kế OTP: Dùng BasicTextField ẩn để đón phím gõ
                    BasicTextField(
                        value = otpInput,
                        onValueChange = {
                            if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                                otpInput = it
                                if (it.length == 6) {
                                    viewModel.verifyOtp(it)
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier
                            .size(1.dp)
                            .focusRequester(focusRequester)
                    )

                    // Vẽ 6 ô vuông hiển thị số
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                try {
                                    focusRequester.requestFocus()
                                } catch (_: Exception) {}
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 0 until 6) {
                            val char = when {
                                i < otpInput.length -> otpInput[i].toString()
                                else -> ""
                            }
                            
                            val isFocused = otpInput.length == i

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .border(
                                        width = if (isFocused) 2.dp else 1.dp,
                                        color = if (isFocused) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                        },
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .background(
                                        color = if (isFocused) {
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                                        } else {
                                            MaterialTheme.colorScheme.surface
                                        },
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = char,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Đếm ngược đếm giây gửi lại mã
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (countdown > 0) {
                            Text(
                                text = "Gửi lại mã sau ${countdown}s",
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                fontSize = 14.sp
                            )
                        } else {
                            Text(
                                text = "Bạn chưa nhận được mã? ",
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Gửi lại ngay",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable {
                                    viewModel.resendOtp()
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Nút xác thực thủ công (phòng trường hợp người dùng nhập đủ 6 số nhưng không tự trigger)
                    Button(
                        onClick = { viewModel.verifyOtp(otpInput) },
                        enabled = otpInput.length == 6 && otpState !is OtpState.Loading,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        if (otpState is OtpState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Xác thực OTP", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    TextButton(
                        onClick = { viewModel.resetOtpState() },
                        enabled = otpState !is OtpState.Loading
                    ) {
                        Text("Thay đổi địa chỉ Email")
                    }
                }
            }
        }
    }

    // ─── DIALOG THÀNH CÔNG (Sau khi đã xác thực OTP thành công và Firebase gửi link) ───
    if (showSuccessDialog) {
        Dialog(
            onDismissRequest = {
                showSuccessDialog = false
                onBackToLogin()
            },
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🎉 Xác thực thành công!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Hệ thống đã xác minh email chính chủ thành công. Để hoàn tất đổi mật khẩu một cách an toàn nhất, Firebase đã gửi một liên kết đổi mật khẩu tới email:\n\n$otpEmail\n\nVui lòng kiểm tra hộp thư, nhấp vào liên kết để đặt lại mật khẩu mới, sau đó quay lại đăng nhập.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = {
                            showSuccessDialog = false
                            onBackToLogin()
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("Quay lại màn hình Đăng nhập", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
