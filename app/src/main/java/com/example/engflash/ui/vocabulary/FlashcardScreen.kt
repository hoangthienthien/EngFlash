package com.example.engflash.ui.vocabulary

import android.graphics.BitmapFactory
import android.speech.tts.TextToSpeech
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.graphics.PathFillType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.engflash.domain.model.Vocabulary
import com.example.engflash.ui.navigation.Routes
import com.example.engflash.ui.theme.GradientEnd
import com.example.engflash.ui.theme.GradientMiddle
import com.example.engflash.ui.theme.GradientStart
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardScreen(
    navController: NavController,
    viewModel: FlashcardViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Initialize TTS
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    DisposableEffect(Unit) {
        val ttsEngine = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
            }
        }
        tts = ttsEngine
        onDispose {
            ttsEngine.stop()
            ttsEngine.shutdown()
        }
    }

    // Load vocabularies once
    LaunchedEffect(Unit) {
        viewModel.loadTopicOrFilter(uiState.selectedFilter)
    }

    // Session progress tracking
    var initialCount by remember(uiState.selectedFilter) { mutableIntStateOf(0) }
    val remainingCount = uiState.vocabularies.size
    LaunchedEffect(remainingCount) {
        if (remainingCount > initialCount) {
            initialCount = remainingCount
        }
    }
    val completedCount = if (initialCount > remainingCount) initialCount - remainingCount else 0
    val progressPercent = if (initialCount > 0) (completedCount * 100) / initialCount else 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "EngFlash",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 20.sp
                    )
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = FireIcon,
                                contentDescription = "Streak",
                                tint = Color(0xFFFF6B35),
                                modifier = Modifier.size(18.dp)
                            )
                            Text("12", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
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
                    label = { Text("Trang chủ", fontSize = 11.sp) },
                    selected = false,
                    onClick = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.HOME) { inclusive = true }
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Book, contentDescription = "Từ vựng") },
                    label = { Text("Từ vựng", fontSize = 11.sp) },
                    selected = false,
                    onClick = {
                        navController.navigate(Routes.VOCABULARY_PLACEHOLDER) {
                            popUpTo(Routes.VOCABULARY_PLACEHOLDER) { inclusive = true }
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Style, contentDescription = "Luyện tập") },
                    label = { Text("Luyện tập", fontSize = 11.sp) },
                    selected = true,
                    onClick = { /* Already here */ }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.School, contentDescription = "Ngữ pháp") },
                    label = { Text("Ngữ pháp", fontSize = 11.sp) },
                    selected = false,
                    onClick = {
                        navController.navigate(Routes.GRAMMAR_TOPIC_LIST)
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Cá nhân") },
                    label = { Text("Cá nhân", fontSize = 11.sp) },
                    selected = false,
                    onClick = {
                        navController.navigate(Routes.PROFILE)
                    }
                )
            }
        }
    ) { paddingValues ->
        val vocabularies = uiState.vocabularies
        val currentIndex = uiState.currentIndex

        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (vocabularies.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Topic Selector Chips at the top even when empty
                val dbTopics by viewModel.allTopics.collectAsState()
                val filterList = remember(dbTopics) {
                    listOf("All", "Favorites") + dbTopics
                }

                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filterList) { filter ->
                        val isSelected = uiState.selectedFilter == filter
                        val containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        
                        Surface(
                            onClick = { viewModel.loadTopicOrFilter(filter) },
                            color = containerColor,
                            contentColor = contentColor,
                            shape = RoundedCornerShape(16.dp),
                            border = if (!isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant) else null,
                            modifier = Modifier.height(36.dp)
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = when (filter) {
                                        "All" -> "Tất cả"
                                        "Favorites" -> "Yêu thích"
                                        else -> filter
                                    },
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DoneAll,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Tất cả đã hoàn thành!",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Bạn đã ôn tập xong tất cả các từ trong danh mục này. Hãy chọn danh mục khác hoặc thêm từ mới để tiếp tục học tập nhé!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            val currentVocab = vocabularies[currentIndex]

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Topic Selector Chips at the top
                    val dbTopics by viewModel.allTopics.collectAsState()
                    val filterList = remember(dbTopics) {
                        listOf("All", "Favorites") + dbTopics
                    }

                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filterList) { filter ->
                            val isSelected = uiState.selectedFilter == filter
                            val containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            
                            Surface(
                                onClick = { viewModel.loadTopicOrFilter(filter) },
                                color = containerColor,
                                contentColor = contentColor,
                                shape = RoundedCornerShape(16.dp),
                                border = if (!isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant) else null,
                                modifier = Modifier.height(36.dp)
                            ) {
                                Box(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = when (filter) {
                                            "All" -> "Tất cả"
                                            "Favorites" -> "Yêu thích"
                                            else -> filter
                                        },
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // ─── Daily Progress ───────────────────────────
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Tiến độ học tập",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.Gray
                            )
                            Text(
                                "$completedCount / $initialCount thẻ",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(56.dp)
                        ) {
                            CircularProgressIndicator(
                                progress = { progressPercent.toFloat() / 100f },
                                modifier = Modifier.fillMaxSize(),
                                strokeWidth = 6.dp,
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            )
                            Text(
                                "$progressPercent%",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // ─── 3D Flashcard ─────────────────────────────
                val rotation by animateFloatAsState(
                    targetValue = if (uiState.isFlipped) 180f else 0f,
                    animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
                    label = "cardFlipRotation"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.95f)
                            .fillMaxHeight(0.85f)
                            .graphicsLayer {
                                rotationY = rotation
                                cameraDistance = 8 * density
                            }
                            .clickable { viewModel.flipCard() }
                            .shadow(
                                elevation = 6.dp,
                                shape = RoundedCornerShape(28.dp),
                                clip = false
                            ),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        if (rotation < 90f) {
                            // FRONT SIDE
                            val hasImage = !currentVocab.imageUrl.isNullOrBlank()

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(20.dp)
                            ) {
                                // Category Tag at top-left
                                val displayPos = when (currentVocab.partOfSpeech.uppercase()) {
                                    "NOUN" -> "Danh từ"
                                    "VERB" -> "Động từ"
                                    "ADJECTIVE" -> "Tính từ"
                                    "ADVERB" -> "Trạng từ"
                                    else -> currentVocab.partOfSpeech
                                }
                                Text(
                                    text = displayPos,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                    fontSize = 12.sp,
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .background(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                )

                                if (hasImage) {
                                    // Image Layout: Word top-ish, Image bottom-ish
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Spacer(modifier = Modifier.height(32.dp))

                                        // Word & Pronounce button row
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = currentVocab.word,
                                                style = MaterialTheme.typography.displayMedium,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                textAlign = TextAlign.Center
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            IconButton(
                                                onClick = {
                                                    tts?.speak(currentVocab.word, TextToSpeech.QUEUE_FLUSH, null, null)
                                                },
                                                modifier = Modifier
                                                    .background(
                                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                                        CircleShape
                                                    )
                                                    .size(44.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                                    contentDescription = "Phát âm",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                        }

                                        if (currentVocab.phonetic.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = currentVocab.phonetic,
                                                style = MaterialTheme.typography.titleMedium,
                                                color = Color.Gray,
                                                textAlign = TextAlign.Center
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        // Image Space
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(180.dp)
                                                .clip(RoundedCornerShape(20.dp))
                                                .background(Color(0xFFF5F5FA)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            NetworkImage(
                                                url = currentVocab.imageUrl!!,
                                                contentDescription = currentVocab.word,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }

                                        Text(
                                            "Chạm để lật thẻ",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.LightGray
                                        )
                                    }
                                } else {
                                    // Text Centered Layout: Everything centered vertically
                                    Column(
                                        modifier = Modifier.align(Alignment.Center),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = currentVocab.word,
                                                style = MaterialTheme.typography.displayMedium,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                textAlign = TextAlign.Center
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            IconButton(
                                                onClick = {
                                                    tts?.speak(currentVocab.word, TextToSpeech.QUEUE_FLUSH, null, null)
                                                },
                                                modifier = Modifier
                                                    .background(
                                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                                        CircleShape
                                                    )
                                                    .size(48.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                                    contentDescription = "Phát âm",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(26.dp)
                                                )
                                            }
                                        }

                                        if (currentVocab.phonetic.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = currentVocab.phonetic,
                                                style = MaterialTheme.typography.titleLarge,
                                                color = Color.Gray,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }

                                    // Hint text at bottom center
                                    Text(
                                        text = "Chạm để lật thẻ",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.LightGray,
                                        modifier = Modifier.align(Alignment.BottomCenter)
                                    )
                                }

                                // Remove from flashcard button overlay top-right
                                IconButton(
                                    onClick = { viewModel.removeFromFlashcard() },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .background(Color(0xFFE53935).copy(alpha = 0.15f), CircleShape)
                                        .size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = CloseIcon,
                                        contentDescription = "Xóa khỏi Flashcard",
                                        tint = Color(0xFFE53935),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        } else {
                            // BACK SIDE
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer {
                                        rotationY = 180f
                                    }
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = currentVocab.word,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color.Gray,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    IconButton(
                                        onClick = {
                                            tts?.speak(currentVocab.word, TextToSpeech.QUEUE_FLUSH, null, null)
                                        },
                                        modifier = Modifier
                                            .background(
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                                CircleShape
                                            )
                                            .size(30.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                            contentDescription = "Phát âm",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = currentVocab.meaning,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = "Ví dụ:",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = currentVocab.example.ifBlank { "Không có ví dụ." },
                                            style = MaterialTheme.typography.bodyLarge,
                                            lineHeight = 22.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                Text(
                                    "Chạm để lật lại",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.LightGray
                                )
                            }
                        }
                    }
                }

                // ─── Actions Buttons & Double Arrows ──────────
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { viewModel.previousCard() }
                        ) {
                            Text("TRƯỚC", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }

                        // Circular Spaced Repetition Buttons: 20 (Yếu), 50 (Được), 80 (Giỏi)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                        ) {
                            // Circular Button 20 (Yếu)
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFEE2E2))
                                        .border(1.5.dp, Color(0xFFFCA5A5), CircleShape)
                                        .clickable { viewModel.reviewCard(currentVocab.id, "yếu") },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "20",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 16.sp,
                                        color = Color(0xFFDC2626)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Yếu", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            }

                            // Circular Button 50 (Được)
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFEFF6FF))
                                        .border(1.5.dp, Color(0xFF93C5FD), CircleShape)
                                        .clickable { viewModel.reviewCard(currentVocab.id, "được") },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "50",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 16.sp,
                                        color = Color(0xFF1D4ED8)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Được", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            }

                            // Circular Button 80 (Giỏi)
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFECFDF5))
                                        .border(1.5.dp, Color(0xFF6EE7B7), CircleShape)
                                        .clickable { viewModel.reviewCard(currentVocab.id, "giỏi") },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "80",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 16.sp,
                                        color = Color(0xFF047857)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Giỏi", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            }
                        }

                        TextButton(
                            onClick = { viewModel.nextCard() }
                        ) {
                            Text("KẾ TIẾP", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NetworkImage(
    url: String,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    var bitmap by remember(url) { mutableStateOf<android.graphics.Bitmap?>(null) }
    LaunchedEffect(url) {
        kotlin.runCatching {
            withContext(Dispatchers.IO) {
                if (url.startsWith("http://") || url.startsWith("https://")) {
                    val connection = java.net.URL(url).openConnection() as java.net.HttpURLConnection
                    connection.doInput = true
                    connection.connect()
                    connection.inputStream.use {
                        BitmapFactory.decodeStream(it)
                    }
                } else {
                    BitmapFactory.decodeFile(url)
                }
            }
        }.onSuccess {
            bitmap = it
        }
    }
    bitmap?.let {
        Image(
            bitmap = it.asImageBitmap(),
            contentDescription = contentDescription,
            modifier = modifier
        )
    }
}

// ── SVG Fire Icon ─────────────────────────────────────────────────────────
private val FireIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "Fire",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = androidx.compose.ui.graphics.SolidColor(Color(0xFFFF6B35)),
            fillAlpha = 1f,
            pathFillType = PathFillType.NonZero
        ) {
            // Outer flame body
            moveTo(12f, 23f)
            curveTo(7.0f, 23f, 3f, 19.0f, 3f, 14f)
            curveTo(3f, 11.5f, 4.0f, 9.2f, 5.7f, 7.5f)
            curveTo(6.0f, 7.1f, 6.0f, 6.6f, 5.8f, 6.1f)
            curveTo(5.3f, 4.9f, 5.2f, 3.6f, 5.5f, 2.3f)
            curveTo(5.6f, 1.8f, 6.1f, 1.5f, 6.6f, 1.7f)
            curveTo(8.3f, 2.4f, 9.7f, 3.6f, 10.5f, 5.1f)
            curveTo(11.0f, 4.7f, 11.5f, 4.4f, 12f, 4.1f)
            curveTo(12.3f, 2.7f, 13.1f, 1.5f, 14.2f, 0.7f)
            curveTo(14.7f, 0.3f, 15.4f, 0.6f, 15.5f, 1.2f)
            curveTo(15.8f, 2.7f, 15.7f, 4.3f, 15.1f, 5.7f)
            curveTo(17.3f, 7.3f, 19f, 9.8f, 19f, 12.5f)
            curveTo(19f, 13.5f, 18.8f, 14.5f, 18.4f, 15.4f)
            curveTo(19.4f, 14.6f, 20f, 13.4f, 20f, 12f)
            curveTo(20f, 11.5f, 20.5f, 11.1f, 21f, 11.3f)
            curveTo(22.3f, 11.9f, 23f, 13.2f, 23f, 14.5f)
            curveTo(23f, 19.2f, 18.0f, 23f, 12f, 23f)
            close()
        }
        path(
            fill = androidx.compose.ui.graphics.SolidColor(Color(0xFFFFC107)),
            fillAlpha = 1f,
            pathFillType = PathFillType.NonZero
        ) {
            // Inner flame highlight
            moveTo(12f, 20f)
            curveTo(9.2f, 20f, 7f, 17.8f, 7f, 15f)
            curveTo(7f, 13.5f, 7.7f, 12.1f, 8.9f, 11.2f)
            curveTo(9.1f, 11.0f, 9.1f, 10.7f, 9.0f, 10.5f)
            curveTo(8.6f, 9.7f, 8.5f, 8.8f, 8.7f, 7.9f)
            curveTo(10.0f, 8.9f, 10.8f, 10.3f, 11f, 11.9f)
            curveTo(11.5f, 11.4f, 12f, 10.7f, 12.3f, 10f)
            curveTo(13.4f, 10.9f, 14f, 12.3f, 14f, 13.8f)
            curveTo(14.3f, 13.3f, 14.5f, 12.7f, 14.5f, 12.1f)
            curveTo(15.4f, 12.9f, 16f, 14.1f, 16f, 15.4f)
            curveTo(16f, 17.9f, 14.2f, 20f, 12f, 20f)
            close()
        }
    }.build()
}

// ── SVG Close (X) Icon ────────────────────────────────────────────────────
private val CloseIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "Close",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = null,
            stroke = androidx.compose.ui.graphics.SolidColor(Color.Black),
            strokeLineWidth = 2.5f,
            strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round
        ) {
            // First diagonal: top-left to bottom-right
            moveTo(6f, 6f)
            lineTo(18f, 18f)
        }
        path(
            fill = null,
            stroke = androidx.compose.ui.graphics.SolidColor(Color.Black),
            strokeLineWidth = 2.5f,
            strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round
        ) {
            // Second diagonal: top-right to bottom-left
            moveTo(18f, 6f)
            lineTo(6f, 18f)
        }
    }.build()
}
