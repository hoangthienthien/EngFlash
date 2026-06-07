package com.example.engflash.ui.vocabulary

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.engflash.ui.navigation.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeTopicListScreen(
    navController: NavController,
    viewModel: FlashcardViewModel
) {
    // ─── Theme Colors ───
    val PageBg = MaterialTheme.colorScheme.background
    val CardBg = MaterialTheme.colorScheme.surface
    val TextPrimary = MaterialTheme.colorScheme.onBackground
    val TextSecondary = MaterialTheme.colorScheme.onSurfaceVariant
    val PurplePrimary = MaterialTheme.colorScheme.primary
    val PurpleLight = MaterialTheme.colorScheme.primaryContainer

    val flashcardTopics by viewModel.flashcardTopics.collectAsState()

    var showConfirmDialog by remember { mutableStateOf(false) }
    var topicToReset by remember { mutableStateOf("") }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Làm lại chủ đề?") },
            text = { Text("Tất cả tiến trình ôn tập của chủ đề \"$topicToReset\" sẽ được đặt lại. Bạn có chắc chắn muốn làm lại?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetTopicProgress(topicToReset)
                        showConfirmDialog = false
                        navController.navigate(Routes.flashcardPractice(topicToReset))
                    }
                ) {
                    Text("Xác nhận", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Luyện Tập",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = PurplePrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { /* Menu */ }) {
                        Icon(Icons.Default.Style, contentDescription = "Luyện tập", tint = PurplePrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PageBg
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Trang chủ") },
                    label = { Text("Trang chủ") },
                    selected = false,
                    onClick = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.HOME) { inclusive = true }
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Book, contentDescription = "Từ vựng") },
                    label = { Text("Từ vựng") },
                    selected = false,
                    onClick = {
                        navController.navigate(Routes.VOCABULARY_PLACEHOLDER) {
                            popUpTo(Routes.VOCABULARY_PLACEHOLDER) { inclusive = true }
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Style, contentDescription = "Luyện tập") },
                    label = { Text("Luyện tập") },
                    selected = true,
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PurplePrimary,
                        selectedTextColor = PurplePrimary,
                        indicatorColor = PurpleLight
                    ),
                    onClick = { /* Already here */ }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.School, contentDescription = "Ngữ pháp") },
                    label = { Text("Ngữ pháp") },
                    selected = false,
                    onClick = {
                        navController.navigate(Routes.GRAMMAR_TOPIC_LIST)
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Cá nhân") },
                    label = { Text("Cá nhân") },
                    selected = false,
                    onClick = {
                        navController.navigate(Routes.PROFILE)
                    }
                )
            }
        },
        containerColor = PageBg
    ) { paddingValues ->
        if (flashcardTopics.isEmpty()) {
            // ─── Empty State ───
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Style,
                        contentDescription = null,
                        tint = PurplePrimary.copy(alpha = 0.4f),
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        "Chưa có từ nào trong Flashcard",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Hãy vào phần Từ vựng và bấm \"Thêm vào Flashcard\" để bắt đầu luyện tập nhé!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = {
                            navController.navigate(Routes.VOCABULARY_PLACEHOLDER) {
                                popUpTo(Routes.VOCABULARY_PLACEHOLDER) { inclusive = true }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PurplePrimary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(horizontal = 32.dp, vertical = 14.dp)
                    ) {
                        Icon(Icons.Default.Book, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Đi tới Từ vựng", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            // ─── Topic List ───
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 32.dp, top = 8.dp)
            ) {
                // Header
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        Text(
                            text = "DANH SÁCH CHỦ ĐỀ",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = PurplePrimary,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Flashcard của bạn",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        Text(
                            text = "Chọn chủ đề để bắt đầu ôn tập từ vựng đã thêm vào Flashcard.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            lineHeight = 20.sp
                        )
                    }
                }

                // Topic Cards
                items(flashcardTopics) { topicName ->
                    val progressState by viewModel.getTopicFlashcardProgress(topicName).collectAsState(initial = Pair(0, 0))
                    val masteredCount = progressState.first
                    val wordCount = progressState.second
                    val progress = if (wordCount > 0) masteredCount.toFloat() / wordCount.toFloat() else 0f

                    // Map topic name to icon and color (same as VocabularyLibraryScreen)
                    val (icon, iconBg) = remember(topicName) {
                        when (topicName.lowercase(java.util.Locale.ROOT)) {
                            "academic" -> Pair(Icons.Default.School, PurplePrimary)
                            "business" -> Pair(Icons.Default.BusinessCenter, Color(0xFF8C76EC))
                            "general" -> Pair(Icons.Default.ChatBubble, Color(0xFFB5A9F0))
                            "travel" -> Pair(Icons.Default.Flight, Color(0xFFFF9F43))
                            "technology" -> Pair(Icons.Default.Computer, Color(0xFF10AC84))
                            else -> Pair(Icons.Default.Folder, Color(0xFF5E3CB3))
                        }
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                navController.navigate(Routes.flashcardPractice(topicName))
                            },
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBg),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(iconBg.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = iconBg,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = topicName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = TextPrimary
                                    )
                                    if (wordCount > 0 && masteredCount == wordCount) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFF10AC84).copy(alpha = 0.15f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = null,
                                                    tint = Color(0xFF10AC84),
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Text(
                                                    text = "Hoàn thành",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF10AC84)
                                                )
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (wordCount > 0) "$masteredCount/$wordCount từ đạt mức Giỏi" else "0 từ trong Flashcard",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (wordCount > 0 && masteredCount == wordCount) Color(0xFF10AC84) else TextSecondary
                                )
                                if (wordCount > 0) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    LinearProgressIndicator(
                                        progress = { progress },
                                        modifier = Modifier
                                            .fillMaxWidth(0.9f)
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp)),
                                        color = if (progress == 1f) Color(0xFF10AC84) else PurplePrimary,
                                        trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .border(1.dp, PurplePrimary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                            .background(PurplePrimary.copy(alpha = 0.05f))
                                            .clickable {
                                                topicToReset = topicName
                                                showConfirmDialog = true
                                            }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = null,
                                            tint = PurplePrimary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Làm lại",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PurplePrimary
                                        )
                                    }
                                }
                            }

                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                // Instruction card at the bottom
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = PurplePrimary)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(PurplePrimary, Color(0xFF7854F7))
                                    )
                                )
                                .padding(24.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "💡 Mẹo luyện tập",
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                Text(
                                    text = "Thêm từ mới vào Flashcard từ phần Từ vựng. Các từ sẽ được giữ lại để bạn ôn tập bất cứ lúc nào!",
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
