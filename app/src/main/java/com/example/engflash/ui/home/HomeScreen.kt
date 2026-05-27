package com.example.engflash.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engflash.ui.theme.*
import com.example.engflash.ui.navigation.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToVocabulary: () -> Unit,
    onNavigateToGrammar: () -> Unit,
    onNavigateToFlashcard: () -> Unit,
    onLogout: () -> Unit
) {
    val topics by viewModel.topics.collectAsState(initial = emptyList())
    val currentUser = viewModel.getCurrentUser()
    val displayName = currentUser?.displayName?.ifEmpty { "Bạn" } ?: "Bạn"

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // ─── Gradient Header ─────────────────────────
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(GradientStart, GradientMiddle, GradientEnd),
                                start = Offset(0f, 0f),
                                end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                            )
                        )
                        .padding(top = 48.dp, bottom = 32.dp, start = 24.dp, end = 24.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Xin chào! 👋",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                                Text(
                                    text = displayName,
                                    style = MaterialTheme.typography.headlineLarge,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            IconButton(
                                onClick = {
                                    viewModel.logout()
                                    onLogout()
                                },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f))
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.Logout,
                                    contentDescription = "Đăng xuất",
                                    tint = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Stats row
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White.copy(alpha = 0.15f)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                StatItem(icon = Icons.Default.MenuBook, value = "${topics.size}", label = "Chủ đề")
                                StatItem(icon = Icons.Default.EmojiEvents, value = "0", label = "Hoàn thành")
                                StatItem(icon = Icons.Default.LocalFireDepartment, value = "0", label = "Streak")
                            }
                        }
                    }
                }
            }

            // ─── Section Title ───────────────────────────
            item {
                Text(
                    text = "Chọn mục học tập",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 24.dp, top = 24.dp, bottom = 12.dp)
                )
            }

            // ─── Section 1: Từ vựng (Vocabulary) ─────────
            item {
                CategoryCard(
                    title = "Từ Vựng",
                    subtitle = "Học qua thẻ lật thông minh và làm quiz ôn tập.",
                    tag = "HỌC TẬP",
                    icon = Icons.Default.MenuBook,
                    gradientColors = listOf(Color(0xFFFFA07A), Color(0xFFFF4500)), // Salmon to OrangeRed
                    onClick = onNavigateToVocabulary
                )
            }

            // ─── Section 2: Ngữ pháp (Grammar) ───────────
            item {
                CategoryCard(
                    title = "Ngữ Pháp",
                    subtitle = "Nắm vững các thì, câu điều kiện & làm bài quiz sinh động.",
                    tag = "LÝ THUYẾT & QUIZ",
                    icon = Icons.Default.School,
                    gradientColors = listOf(Color(0xFF36D1DC), Color(0xFF5B86E5)), // Teal to Blue
                    onClick = onNavigateToGrammar
                )
            }

            // ─── Section 3: Flashcard (Flashcards) ────────
            item {
                CategoryCard(
                    title = "Flashcard Ôn Tập",
                    subtitle = "Ôn tập các từ vựng khó đã được đánh dấu lưu trữ.",
                    tag = "ÔN TẬP NHANH",
                    icon = Icons.Default.Style,
                    gradientColors = listOf(Color(0xFF11998E), Color(0xFF38EF7D)), // Forest Green to Emerald
                    onClick = onNavigateToFlashcard
                )
            }
        }
    }
}

@Composable
private fun StatItem(icon: ImageVector, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(label, color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun CategoryCard(
    title: String,
    subtitle: String,
    tag: String,
    icon: ImageVector,
    gradientColors: List<Color>,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .shadow(4.dp, shape = RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(Brush.linearGradient(gradientColors))
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    // Tag label
                    Box(
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.25f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = tag,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f),
                        lineHeight = 20.sp
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Beautiful glassmorphic circle for Icon
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = title,
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
        }
    }
}
