package com.example.engflash.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

private val PageBg @Composable get() = MaterialTheme.colorScheme.background
private val CardBg @Composable get() = MaterialTheme.colorScheme.surface
private val PurplePrimary @Composable get() = MaterialTheme.colorScheme.primary
private val PurpleLight @Composable get() = MaterialTheme.colorScheme.primaryContainer
private val TextPrimary @Composable get() = MaterialTheme.colorScheme.onBackground
private val TextSecondary @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(
    navController: NavController,
    viewModel: ProfileViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentStreak by viewModel.streakManager.currentStreak.collectAsState()
    val achievements = remember(uiState, currentStreak) { viewModel.getAchievements(currentStreak) }

    val unlockedCount = achievements.count { it.isUnlocked }
    val totalCount    = achievements.size

    Scaffold(
        containerColor = PageBg,
        topBar = {
            TopAppBar(
                title = { Text("Thành tựu", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CardBg)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            // ── Header summary ────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                shape = RoundedCornerShape(20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(listOf(PurplePrimary, PurpleLight))
                        )
                        .padding(20.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "$unlockedCount / $totalCount",
                            color = Color.White,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Thành tựu đã mở khóa",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 14.sp
                        )
                        Spacer(Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = { unlockedCount.toFloat() / totalCount },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape),
                            color = Color.White,
                            trackColor = Color.White.copy(alpha = 0.3f)
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Achievement Grid ──────────────────────────────
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement   = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(achievements) { achievement ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (achievement.isUnlocked) CardBg else Color(0xFFF0EEF8)
                        ),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = if (achievement.isUnlocked) 4.dp else 0.dp
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Icon circle
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (achievement.isUnlocked)
                                            achievement.tint.copy(alpha = 0.15f)
                                        else
                                            Color.Gray.copy(alpha = 0.08f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    achievement.icon, null,
                                    tint = if (achievement.isUnlocked) achievement.tint else Color.Gray.copy(0.4f),
                                    modifier = Modifier.size(30.dp)
                                )
                            }

                            Spacer(Modifier.height(10.dp))

                            Text(
                                achievement.label,
                                color = if (achievement.isUnlocked) TextPrimary else TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center,
                                lineHeight = 16.sp
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                achievement.description,
                                color = TextSecondary,
                                fontSize = 10.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 14.sp
                            )

                            Spacer(Modifier.height(10.dp))

                            // Progress bar
                            if (achievement.isUnlocked) {
                                Text(
                                    "✅ Đã mở khóa",
                                    color = achievement.tint,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                LinearProgressIndicator(
                                    progress = { achievement.progress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .clip(CircleShape),
                                    color = achievement.tint,
                                    trackColor = Color.Gray.copy(0.15f)
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "${(achievement.progress * 100).toInt()}%",
                                    color = TextSecondary,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
