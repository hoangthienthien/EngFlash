package com.example.engflash.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.engflash.ui.navigation.Routes
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.engflash.domain.model.*

// Custom colors (không phụ thuộc theme)
private val GreenSuccess = Color(0xFF22C55E)
private val OrangeWarm = Color(0xFFF59E0B)
private val PurpleDarkStatic = Color(0xFF3D1F8E) // Dùng cho card đặc biệt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel,
    onNavigateToVocabulary: () -> Unit,
    onNavigateToGrammar: () -> Unit,
    onNavigateToFlashcard: () -> Unit,
    onLogout: () -> Unit
) {
    val currentUser = viewModel.getCurrentUser()
    val displayName = currentUser?.displayName?.ifEmpty { "User" } ?: "User"
    val totalWords by viewModel.totalWords.collectAsState()
    val learnedWords by viewModel.learnedWords.collectAsState()
    val favoriteCount by viewModel.favoriteCount.collectAsState()
    val vocabTopics by viewModel.vocabTopics.collectAsState()
    val recentlyAdded by viewModel.recentlyAdded.collectAsState()
    val stats by viewModel.vocabStats.collectAsState()

    // ─── Theme Colors ───
    val PageBg = MaterialTheme.colorScheme.background
    val PurplePrimary = MaterialTheme.colorScheme.primary
    val PurpleLight = MaterialTheme.colorScheme.primaryContainer
    val PurpleDark = MaterialTheme.colorScheme.primary // Dùng primary thay cho PurpleDark để tự động thích ứng
    val TextPrimary = MaterialTheme.colorScheme.onBackground
    val TextSecondary = MaterialTheme.colorScheme.onSurfaceVariant
    val CardBg = MaterialTheme.colorScheme.surface
    val StreakBg = MaterialTheme.colorScheme.surfaceVariant

    val greetingText = remember {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        when {
            hour < 12 -> "Chào buổi sáng"
            hour < 18 -> "Chào buổi chiều"
            else -> "Chào buổi tối"
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = CardBg,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Trang chủ") },
                    label = { Text("Trang chủ") },
                    selected = true,
                    onClick = {}
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Book, contentDescription = "Từ vựng") },
                    label = { Text("Từ vựng") },
                    selected = false,
                    onClick = { navController.navigate(Routes.VOCABULARY_PLACEHOLDER) }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Style, contentDescription = "Flashcard") },
                    label = { Text("Luyện tập") },
                    selected = false,
                    onClick = { navController.navigate(Routes.FLASHCARD_PLACEHOLDER) }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.School, contentDescription = "Ngữ pháp") },
                    label = { Text("Ngữ pháp") },
                    selected = false,
                    onClick = { navController.navigate(Routes.GRAMMAR_TOPIC_LIST) }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Cá nhân") },
                    label = { Text("Cá nhân") },
                    selected = false,
                    onClick = { navController.navigate(Routes.PROFILE) }
                )
            }
        },
        containerColor = PageBg
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp)
        ) {
            // ─── Top Bar ───────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Bolt,
                        contentDescription = null,
                        tint = PurplePrimary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "EngFlash",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp,
                        color = TextPrimary
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = { navController.navigate(Routes.SEARCH) }) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Tìm kiếm",
                            tint = TextPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(PurplePrimary)
                            .clickable { navController.navigate(Routes.PROFILE) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            displayName.first().uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // ─── Greeting ──────────────────────────────────
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    "$greetingText, $displayName!",
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = TextPrimary
                )
                Text(
                    "Kiên trì là chìa khóa để làm chủ ngôn ngữ.\nSẵn sàng cho thử thách hôm nay?",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }

            Spacer(Modifier.height(16.dp))

            // ─── 12 Day Streak Card ────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = StreakBg),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Fire icon circle
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(PurpleLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = PurplePrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        val streak by viewModel.currentStreak.collectAsStateWithLifecycle()
                        Text(
                            "Chuỗi học $streak ngày",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = TextPrimary
                        )
                        Text(
                            when {
                                streak >= 7 -> "Tuyệt vời! Bạn đã duy trì chuỗi học cả tuần!"
                                streak >= 3 -> "Đang tiến bộ tốt, hãy tiếp tục nhé!"
                                else -> "Hãy duy trì chuỗi học mỗi ngày nhé!"
                            },
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    }

                }
            }

            Spacer(Modifier.height(16.dp))

            // ─── Continue Learning Card ────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clickable { onNavigateToGrammar() },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(PurpleLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.School, null, tint = PurplePrimary, modifier = Modifier.size(22.dp))
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = GreenSuccess.copy(alpha = 0.12f)
                        ) {
                            Text(
                                "Bài học mới",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                color = GreenSuccess,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("Ngữ pháp tiếng Anh", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                    Text(
                        "Làm chủ các thì, cấu trúc & quy tắc ngữ pháp rõ ràng",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                    Spacer(Modifier.height(14.dp))
                    Button(
                        onClick = { onNavigateToGrammar() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                        contentPadding = PaddingValues(vertical = 14.dp)
                    ) {
                        Text("Bắt đầu học", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Spacer(Modifier.width(6.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // ─── Daily Review Card ─────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clickable { onNavigateToFlashcard() },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(PurpleLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Replay, null, tint = PurplePrimary, modifier = Modifier.size(22.dp))
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = OrangeWarm.copy(alpha = 0.12f)
                        ) {
                            Text(
                                "$favoriteCount từ cần ôn",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                color = OrangeWarm,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("Ôn tập Flashcard", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                    Text(
                        "Ôn tập từ vựng đã lưu bằng thẻ lật 3D thông minh",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(Modifier.height(14.dp))
                    OutlinedButton(
                        onClick = { onNavigateToFlashcard() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(vertical = 14.dp)
                    ) {
                        Text("Ôn tập ngay", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = PurplePrimary)
                        Spacer(Modifier.width(6.dp))
                        Icon(Icons.Default.AccessTime, null, tint = PurplePrimary, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ─── Explore Topics ────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Khám phá chủ đề", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                Text(
                    "Xem tất cả",
                    color = PurplePrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onNavigateToVocabulary() }
                )
            }

            Spacer(Modifier.height(12.dp))

            // Horizontal topic cards
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                vocabTopics.forEach { topicName ->
                    val wordCount by viewModel.getTopicWordCount(topicName)
                        .collectAsState(initial = 0)
                    TopicChipCard(
                        name = topicName,
                        wordCount = wordCount,
                        onClick = { navController.navigate(Routes.vocabularyList(topicName)) }
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // ─── Vocabulary Mastery ────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = PurpleDark),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Mức độ", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            Text("Thông thạo", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Tiến trình học tập từ vựng của bạn",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("${stats.totalCount}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 28.sp)
                            Text("TỔNG SỐ", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(4.dp))
                            Text("TỪ VỰNG", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Progress bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Từ đã thuộc (Giỏi)", color = Color.White, fontSize = 13.sp)
                        Text("${stats.masteredCount} / ${stats.totalCount}", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                    }
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { if (stats.totalCount > 0) stats.masteredCount.toFloat() / stats.totalCount else 0f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Color.White,
                        trackColor = Color.White.copy(alpha = 0.2f),
                    )

                    Spacer(Modifier.height(20.dp))

                    // Stats row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        MasteryStatItem(value = "${stats.unlearnedCount}", label = "Chưa thuộc")
                        MasteryStatItem(value = "${stats.retentionRate}%", label = "Ghi nhớ")
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ─── Recently Added ────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Từ vựng mới thêm gần đây", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                Text(
                    "Xem tất cả",
                    color = PurplePrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onNavigateToVocabulary() }
                )
            }

            Spacer(Modifier.height(8.dp))

            for (vocab in recentlyAdded) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(PurpleLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                vocab.word.first().uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = PurplePrimary,
                                fontSize = 18.sp
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(vocab.word, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = TextPrimary)
                            Text(vocab.meaning, fontSize = 12.sp, color = TextSecondary, maxLines = 1)
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = PurpleLight
                        ) {
                            Text(
                                vocab.topic,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                color = PurplePrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

// ─── Topic Chip Card ───────────────────────────────────
@Composable
private fun TopicChipCard(
    name: String,
    wordCount: Int,
    onClick: () -> Unit
) {
    val CardBg = MaterialTheme.colorScheme.surface
    val PurplePrimary = MaterialTheme.colorScheme.primary
    val PurpleLight = MaterialTheme.colorScheme.primaryContainer
    val TextPrimary = MaterialTheme.colorScheme.onBackground
    val TextSecondary = MaterialTheme.colorScheme.onSurfaceVariant

    Card(
        modifier = Modifier
            .width(110.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(PurpleLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.School,
                    contentDescription = null,
                    tint = PurplePrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextPrimary)
            Text("$wordCount từ", fontSize = 11.sp, color = TextSecondary)
        }
    }
}

// ─── Mastery Stat Item ─────────────────────────────────
@Composable
private fun MasteryStatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Text(label, color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
    }
}
