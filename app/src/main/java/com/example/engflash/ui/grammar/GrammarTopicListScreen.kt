package com.example.engflash.ui.grammar

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.engflash.domain.model.GrammarRule
import com.example.engflash.ui.navigation.Routes
import com.example.engflash.util.GrammarTipsPool
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle

// Màu sắc sẽ được lấy từ MaterialTheme.colorScheme bên trong mỗi Composable

@Composable
fun GrammarTopicListScreen(
    viewModel: GrammarViewModel,
    navController: NavController,
    onStartQuiz: (String) -> Unit
) {
    // ─── Theme Colors ───
    val PageBg = MaterialTheme.colorScheme.background
    val CardBg = MaterialTheme.colorScheme.surface
    val TextPrimary = MaterialTheme.colorScheme.onBackground
    val TextSecondary = MaterialTheme.colorScheme.onSurfaceVariant
    val PurplePrimary = MaterialTheme.colorScheme.primary
    val PurpleLight = MaterialTheme.colorScheme.primaryContainer
    val BorderColor = MaterialTheme.colorScheme.outlineVariant

    val grammarRules by viewModel.allGrammarRules.collectAsState(initial = emptyList())
    var expandedRuleId by remember { mutableStateOf<String?>("present_perfect") }

    val currentUser = remember { viewModel.getCurrentUser() }
    val displayName = currentUser?.displayName ?: "User"
    val currentStreak by viewModel.streakManager.currentStreak.collectAsStateWithLifecycle()

    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember(context) { context.getSharedPreferences("engflash_prefs", android.content.Context.MODE_PRIVATE) }

    // State to trigger recomposition when quizzes are completed
    var updateTrigger by remember { mutableIntStateOf(0) }

    // Re-check scores whenever screen is displayed
    val completedRulesCount = remember(grammarRules, updateTrigger) {
        grammarRules.count { rule ->
            prefs.getInt("grammar_score_${rule.id}", -1) != -1
        }
    }
    val totalRules = grammarRules.size
    val overallProgressPercent = if (totalRules > 0) (completedRulesCount * 100 / totalRules) else 0

    // Random tips — chọn 1 lần khi màn hình được compose
    val studyTip = remember { GrammarTipsPool.getRandomStudyTip() }
    val aiInsight = remember { GrammarTipsPool.getRandomAiInsight() }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = CardBg, tonalElevation = 0.dp) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, null) },
                    label = { Text("Trang chủ", fontSize = 11.sp) }, // Consistently Vietnamese
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
                    selected = true,
                    onClick = { },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        unselectedIconColor = TextSecondary,
                        indicatorColor = PurplePrimary
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, null) },
                    label = { Text("Cá nhân", fontSize = 11.sp) },
                    selected = false,
                    onClick = { navController.navigate(Routes.PROFILE) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        unselectedIconColor = TextSecondary,
                        indicatorColor = PurplePrimary
                    )
                )
            }
        },
        containerColor = PageBg
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ─── Header Row ────────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(PurplePrimary)
                                .clickable { navController.navigate(Routes.PROFILE) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = displayName.firstOrNull()?.uppercase() ?: "U",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Text(
                            text = "EngFlash",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp,
                            color = TextPrimary
                        )
                    }

                    // Streak pill (no material icon as requested, uses sleek styling/text)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFF1F0F7))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "$currentStreak 🔥",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }

            // ─── Grammar Hub Title & Progress ──────────────
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Trung Tâm Ngữ Pháp",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp,
                        color = TextPrimary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Làm chủ ngữ pháp tiếng Anh qua các bài học trực quan sinh động.",
                        color = TextSecondary,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )

                    Spacer(Modifier.height(16.dp))

                    // Progress Section (Tiến độ hoàn thành)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = PurpleLight.copy(alpha = 0.3f)),
                        border = BorderStroke(1.dp, PurpleLight)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Tiến độ của bạn",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = PurplePrimary
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = "Đã hoàn thành $completedRulesCount / $totalRules chủ đề",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Box(contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(
                                    progress = { if (totalRules > 0) completedRulesCount.toFloat() / totalRules.toFloat() else 0f },
                                    color = PurplePrimary,
                                    trackColor = Color.White,
                                    strokeWidth = 5.dp,
                                    modifier = Modifier.size(48.dp)
                                )
                                Text(
                                    text = "$overallProgressPercent%",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 11.sp,
                                    color = PurplePrimary
                                )
                            }
                        }
                    }
                }
            }

            // ─── Grammar Rules List ────────────────────────
            items(grammarRules) { rule ->
                val savedScore = prefs.getInt("grammar_score_${rule.id}", -1)
                val savedTotal = prefs.getInt("grammar_total_${rule.id}", 0)

                GrammarRuleCard(
                    rule = rule,
                    isExpanded = expandedRuleId == rule.id,
                    savedScore = savedScore,
                    savedTotal = savedTotal,
                    onToggleExpand = {
                        expandedRuleId = if (expandedRuleId == rule.id) null else rule.id
                    },
                    onStartQuiz = { onStartQuiz(rule.id) }
                )
            }

            // ─── Smart Tips & AI Insights ──────────────────
            item {
                Spacer(Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Smart Tips (Clean card - no icons)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBg),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(PurpleLight)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "MẸO HỌC TẬP",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PurplePrimary
                                    )
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = studyTip.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = TextPrimary
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = studyTip.description,
                                color = TextSecondary,
                                fontSize = 13.sp
                            )
                        }
                    }

                    // AI Insights (Clean card - no icons)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBg),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFF1F2F6))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "PHÂN TÍCH AI",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF57606F)
                                    )
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = aiInsight.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = TextPrimary
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = aiInsight.description,
                                color = TextSecondary,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GrammarRuleCard(
    rule: GrammarRule,
    isExpanded: Boolean,
    savedScore: Int,
    savedTotal: Int,
    onToggleExpand: () -> Unit,
    onStartQuiz: () -> Unit
) {
    // ─── Theme Colors ───
    val PageBg = MaterialTheme.colorScheme.background
    val CardBg = MaterialTheme.colorScheme.surface
    val TextPrimary = MaterialTheme.colorScheme.onBackground
    val TextSecondary = MaterialTheme.colorScheme.onSurfaceVariant
    val PurplePrimary = MaterialTheme.colorScheme.primary
    val PurpleLight = MaterialTheme.colorScheme.primaryContainer
    val BorderColor = MaterialTheme.colorScheme.outlineVariant

    val levelEn = getGrammarLevel(rule.id)
    val categoryEn = getGrammarCategory(rule.topicId)

    val level = when(levelEn) {
        "BEGINNER" -> "CƠ BẢN"
        "INTERMEDIATE" -> "TRUNG CẤP"
        "ADVANCED" -> "NÂNG CAO"
        else -> levelEn
    }

    val category = when(categoryEn) {
        "Verb Tenses" -> "Thì của Động từ"
        "Auxiliaries" -> "Trợ động từ"
        "Grammar" -> "Ngữ pháp"
        else -> categoryEn
    }

    val (levelBg, levelText) = when (levelEn) {
        "BEGINNER" -> Pair(Color(0xFFF1F2F6), Color(0xFF57606F))
        "INTERMEDIATE" -> Pair(Color(0xFFEAE5FF), Color(0xFF5E3CB3))
        "ADVANCED" -> Pair(Color(0xFFFFF3E0), Color(0xFFFFB300))
        else -> Pair(Color(0xFFEAE5FF), Color(0xFF5E3CB3))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleExpand() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(levelBg)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = level,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = levelText
                        )
                    }
                    Text(
                        text = category,
                        fontSize = 12.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )

                    // Completion score badge
                    if (savedScore != -1) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFE2F0D9))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Đã đạt ($savedScore/$savedTotal)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF385723)
                            )
                        }
                    }
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = rule.title,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = TextPrimary
            )

            if (isExpanded) {
                Spacer(Modifier.height(16.dp))

                // STRUCTURE Section
                Text(
                    text = "CẤU TRÚC",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = PurplePrimary,
                    letterSpacing = 0.5.sp
                )
                Spacer(Modifier.height(6.dp))
                val cleanStructure = remember(rule.structure) {
                    rule.structure
                        .split("\n")
                        .map { line ->
                            line.trim()
                                .removePrefix("✅")
                                .removePrefix("❌")
                                .removePrefix("❓")
                                .trim()
                        }
                        .joinToString("\n")
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF7F5FC))
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = cleanStructure,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF5E3CB3),
                        lineHeight = 22.sp
                    )
                }

                Spacer(Modifier.height(16.dp))

                // USAGE Section (No material checkmark icons as requested)
                Text(
                    text = "CÁCH DÙNG",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = PurplePrimary,
                    letterSpacing = 0.5.sp
                )
                Spacer(Modifier.height(6.dp))
                rule.usage.forEach { usageItem ->
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "•",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = PurplePrimary,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = usageItem,
                            fontSize = 13.sp,
                            color = TextPrimary,
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // EXAMPLES Section
                Text(
                    text = "VÍ DỤ",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = PurplePrimary,
                    letterSpacing = 0.5.sp
                )
                Spacer(Modifier.height(6.dp))

                // Table of Examples
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFBFBFE))
                        .border(1.dp, Color(0xFFEEEAF7), RoundedCornerShape(12.dp))
                ) {
                    // Table Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF5F4FA))
                            .padding(vertical = 8.dp, horizontal = 12.dp)
                    ) {
                        Text(
                            text = "Ý nghĩa / Ngữ cảnh",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = TextPrimary,
                            modifier = Modifier.weight(0.35f)
                        )
                        Text(
                            text = "Câu ví dụ",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = TextPrimary,
                            modifier = Modifier.weight(0.65f)
                        )
                    }

                    // Table Rows
                    rule.examples.forEachIndexed { idx, example ->
                        HorizontalDivider(color = Color(0xFFEEEAF7))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp, horizontal = 12.dp)
                        ) {
                            Text(
                                text = example.vietnamese,
                                fontSize = 12.sp,
                                color = TextSecondary,
                                modifier = Modifier.weight(0.35f)
                            )
                            Box(modifier = Modifier.weight(0.65f)) {
                                HighlightedSentence(text = example.english, structure = rule.structure)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Quick Quiz Button
                Button(
                    onClick = onStartQuiz,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(PurplePrimary, Color(0xFF00B4DB))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Luyện tập nhanh ⚡", // Vietnamese quiz title
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HighlightedSentence(text: String, structure: String = "") {
    val TextPrimary = MaterialTheme.colorScheme.onBackground

    val annotatedString = buildAnnotatedString {
        val patterns = buildHighlightPatterns(structure)
        val allMatches = patterns
            .flatMap { it.findAll(text).toList() }
            .sortedBy { it.range.first }

        if (allMatches.isNotEmpty()) {
            val match = allMatches.first()
            append(text.substring(0, match.range.first))
            pushStyle(SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic))
            append(match.value)
            pop()
            append(text.substring(match.range.last + 1))
        } else {
            append(text)
        }
    }
    Text(
        text = annotatedString,
        fontSize = 12.sp,
        color = TextPrimary
    )
}

/**
 * Xây dựng danh sách Regex dựa trên structure của grammar rule.
 * Ví dụ: "[S + have/has + V3/V-ed]" → tìm pattern "have/has + word"
 */
private fun buildHighlightPatterns(structure: String): List<Regex> {
    val patterns = mutableListOf<Regex>()

    // Present Perfect: have/has + past participle
    if (structure.contains("have/has") || structure.contains("V3")) {
        patterns.add(Regex("\\b(have|has)\\s+\\w+\\b", RegexOption.IGNORE_CASE))
    }
    // Past Simple: did + not + V or V-ed endings
    if (structure.contains("V2") || structure.contains("did")) {
        patterns.add(Regex("\\b(did\\s+not|didn't)\\s+\\w+\\b", RegexOption.IGNORE_CASE))
        patterns.add(Regex("\\b\\w{4,}ed\\b", RegexOption.IGNORE_CASE))
    }
    // Modal verbs: must/might/could/can't + V
    if (structure.contains("must") || structure.contains("might") || structure.contains("can't")) {
        patterns.add(Regex("\\b(must|might|could|can't|cannot)\\s+\\w+\\b", RegexOption.IGNORE_CASE))
    }
    // Conditionals: would/would have
    if (structure.contains("would")) {
        patterns.add(Regex("\\bwould(\\s+have)?\\s+\\w+\\b", RegexOption.IGNORE_CASE))
    }

    // Fallback: highlight các động từ bất quy tắc phổ biến
    if (patterns.isEmpty()) {
        patterns.add(Regex("\\b(went|gone|seen|grown|taken|given|been|done|made|had|was|were)\\b", RegexOption.IGNORE_CASE))
    }

    return patterns
}

private fun getGrammarLevel(id: String): String {
    return when (id) {
        "past_simple" -> "BEGINNER"
        "present_perfect" -> "INTERMEDIATE"
        "modal_deduction" -> "ADVANCED"
        else -> "INTERMEDIATE"
    }
}

private fun getGrammarCategory(topicId: String): String {
    return when (topicId) {
        "basic_tenses", "verb_tenses" -> "Verb Tenses"
        "auxiliaries" -> "Auxiliaries"
        else -> "Grammar"
    }
}
