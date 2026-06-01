package com.example.engflash.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    val loginState by viewModel.loginState.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf<String?>(null) }

    val focusManager = LocalFocusManager.current

    // Navigate on success
    LaunchedEffect(loginState) {
        if (loginState is AuthState.Success) {
            onLoginSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFFBFBFE), Color(0xFFF3F1FD))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // Logo Container
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFFEAE5FF))
                        .border(1.5.dp, Color(0xFFC7B3FF), RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.MenuBook,
                        contentDescription = null,
                        tint = Color(0xFF5E3CB3),
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Brand Name
                Text(
                    text = "EngFlash",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF5E3CB3)
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Slogan
                Text(
                    text = "Làm chủ tiếng Anh cùng độ chính xác tuyệt đối",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF7D7799),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Form Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(28.dp),
                            clip = false
                        ),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Text(
                            text = "Chào mừng trở lại",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E1640)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Đăng nhập để tiếp tục học tập",
                            fontSize = 13.sp,
                            color = Color(0xFF7D7799)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Email Address label
                        Text(
                            text = "Địa chỉ Email",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF7D7799)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Email Input
                        OutlinedTextField(
                            value = email,
                            onValueChange = {
                                email = it
                                localError = null
                            },
                            placeholder = { Text("name@example.com", color = Color(0xFFB5B2C2)) },
                            leadingIcon = {
                                Text(
                                    text = "@",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF5E3CB3),
                                    modifier = Modifier.padding(start = 14.dp, end = 4.dp)
                                )
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            ),
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFFF7F5FC),
                                unfocusedContainerColor = Color(0xFFF7F5FC),
                                focusedBorderColor = Color(0xFFE5DFFF),
                                unfocusedBorderColor = Color(0xFFF1EEFA),
                                focusedTextColor = Color(0xFF1E1640),
                                unfocusedTextColor = Color(0xFF1E1640)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Password label
                        Text(
                            text = "Mật khẩu",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF7D7799)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Password Input
                        OutlinedTextField(
                            value = password,
                            onValueChange = {
                                password = it
                                localError = null
                            },
                            placeholder = { Text("........", color = Color(0xFFB5B2C2)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = Color(0xFF5E3CB3),
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Hiện/ẩn mật khẩu",
                                        tint = Color(0xFF7D7799),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None
                            else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { focusManager.clearFocus() }
                            ),
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFFF7F5FC),
                                unfocusedContainerColor = Color(0xFFF7F5FC),
                                focusedBorderColor = Color(0xFFE5DFFF),
                                unfocusedBorderColor = Color(0xFFF1EEFA),
                                focusedTextColor = Color(0xFF1E1640),
                                unfocusedTextColor = Color(0xFF1E1640)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Error Message if any
                        AnimatedVisibility(visible = loginState is AuthState.Error || localError != null) {
                            val errorMessage = localError ?: (loginState as? AuthState.Error)?.message ?: ""
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            ) {
                                Text(
                                    text = errorMessage,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(12.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        // Login Button
                        Button(
                            onClick = {
                                when {
                                    email.isBlank() -> {
                                        localError = "Địa chỉ email không được để trống"
                                    }
                                    password.isBlank() -> {
                                        localError = "Mật khẩu không được để trống"
                                    }
                                    else -> {
                                        localError = null
                                        viewModel.login(email.trim(), password)
                                    }
                                }
                            },
                            enabled = loginState !is AuthState.Loading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .shadow(2.dp, shape = RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF5E3CB3),
                                contentColor = Color.White
                            )
                        ) {
                            if (loginState is AuthState.Loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        "Đăng nhập ",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "⚡",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Forgot Password Link
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Quên mật khẩu?",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF5E3CB3),
                                modifier = Modifier
                                    .clickable { onNavigateToForgotPassword() }
                                    .padding(8.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Don't have an account? Join the Fleet
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Chưa có tài khoản? ",
                                fontSize = 14.sp,
                                color = Color(0xFF7D7799)
                            )
                            Text(
                                text = "Tham gia ngay",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF5E3CB3),
                                modifier = Modifier
                                    .clickable { onNavigateToRegister() }
                                    .padding(vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Footer
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = Color(0xFFB5B2C2),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Bảo mật thông tin bằng mã hóa AES-256",
                    fontSize = 12.sp,
                    color = Color(0xFF7D7799)
                )
            }
        }
    }
}
