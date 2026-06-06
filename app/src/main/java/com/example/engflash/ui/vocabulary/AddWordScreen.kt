package com.example.engflash.ui.vocabulary

import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

private val PageBg @Composable get() = MaterialTheme.colorScheme.background
private val CardBg @Composable get() = MaterialTheme.colorScheme.surface
private val TextPrimary @Composable get() = MaterialTheme.colorScheme.onBackground
private val TextSecondary @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
private val PurplePrimary @Composable get() = MaterialTheme.colorScheme.primary
private val PurpleLight @Composable get() = MaterialTheme.colorScheme.primaryContainer
private val BorderColor @Composable get() = MaterialTheme.colorScheme.outlineVariant

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddWordScreen(
    navController: NavController,
    viewModel: FlashcardViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var word by remember { mutableStateOf("") }
    var phonetic by remember { mutableStateOf("") }
    var definition by remember { mutableStateOf("") }
    var example by remember { mutableStateOf("") }

    var isFetchingPhonetic by remember { mutableStateOf(false) }

    // Subjects loaded from DB
    val dbTopics by viewModel.allTopics.collectAsState()
    var customSubjects by remember { mutableStateOf(listOf<String>()) }

    val subjectsList = remember(dbTopics, customSubjects) {
        (dbTopics + customSubjects).distinct()
    }

    var selectedSubject by remember { mutableStateOf("") }
    var selectedPartOfSpeech by remember { mutableStateOf("NOUN") }
    var showNewSubjectDialog by remember { mutableStateOf(false) }

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // Auto-select first subject when list loads
    LaunchedEffect(subjectsList) {
        if (subjectsList.isNotEmpty() && (selectedSubject.isEmpty() || !subjectsList.contains(selectedSubject))) {
            selectedSubject = subjectsList.first()
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    // Default header image from assets
    val defaultHeaderBitmap = remember {
        try {
            context.assets.open("add_word_header.png").use {
                BitmapFactory.decodeStream(it)
            }
        } catch (e: Exception) {
            null
        }
    }

    // Picked image bitmap
    val pickedBitmap = remember(selectedImageUri) {
        if (selectedImageUri != null) {
            try {
                context.contentResolver.openInputStream(selectedImageUri!!)?.use {
                    BitmapFactory.decodeStream(it)
                }
            } catch (e: Exception) {
                null
            }
        } else null
    }

    // API Phonetic Auto-Fetch logic
    fun fetchPhonetic(wordToFetch: String) {
        if (wordToFetch.isBlank()) return
        isFetchingPhonetic = true
        scope.launch(Dispatchers.IO) {
            try {
                val urlStr = "https://api.dictionaryapi.dev/api/v2/entries/en/${wordToFetch.trim().lowercase()}"
                val connection = java.net.URL(urlStr).openConnection() as java.net.HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 4000
                connection.readTimeout = 4000
                
                val responseCode = connection.responseCode
                if (responseCode == 200) {
                    val jsonText = connection.inputStream.bufferedReader().use { it.readText() }
                    val regex = "\"phonetic\"\\s*:\\s*\"([^\"]+)\"".toRegex()
                    val match = regex.find(jsonText)
                    var foundPhonetic = match?.groups?.get(1)?.value ?: ""
                    
                    if (foundPhonetic.isEmpty()) {
                        val regexText = "\"text\"\\s*:\\s*\"([^\"]+)\"".toRegex()
                        val matches = regexText.findAll(jsonText)
                        for (m in matches) {
                            val txt = m.groups[1]?.value ?: ""
                            if (txt.startsWith("/") && txt.endsWith("/")) {
                                foundPhonetic = txt
                                break
                            }
                        }
                    }
                    
                    withContext(Dispatchers.Main) {
                        if (foundPhonetic.isNotEmpty()) {
                            phonetic = foundPhonetic
                            Toast.makeText(context, "Đã lấy phiên âm thành công!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Không tìm thấy ký tự phiên âm IPA.", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Không tìm thấy thông tin cho từ này.", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Không thể kết nối API phiên âm.", Toast.LENGTH_SHORT).show()
                }
            } finally {
                withContext(Dispatchers.Main) {
                    isFetchingPhonetic = false
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Thêm Từ Vựng Mới",
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(PurplePrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(Modifier.width(16.dp))
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ─── Header illustration (tap to pick image) ────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { imagePickerLauncher.launch("image/*") }
            ) {
                val displayBitmap = pickedBitmap ?: defaultHeaderBitmap
                if (displayBitmap != null) {
                    Image(
                        bitmap = displayBitmap.asImageBitmap(),
                        contentDescription = "Illustration",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(PurpleLight)
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f)),
                                startY = 80f
                            )
                        )
                )
                Text(
                    text = "Lưu giữ từ vựng để ôn tập mỗi ngày.",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                )

                // Camera hint
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .size(32.dp)
                        .background(Color.Black.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = "Chọn ảnh",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Remove picked image button
                if (pickedBitmap != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(10.dp)
                            .size(32.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            .clickable { selectedImageUri = null },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Xóa ảnh",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // ─── New Word ──────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Từ mới", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                OutlinedTextField(
                    value = word,
                    onValueChange = { word = it },
                    placeholder = { Text("Ví dụ: Serendipity", color = TextSecondary.copy(alpha = 0.5f)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = outlinedFieldColors()
                )
            }

            // ─── Phonetic ──────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Phiên âm", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = phonetic,
                        onValueChange = { phonetic = it },
                        placeholder = { Text("Ví dụ: /ˌserənˈdipədē/", color = TextSecondary.copy(alpha = 0.5f)) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = outlinedFieldColors()
                    )
                    Button(
                        onClick = { fetchPhonetic(word) },
                        enabled = word.isNotBlank() && !isFetchingPhonetic,
                        colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(56.dp)
                    ) {
                        if (isFetchingPhonetic) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(20.dp))
                        } else {
                            Text("Lấy IPA", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }

            // ─── Definition ────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Nghĩa của từ / Định nghĩa", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                OutlinedTextField(
                    value = definition,
                    onValueChange = { definition = it },
                    placeholder = {
                        Text(
                            "Ví dụ: Sự tình cờ may mắn",
                            color = TextSecondary.copy(alpha = 0.5f)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 4,
                    colors = outlinedFieldColors()
                )
            }

            // ─── Example Sentence ──────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Ví dụ minh họa", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                OutlinedTextField(
                    value = example,
                    onValueChange = { example = it },
                    placeholder = {
                        Text(
                            "Ví dụ: We found the place by pure serendipity.",
                            color = TextSecondary.copy(alpha = 0.5f)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 3,
                    colors = outlinedFieldColors()
                )
            }

            // ─── Select Subject ────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Chọn chủ đề", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)

                if (subjectsList.isEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Chưa có chủ đề nào",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                        DottedAddButton(onClick = { showNewSubjectDialog = true })
                    }
                } else {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        subjectsList.forEach { subject ->
                            SubjectChip(
                                name = subject,
                                selected = selectedSubject == subject,
                                onClick = { selectedSubject = subject }
                            )
                        }
                        DottedAddButton(onClick = { showNewSubjectDialog = true })
                    }
                }
            }

            // ─── Select Part of Speech ────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Chọn loại từ", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                val partsOfSpeech = listOf(
                    "NOUN" to "Danh từ",
                    "VERB" to "Động từ",
                    "ADJECTIVE" to "Tính từ",
                    "ADVERB" to "Trạng từ"
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    partsOfSpeech.forEach { pair ->
                        SubjectChip(
                            name = pair.second,
                            selected = selectedPartOfSpeech == pair.first,
                            onClick = { selectedPartOfSpeech = pair.first }
                        )
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            // ─── Save Word ─────────────────────────────────
            Button(
                onClick = {
                    if (word.isNotBlank() && definition.isNotBlank() && selectedSubject.isNotBlank()) {
                        var savedImagePath = ""
                        selectedImageUri?.let { uri ->
                            savedImagePath = copyUriToInternalStorage(context, uri) ?: ""
                        }
                        viewModel.addNewVocabulary(
                            word = word,
                            meaning = definition,
                            example = example,
                            phonetic = phonetic,
                            partOfSpeech = selectedPartOfSpeech,
                            topic = selectedSubject,
                            imageUrl = savedImagePath
                        )
                        navController.popBackStack()
                    }
                },
                enabled = word.isNotBlank() && definition.isNotBlank() && selectedSubject.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PurplePrimary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Lưu từ vựng", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            // ─── Cancel ────────────────────────────────────
            OutlinedButton(
                onClick = { navController.popBackStack() },
                border = BorderStroke(1.dp, BorderColor),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
            ) {
                Text("Hủy bỏ", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(Modifier.height(8.dp))
        }
    }

    // ─── Add new subject dialog ────────────────────────
    if (showNewSubjectDialog) {
        var newSubjectName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showNewSubjectDialog = false },
            title = { Text("Thêm chủ đề mới", fontWeight = FontWeight.Bold, color = TextPrimary) },
            text = {
                OutlinedTextField(
                    value = newSubjectName,
                    onValueChange = { newSubjectName = it },
                    placeholder = { Text("Ví dụ: Science") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = outlinedFieldColors()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newSubjectName.isNotBlank()) {
                            val cleanName = newSubjectName.trim().replaceFirstChar { it.uppercase() }
                            if (!subjectsList.contains(cleanName)) {
                                customSubjects = customSubjects + cleanName
                            }
                            selectedSubject = cleanName
                            showNewSubjectDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                ) {
                    Text("Thêm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewSubjectDialog = false }) {
                    Text("Hủy", color = TextSecondary)
                }
            }
        )
    }
}

// ─── Reusable outlined text field colors ───────────────
@Composable
private fun outlinedFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = PurplePrimary,
    unfocusedBorderColor = BorderColor,
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent
)

// ─── Subject chip ──────────────────────────────────────
@Composable
private fun SubjectChip(
    name: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .height(40.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) PurplePrimary else BorderColor
        ),
        color = if (selected) PurpleLight else Color.Transparent
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = name,
                color = if (selected) PurplePrimary else TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

// ─── Dotted circle add button ──────────────────────────
@Composable
private fun DottedAddButton(onClick: () -> Unit) {
    val primaryColor = PurplePrimary
    Box(
        modifier = Modifier
            .size(40.dp)
            .drawBehind {
                val stroke = Stroke(
                    width = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )
                drawCircle(
                    color = primaryColor,
                    radius = size.minDimension / 2,
                    style = stroke
                )
            }
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Thêm chủ đề",
            tint = PurplePrimary,
            modifier = Modifier.size(20.dp)
        )
    }
}

private fun copyUriToInternalStorage(context: android.content.Context, uri: Uri): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val file = File(context.filesDir, "vocab_${System.currentTimeMillis()}.jpg")
        inputStream.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }
        file.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
