package com.example.engflash.ui.vocabulary

import android.graphics.BitmapFactory
import android.net.Uri
import android.speech.tts.TextToSpeech
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.engflash.domain.model.Vocabulary
import com.example.engflash.ui.navigation.Routes
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

// Màu sắc sẽ được lấy từ MaterialTheme.colorScheme bên trong mỗi Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VocabularyListScreen(
    topicName: String,
    viewModel: FlashcardViewModel,
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // ─── Theme Colors ───
    val PageBg = MaterialTheme.colorScheme.background
    val CardBg = MaterialTheme.colorScheme.surface
    val TextPrimary = MaterialTheme.colorScheme.onBackground
    val TextSecondary = MaterialTheme.colorScheme.onSurfaceVariant
    val PurplePrimary = MaterialTheme.colorScheme.primary
    val PurpleLight = MaterialTheme.colorScheme.primaryContainer

    // Initialize TTS engine
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

    // Load vocabularies for the selected topic name when screen is opened
    LaunchedEffect(topicName) {
        viewModel.loadTopic(topicName)
    }

    // Vocabulary Set details depending on the topic
    val (setName, setDescription) = remember(topicName) {
        when (topicName.lowercase()) {
            "academic" -> Pair(
                "Từ vựng học thuật",
                "Làm chủ các thuật ngữ nâng cao cần thiết cho học thuật, nghiên cứu và công việc chuyên môn."
            )
            "business" -> Pair(
                "Từ vựng công sở",
                "Các từ vựng thiết yếu về tài chính, kinh tế, chiến lược kinh doanh và thương mại."
            )
            else -> Pair(
                "Từ vựng thông dụng",
                "Từ vựng phổ thông giúp nâng cao khả năng giao tiếp và đọc hiểu tiếng Anh hàng ngày."
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = topicName,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại", tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { /* Search */ }) {
                        Icon(Icons.Default.Search, contentDescription = "Tìm kiếm", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PageBg)
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
                    selected = true,
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PurplePrimary,
                        selectedTextColor = PurplePrimary,
                        indicatorColor = PurpleLight
                    ),
                    onClick = {
                        navController.navigate(Routes.VOCABULARY_PLACEHOLDER) {
                            popUpTo(Routes.VOCABULARY_PLACEHOLDER) { inclusive = true }
                        }
                    }
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Routes.ADD_WORD) },
                containerColor = PurplePrimary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 16.dp, end = 8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Thêm từ vựng", modifier = Modifier.size(28.dp))
            }
        },
        containerColor = PageBg
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PurplePrimary)
            }
        } else {
            var showDeleteDialog by remember { mutableStateOf<Vocabulary?>(null) }

            // Delete Confirmation Dialog — hoisted outside LazyColumn
            if (showDeleteDialog != null) {
                val vocabToDelete = showDeleteDialog!!
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = null },
                    title = { Text("Xoá từ vựng") },
                    text = { Text("Bạn có chắc chắn muốn xoá từ '${vocabToDelete.word}' khỏi bộ từ vựng không?") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                viewModel.deleteVocabulary(vocabToDelete.id)
                                showDeleteDialog = null
                            }
                        ) { Text("Xoá", color = Color.Red) }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = null }) { Text("Huỷ") }
                    }
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 32.dp, top = 8.dp)
            ) {
                // Header Banner
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        Text(
                            text = "BỘ TỪ VỰNG",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = PurplePrimary,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = setName,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        Text(
                            text = setDescription,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            lineHeight = 20.sp
                        )
                    }
                }

                // Vocabulary Items
                items(uiState.vocabularies) { vocab ->
                    VocabularyWordCard(
                        vocab = vocab,
                        onPlayAudio = {
                            tts?.speak(vocab.word, TextToSpeech.QUEUE_FLUSH, null, null)
                        },
                        onToggleFavorite = {
                            viewModel.toggleVocabularyFavorite(vocab.id, !vocab.isFavorite)
                        },
                        onEdit = {
                            navController.navigate(Routes.editWord(vocab.id))
                        },
                        onDelete = {
                            showDeleteDialog = vocab
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun VocabularyWordCard(
    vocab: Vocabulary,
    onPlayAudio: () -> Unit,
    onToggleFavorite: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    // ─── Theme Colors ───
    val PageBg = MaterialTheme.colorScheme.background
    val CardBg = MaterialTheme.colorScheme.surface
    val TextPrimary = MaterialTheme.colorScheme.onBackground
    val TextSecondary = MaterialTheme.colorScheme.onSurfaceVariant
    val PurplePrimary = MaterialTheme.colorScheme.primary
    val PurpleLight = MaterialTheme.colorScheme.primaryContainer

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, shape = RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header: Word and Part of Speech tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = vocab.word,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                // Part of Speech capsule tag and Actions
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val displayPos = when (vocab.partOfSpeech.uppercase()) {
                        "NOUN" -> "Danh từ"
                        "VERB" -> "Động từ"
                        "ADJECTIVE" -> "Tính từ"
                        "ADVERB" -> "Trạng từ"
                        else -> vocab.partOfSpeech
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(PurpleLight)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = displayPos,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PurplePrimary
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Sửa", tint = TextSecondary, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Xoá", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                    }
                }
            }

            // Subtitle: Phonetic & Audio Speaker Icon
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = vocab.phonetic,
                    fontSize = 14.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = "Phát âm",
                    tint = TextSecondary,
                    modifier = Modifier
                        .size(16.dp)
                        .clickable { onPlayAudio() }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Body: Meaning / definition
            Text(
                text = vocab.meaning,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = vocab.example,
                fontSize = 13.sp,
                color = TextSecondary,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Toggle Add/Remove button
            val isAdded = vocab.isFavorite
            Button(
                onClick = onToggleFavorite,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isAdded) MaterialTheme.colorScheme.primaryContainer else PurplePrimary,
                    contentColor = if (isAdded) PurplePrimary else MaterialTheme.colorScheme.onPrimary
                ),
                contentPadding = PaddingValues(vertical = 12.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (isAdded) Icons.Default.Check else Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isAdded) "Đã thêm vào Flashcard" else "Thêm vào Flashcard",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
