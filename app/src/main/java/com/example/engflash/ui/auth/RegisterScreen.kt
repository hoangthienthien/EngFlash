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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
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
fun RegisterScreen(
    viewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    val registerState by viewModel.registerState.collectAsState()

    var displayName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current

    // Local validation errors
    val passwordMismatch = confirmPassword.isNotEmpty() && password != confirmPassword
    val passwordTooShort = password.isNotEmpty() && password.length < 6

    // Navigate on success
    LaunchedEffect(registerState) {
        if (registerState is AuthState.Success) {
            onRegisterSuccess()
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
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Top Header Row with back arrow
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onNavigateToLogin,
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFFE5DFFF), RoundedCornerShape(12.dp))
                            .size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = Color(0xFF5E3CB3),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Logo Container
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFFEAE5FF))
                        .border(1.5.dp, Color(0xFFC7B3FF), RoundedCornerShape(18.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.MenuBook,
                        contentDescription = null,
                        tint = Color(0xFF5E3CB3),
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Brand Name
                Text(
                    text = "EngFlash",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF5E3CB3)
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Slogan
                Text(
                    text = "Đăng ký thành viên mới",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF7D7799),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

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
                            text = "Tạo tài khoản",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E1640)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Nhập thông tin bên dưới để bắt đầu học",
                            fontSize = 13.sp,
                            color = Color(0xFF7D7799)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Display Name label
                        Text(
                            text = "Tên hiển thị",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF7D7799)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Display Name Input
                        OutlinedTextField(
                            value = displayName,
                            onValueChange = { displayName = it },
                            placeholder = { Text("Họ và tên của bạn", color = Color(0xFFB5B2C2)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color(0xFF5E3CB3),
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
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

                        Spacer(modifier = Modifier.height(14.dp))

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
                            onValueChange = { email = it },
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

                        Spacer(modifier = Modifier.height(14.dp))

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
                            onValueChange = { password = it },
                            placeholder = { Text("Tối thiểu 6 ký tự", color = Color(0xFFB5B2C2)) },
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
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            ),
                            isError = passwordTooShort,
                            supportingText = if (passwordTooShort) {
                                { Text("Mật khẩu cần ít nhất 6 ký tự", color = MaterialTheme.colorScheme.error) }
                            } else null,
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

                        Spacer(modifier = Modifier.height(14.dp))

                        // Confirm Password label
                        Text(
                            text = "Xác nhận mật khẩu",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF7D7799)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Confirm Password Input
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            placeholder = { Text("Nhập lại mật khẩu", color = Color(0xFFB5B2C2)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = Color(0xFF5E3CB3),
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                    Icon(
                                        imageVector = if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Hiện/ẩn mật khẩu",
                                        tint = Color(0xFF7D7799),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            },
                            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None
                            else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { focusManager.clearFocus() }
                            ),
                            isError = passwordMismatch,
                            supportingText = if (passwordMismatch) {
                                { Text("Mật khẩu không khớp", color = MaterialTheme.colorScheme.error) }
                            } else null,
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
                        AnimatedVisibility(visible = registerState is AuthState.Error) {
                            val errorMessage = (registerState as? AuthState.Error)?.message ?: ""
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

                        // Register Button
                        val canRegister = displayName.isNotBlank()
                                && email.isNotBlank()
                                && password.length >= 6
                                && password == confirmPassword
                                && registerState !is AuthState.Loading

                        Button(
                            onClick = { viewModel.register(email.trim(), password, confirmPassword, displayName.trim()) },
                            enabled = canRegister,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .shadow(2.dp, shape = RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF5E3CB3),
                                disabledContainerColor = Color(0xFF5E3CB3).copy(alpha = 0.5f),
                                contentColor = Color.White,
                                disabledContentColor = Color.White.copy(alpha = 0.7f)
                            )
                        ) {
                            if (registerState is AuthState.Loading) {
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
                                        "Đăng ký ",
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

                        // Already have an account? Login Link
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Đã có tài khoản? ",
                                fontSize = 14.sp,
                                color = Color(0xFF7D7799)
                            )
                            Text(
                                text = "Đăng nhập",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF5E3CB3),
                                modifier = Modifier
                                    .clickable { onNavigateToLogin() }
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
