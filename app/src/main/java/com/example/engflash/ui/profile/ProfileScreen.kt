package com.example.engflash.ui.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.engflash.ui.navigation.Routes
import com.example.engflash.util.NotificationHelper
import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

// ─── Custom Colors (không thay đổi theo theme) ──────────────────────
private val AccentGreen     = Color(0xFF4CAF50)
private val AccentYellow    = Color(0xFFFFC107)
private val AccentOrange    = Color(0xFFFF6B35)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel,
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    var nameInput by remember { mutableStateOf("") }
    var bioInput  by remember { mutableStateOf("") }

    // ─── Theme Colors ───
    val PurplePrimary   = MaterialTheme.colorScheme.primary
    val PurpleLight     = MaterialTheme.colorScheme.primaryContainer
    val PurpleDark      = MaterialTheme.colorScheme.primary
    val PageBg          = MaterialTheme.colorScheme.background
    val CardBg          = MaterialTheme.colorScheme.surface
    val TextPrimary     = MaterialTheme.colorScheme.onBackground
    val TextSecondary   = MaterialTheme.colorScheme.onSurfaceVariant
    val DividerColor    = MaterialTheme.colorScheme.outlineVariant
    val ProgressTrack   = MaterialTheme.colorScheme.surfaceVariant

    val context       = LocalContext.current
    val prefsReminder = context.getSharedPreferences("engflash_prefs", android.content.Context.MODE_PRIVATE)
    var pushNotifs     by remember { mutableStateOf(prefsReminder.getBoolean("push_notifs_enabled", true)) }
    var hasChanges     by remember { mutableStateOf(false) }

    // Reminder time — đọc từ SharedPreferences
    var reminderHour   by remember { mutableIntStateOf(prefsReminder.getInt("reminder_hour", 9)) }
    var reminderMinute by remember { mutableIntStateOf(prefsReminder.getInt("reminder_minute", 0)) }
    var showTimePicker  by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            pushNotifs = true
            prefsReminder.edit().putBoolean("push_notifs_enabled", true).apply()
            NotificationHelper.scheduleDailyReminder(context, reminderHour, reminderMinute)
        } else {
            pushNotifs = false
            prefsReminder.edit().putBoolean("push_notifs_enabled", false).apply()
        }
    }

    val csvExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/comma-separated-values")
    ) { uri: Uri? ->
        uri?.let {
            viewModel.exportVocabulariesToUri(context, it)
        }
    }

    val csvImportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.importVocabulariesFromUri(context, it)
        }
    }

    // TimePickerDialog (side-effect — không phải Composable)
    if (showTimePicker) {
        android.app.TimePickerDialog(
            context,
            { _, hour, minute ->
                reminderHour = hour
                reminderMinute = minute
                prefsReminder.edit()
                    .putInt("reminder_hour", hour)
                    .putInt("reminder_minute", minute)
                    .apply()
                if (pushNotifs) {
                    NotificationHelper.scheduleDailyReminder(context, hour, minute)
                }
            },
            reminderHour, reminderMinute, false
        ).show()
        showTimePicker = false
    }

    LaunchedEffect(Unit) {
        viewModel.loadProfile()
    }

    LaunchedEffect(uiState.profile) {
        uiState.profile?.let {
            nameInput = it.displayName
            bioInput  = it.bio
        }
    }


    val currentStreak by viewModel.streakManager.currentStreak.collectAsStateWithLifecycle()
    val achievements = remember(uiState, currentStreak) { viewModel.getAchievements(currentStreak) }

    Scaffold(
        containerColor = PageBg,
        bottomBar = {
            NavigationBar(containerColor = CardBg, tonalElevation = 0.dp) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, null) },
                    label = { Text("Trang chủ", fontSize = 11.sp) },
                    selected = false,
                    onClick = { navController.navigate(Routes.HOME) { popUpTo(Routes.HOME) { inclusive = true } } },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        unselectedIconColor = TextSecondary,
                        indicatorColor = PurplePrimary
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Book, null) },
                    label = { Text("Từ vựng", fontSize = 11.sp) },
                    selected = false,
                    onClick = { navController.navigate(Routes.VOCABULARY_PLACEHOLDER) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        unselectedIconColor = TextSecondary,
                        indicatorColor = PurplePrimary
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Style, null) },
                    label = { Text("Luyện tập", fontSize = 11.sp) },
                    selected = false,
                    onClick = { navController.navigate(Routes.FLASHCARD_PLACEHOLDER) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        unselectedIconColor = TextSecondary,
                        indicatorColor = PurplePrimary
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.School, null) },
                    label = { Text("Ngữ pháp", fontSize = 11.sp) },
                    selected = false,
                    onClick = { navController.navigate(Routes.GRAMMAR_TOPIC_LIST) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        unselectedIconColor = TextSecondary,
                        indicatorColor = PurplePrimary
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, null) },
                    label = { Text("Cá nhân", fontSize = 11.sp) },
                    selected = true,
                    onClick = {},
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        unselectedIconColor = TextSecondary,
                        indicatorColor = PurplePrimary
                    )
                )
            }
        }
    ) { pv ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(PageBg)
                .padding(pv)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = PurpleLight
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {

                    // ── Top bar ──────────────────────────────────────────
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Hồ Sơ & Thống Kê",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    // ── Avatar card ──────────────────────────────────────
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBg),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Avatar circle with gradient ring
                            Box(
                                modifier = Modifier
                                    .size(84.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(listOf(PurpleLight, PurpleDark))
                                    )
                                    .border(
                                        width = 3.dp,
                                        brush = Brush.linearGradient(listOf(PurpleLight, AccentYellow)),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                val initials = nameInput.trim().split(" ")
                                    .mapNotNull { it.firstOrNull()?.toString() }
                                    .take(2).joinToString("").uppercase()
                                Text(
                                    text = initials.ifEmpty { "EF" },
                                    color = Color.White,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(Modifier.height(12.dp))

                            Text(
                                nameInput.ifEmpty { "Người dùng" },
                                color = TextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                uiState.profile?.email ?: "",
                                color = TextSecondary,
                                fontSize = 13.sp
                              )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                bioInput.ifEmpty { "Học tiếng Anh mỗi ngày cùng EngFlash!" },
                                color = PurpleLight,
                                fontSize = 13.sp
                            )

                            Spacer(Modifier.height(20.dp))
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // ── Stats Row ────────────────────────────────────────
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Words Mastered
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = CardBg),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.AutoMirrored.Filled.MenuBook, null, tint = PurpleLight, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Từ vựng đã thuộc", color = TextSecondary, fontSize = 11.sp)
                                }
                                Spacer(Modifier.height(8.dp))
                                Text("${uiState.learnedCount}", color = TextPrimary, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Bookmark, null, tint = PurpleLight, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(2.dp))
                                    Text("Đã lưu: ${uiState.favoriteCount}", color = TextSecondary, fontSize = 11.sp)
                                }
                            }
                        }

                        // Total Words
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = CardBg),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Layers, null, tint = PurpleLight, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Tổng từ vựng", color = TextSecondary, fontSize = 11.sp)
                                }
                                Spacer(Modifier.height(8.dp))
                                Text("${uiState.totalCount}", color = TextPrimary, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.AutoMirrored.Filled.TrendingUp, null, tint = AccentGreen, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(2.dp))
                                    val pct = if (uiState.totalCount > 0) (uiState.learnedCount * 100 / uiState.totalCount) else 0
                                    Text("Hoàn thành: $pct%", color = TextSecondary, fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // ── Edit Profile Section ────────────────────────────
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBg),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text("Chỉnh sửa hồ sơ", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(12.dp))

                            OutlinedTextField(
                                value = nameInput,
                                onValueChange = {
                                    nameInput = it
                                    hasChanges = true
                                },
                                label = { Text("Tên hiển thị") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = bioInput,
                                onValueChange = {
                                    bioInput = it
                                    hasChanges = true
                                },
                                label = { Text("Tiểu sử") },
                                maxLines = 3,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            Spacer(Modifier.height(12.dp))

                            // Nút Lưu — chỉ hiện khi có thay đổi
                            AnimatedVisibility(visible = hasChanges) {
                                Button(
                                    onClick = {
                                        viewModel.updateName(nameInput)
                                        viewModel.updateBio(bioInput)
                                        viewModel.saveProfile()
                                        hasChanges = false
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                                ) {
                                    if (uiState.isSaving) {
                                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                                    } else {
                                        Text("Lưu thay đổi", color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            // Success message
                            if (uiState.saveSuccess) {
                                Spacer(Modifier.height(8.dp))
                                Text("✅ Đã lưu thành công!", color = AccentGreen, fontSize = 13.sp)
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // ── Achievements ─────────────────────────────────────
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Thành tựu đạt được", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "Xem tất cả",
                            color = PurpleLight,
                            fontSize = 13.sp,
                            modifier = Modifier.clickable {
                                navController.navigate("achievements")
                            }
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(achievements) { achievement ->
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (achievement.isUnlocked) CardBg else CardBg.copy(alpha = 0.6f)
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.width(100.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (achievement.isUnlocked)
                                                    achievement.tint.copy(alpha = 0.15f)
                                                else
                                                    Color.Gray.copy(alpha = 0.1f)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            achievement.icon, null,
                                            tint = if (achievement.isUnlocked) achievement.tint else Color.Gray,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    if (!achievement.isUnlocked) {
                                        Spacer(Modifier.height(4.dp))
                                        LinearProgressIndicator(
                                            progress = { achievement.progress },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(3.dp)
                                                .clip(CircleShape),
                                            color = achievement.tint,
                                            trackColor = Color.Gray.copy(0.15f)
                                        )
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        achievement.label,
                                        color = if (achievement.isUnlocked) TextPrimary else TextSecondary,
                                        fontSize = 10.sp,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 14.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // ── Quick Settings ───────────────────────────────────
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBg),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Cài đặt nhanh", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(8.dp))
                            val themeMode by viewModel.themeManager.themeMode.collectAsStateWithLifecycle()
                            val isDarkMode = themeMode == true

                            // Theme Mode Selection
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.DarkMode, null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(10.dp))
                                    Text("Chế độ tối (Dark Mode)", color = TextPrimary, fontSize = 14.sp)
                                }
                                Switch(
                                    checked = isDarkMode,
                                    onCheckedChange = { checked ->
                                        viewModel.themeManager.setThemeMode(checked)
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = PurplePrimary,
                                        uncheckedTrackColor = Color.Gray.copy(alpha = 0.3f)
                                    )
                                )
                            }

                            HorizontalDivider(color = DividerColor)

                            // Push Notifications
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Notifications, null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(10.dp))
                                    Text("Thông báo nhắc nhở", color = TextPrimary, fontSize = 14.sp)
                                }
                                Switch(
                                    checked = pushNotifs,
                                    onCheckedChange = { checked -> 
                                        if (checked) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                            } else {
                                                pushNotifs = true
                                                prefsReminder.edit().putBoolean("push_notifs_enabled", true).apply()
                                                NotificationHelper.scheduleDailyReminder(context, reminderHour, reminderMinute)
                                            }
                                        } else {
                                            pushNotifs = false
                                            prefsReminder.edit().putBoolean("push_notifs_enabled", false).apply()
                                            NotificationHelper.cancelDailyReminder(context)
                                        }
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = PurplePrimary,
                                        uncheckedTrackColor = Color.White.copy(0.2f)
                                    )
                                )
                            }
                            
                            HorizontalDivider(color = DividerColor)
                            
                            // Sound Settings
                            var soundEnabled by remember { mutableStateOf(viewModel.soundManager.isSoundEnabled) }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.AutoMirrored.Filled.VolumeUp, null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(10.dp))
                                    Text("Âm thanh hiệu ứng", color = TextPrimary, fontSize = 14.sp)
                                }
                                Switch(
                                    checked = soundEnabled,
                                    onCheckedChange = { 
                                        soundEnabled = it
                                        viewModel.soundManager.isSoundEnabled = it
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = PurplePrimary,
                                        uncheckedTrackColor = Color.White.copy(0.2f)
                                    )
                                )
                            }

                            HorizontalDivider(color = DividerColor)

                            // Daily Reminder
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showTimePicker = true },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AccessTime, null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(10.dp))
                                    Text("Hẹn giờ hàng ngày", color = TextPrimary, fontSize = 14.sp)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val ampm = if (reminderHour < 12) "AM" else "PM"
                                    val h12  = reminderHour % 12
                                    val displayH = if (h12 == 0) 12 else h12
                                    Text(
                                        String.format("%02d:%02d %s", displayH, reminderMinute, ampm),
                                        color = PurpleLight, fontSize = 13.sp, fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Icon(Icons.Default.Schedule, null, tint = PurpleLight, modifier = Modifier.size(14.dp))
                                }
                            }

                            HorizontalDivider(color = DividerColor)

                            // Import CSV
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { csvImportLauncher.launch("text/*") },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Upload, null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(10.dp))
                                    Text("Nhập từ vựng từ file (CSV)", color = TextPrimary, fontSize = 14.sp)
                                }
                                Icon(Icons.Default.ChevronRight, null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                            }

                            HorizontalDivider(color = DividerColor)

                            // Export CSV
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { csvExportLauncher.launch("engflash_vocab_backup.csv") },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Download, null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(10.dp))
                                    Text("Xuất từ vựng ra file (CSV)", color = TextPrimary, fontSize = 14.sp)
                                }
                                Icon(Icons.Default.ChevronRight, null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                            }

                            HorizontalDivider(color = DividerColor)

                            // Clean up duplicates
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.cleanUpDuplicates(context) },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CleaningServices, null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(10.dp))
                                    Text("Dọn dẹp từ trùng lặp", color = TextPrimary, fontSize = 14.sp)
                                }
                                Icon(Icons.Default.ChevronRight, null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))



                    // ── Logout ───────────────────────────────────────────
                    TextButton(
                        onClick = {
                            FirebaseAuth.getInstance().signOut()
                            onLogout()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                    ) {
                        Text("Đăng xuất tài khoản", color = Color.Red.copy(0.7f), fontSize = 14.sp)
                    }

                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

// ── SVG Achievement Icons ─────────────────────────────────────────────────
private val AchieveTrophyIcon: ImageVector by lazy {
    ImageVector.Builder("Trophy", 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = SolidColor(Color(0xFFFFC107))) {
            moveTo(5f, 3f); lineTo(19f, 3f); lineTo(19f, 8f)
            curveTo(19f, 11.87f, 15.87f, 15f, 12f, 15f)
            curveTo(8.13f, 15f, 5f, 11.87f, 5f, 8f); close()
            moveTo(12f, 17f); curveTo(13.1f, 17f, 14f, 17.9f, 14f, 19f)
            lineTo(14f, 20f); lineTo(10f, 20f); lineTo(10f, 19f)
            curveTo(10f, 17.9f, 10.9f, 17f, 12f, 17f); close()
            moveTo(8f, 20f); lineTo(16f, 20f); lineTo(16f, 22f); lineTo(8f, 22f); close()
        }
    }.build()
}

private val AchieveLightningIcon: ImageVector by lazy {
    ImageVector.Builder("Lightning", 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = SolidColor(Color(0xFF9B6DFF))) {
            moveTo(13f, 2f); lineTo(5f, 14f); lineTo(11f, 14f)
            lineTo(11f, 22f); lineTo(19f, 10f); lineTo(13f, 10f); close()
        }
    }.build()
}

private val AchieveStarIcon: ImageVector by lazy {
    ImageVector.Builder("Star", 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = SolidColor(Color(0xFF4CAF50)), pathFillType = PathFillType.NonZero) {
            moveTo(12f, 2f)
            lineTo(15.09f, 8.26f); lineTo(22f, 9.27f); lineTo(17f, 14.14f)
            lineTo(18.18f, 21.02f); lineTo(12f, 17.77f); lineTo(5.82f, 21.02f)
            lineTo(7f, 14.14f); lineTo(2f, 9.27f); lineTo(8.91f, 8.26f); close()
        }
    }.build()
}
