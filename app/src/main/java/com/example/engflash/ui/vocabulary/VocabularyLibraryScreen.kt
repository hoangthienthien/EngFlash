package com.example.engflash.ui.vocabulary

import androidx.compose.foundation.background
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

// Xóa màu hardcode, chuyển vào trong Composable

data class SubjectItem(
    val name: String,
    val wordCount: Int,
    val unitCount: Int,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val iconBg: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VocabularyLibraryScreen(
    navController: NavController,
    viewModel: FlashcardViewModel
) {
    var searchQuery by remember { mutableStateOf("") }

    // ─── Theme Colors ───
    val PageBg = MaterialTheme.colorScheme.background
    val CardBg = MaterialTheme.colorScheme.surface
    val TextPrimary = MaterialTheme.colorScheme.onBackground
    val TextSecondary = MaterialTheme.colorScheme.onSurfaceVariant
    val PurplePrimary = MaterialTheme.colorScheme.primary
    val PurpleLight = MaterialTheme.colorScheme.primaryContainer

    val topics by viewModel.allTopics.collectAsState()

    val filteredTopics = topics.filter {
        it.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Thư Viện Chủ Đề",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = PurplePrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { /* Open drawer */ }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = PurplePrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { /* Search action */ }) {
                        Icon(Icons.Default.Search, contentDescription = "Tìm kiếm", tint = PurplePrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PageBg
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Routes.ADD_WORD) },
                containerColor = PurplePrimary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 16.dp, end = 8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Thêm từ mới", modifier = Modifier.size(28.dp))
            }
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
                    selected = true,
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PurplePrimary,
                        selectedTextColor = PurplePrimary,
                        indicatorColor = PurpleLight
                    ),
                    onClick = { /* Already here */ }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Style, contentDescription = "Flashcard") },
                    label = { Text("Luyện tập") },
                    selected = false,
                    onClick = {
                        navController.navigate(Routes.FLASHCARD_PLACEHOLDER)
                    }
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 32.dp, top = 8.dp)
        ) {
            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Tìm kiếm chủ đề của bạn...", color = TextSecondary) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(2.dp, shape = RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = CardBg,
                        unfocusedContainerColor = CardBg,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true
                )
            }

            // Subject Cards List
            items(filteredTopics) { topicName ->
                val wordCount by viewModel.getTopicWordCount(topicName).collectAsState(initial = 0)

                // Map topic name to icon and color
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
                            navController.navigate(Routes.vocabularyList(topicName))
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
                            Text(
                                text = topicName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = "$wordCount từ vựng",
                                fontSize = 14.sp,
                                color = TextSecondary
                            )
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

            // Daily Review Card
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
                                text = "Luyện tập hàng ngày",
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text(
                                text = "Học và ôn tập các từ vựng mới bằng Flashcard.",
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(bottom = 20.dp)
                            )
                            Button(
                                onClick = {
                                    navController.navigate(Routes.FLASHCARD_PLACEHOLDER)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface, contentColor = PurplePrimary),
                                shape = RoundedCornerShape(16.dp),
                                contentPadding = PaddingValues(horizontal = 32.dp, vertical = 12.dp),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                            ) {
                                Text(
                                    "Bắt đầu ôn tập",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
