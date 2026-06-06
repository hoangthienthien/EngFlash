package com.example.engflash.ui.auth

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController

private val PageBg @Composable get() = MaterialTheme.colorScheme.background
private val PurplePrimary @Composable get() = MaterialTheme.colorScheme.primary
private val TextPrimary @Composable get() = MaterialTheme.colorScheme.onBackground
private val TextSecondary @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
private val BorderColor @Composable get() = MaterialTheme.colorScheme.outlineVariant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    navController: NavController,
    viewModel: AuthViewModel
) {
    val uiState by viewModel.changePasswordState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var currentPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        when (uiState) {
            is ChangePasswordState.Success -> {
                Toast.makeText(context, "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show()
                viewModel.resetChangePasswordState()
                navController.popBackStack()
            }
            is ChangePasswordState.Error -> {
                Toast.makeText(context, (uiState as ChangePasswordState.Error).message, Toast.LENGTH_LONG).show()
                viewModel.resetChangePasswordState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Đổi mật khẩu",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PageBg)
            )
        },
        containerColor = PageBg
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "Vui lòng nhập mật khẩu hiện tại của bạn để xác thực, sau đó nhập mật khẩu mới.",
                color = TextSecondary,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )

            // Current Password
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Mật khẩu hiện tại", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it },
                    placeholder = { Text("Nhập mật khẩu hiện tại", color = TextSecondary.copy(alpha = 0.5f)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = outlinedFieldColors(),
                    visualTransformation = if (currentPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        val image = if (currentPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                        IconButton(onClick = { currentPasswordVisible = !currentPasswordVisible }) {
                            Icon(imageVector = image, contentDescription = null, tint = TextSecondary)
                        }
                    }
                )
            }

            // New Password
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Mật khẩu mới", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    placeholder = { Text("Nhập mật khẩu mới (ít nhất 6 ký tự)", color = TextSecondary.copy(alpha = 0.5f)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = outlinedFieldColors(),
                    visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        val image = if (newPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                        IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                            Icon(imageVector = image, contentDescription = null, tint = TextSecondary)
                        }
                    }
                )
            }

            // Confirm Password
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Xác nhận mật khẩu mới", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    placeholder = { Text("Nhập lại mật khẩu mới", color = TextSecondary.copy(alpha = 0.5f)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = outlinedFieldColors(),
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        val image = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(imageVector = image, contentDescription = null, tint = TextSecondary)
                        }
                    }
                )
            }

            Spacer(Modifier.height(12.dp))

            // Submit Button
            Button(
                onClick = {
                    viewModel.changePassword(currentPassword, newPassword, confirmPassword)
                },
                enabled = currentPassword.isNotBlank() && newPassword.isNotBlank() && confirmPassword.isNotBlank() && uiState !is ChangePasswordState.Loading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PurplePrimary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                if (uiState is ChangePasswordState.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Text("Đổi mật khẩu", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
private fun outlinedFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = PurplePrimary,
    unfocusedBorderColor = BorderColor,
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent
)
