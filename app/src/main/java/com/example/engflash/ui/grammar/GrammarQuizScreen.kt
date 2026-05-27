package com.example.engflash.ui.grammar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engflash.domain.model.GrammarQuestion
import com.example.engflash.domain.model.GrammarRule
import com.example.engflash.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrammarQuizScreen(
    grammarRuleId: String,
    viewModel: GrammarViewModel,
    onFinish: () -> Unit
) {
    val grammarRule by viewModel.getGrammarById(grammarRuleId).collectAsState(initial = null)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Trắc nghiệm ngữ pháp",
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onFinish) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Thoát Quiz"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        val rule = grammarRule
        if (rule == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (rule.questions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Bài học này hiện chưa có câu hỏi trắc nghiệm.",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onFinish) {
                        Text("Quay lại")
                    }
                }
            }
        } else {
            QuizContent(
                rule = rule,
                questions = rule.questions,
                paddingValues = paddingValues,
                onFinish = onFinish
            )
        }
    }
}

@Composable
private fun QuizContent(
    rule: GrammarRule,
    questions: List<GrammarQuestion>,
    paddingValues: PaddingValues,
    onFinish: () -> Unit
) {
    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    var selectedAnswerIndex by remember { mutableStateOf<Int?>(null) }
    var isAnswered by remember { mutableStateOf(false) }
    var score by remember { mutableIntStateOf(0) }
    var showResults by remember { mutableStateOf(false) }

    // Dùng để reset/chơi lại
    fun resetQuiz() {
        currentQuestionIndex = 0
        selectedAnswerIndex = null
        isAnswered = false
        score = 0
        showResults = false
    }

    if (showResults) {
        QuizResultsView(
            score = score,
            totalQuestions = questions.size,
            ruleTitle = rule.title,
            paddingValues = paddingValues,
            onRetry = ::resetQuiz,
            onFinish = onFinish
        )
    } else {
        val currentQuestion = questions[currentQuestionIndex]
        val progress = (currentQuestionIndex + 1).toFloat() / questions.size
        val animatedProgress by animateFloatAsState(targetValue = progress, label = "Progress")

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // ─── Progress Bar & Counter ─────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Câu ${currentQuestionIndex + 1} / ${questions.size}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Đúng: $score",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2E7D32)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                // ─── Question Card ──────────────────────────
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = currentQuestion.questionText,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 28.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ─── Options Grid ───────────────────────────
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    currentQuestion.options.forEachIndexed { index, option ->
                        val isSelected = selectedAnswerIndex == index
                        val isCorrectOption = index == currentQuestion.correctAnswerIndex

                        // Màu sắc trực quan tùy vào trạng thái câu trả lời
                        val (cardColor, borderColor, textColor, icon) = when {
                            !isAnswered -> {
                                Quad(
                                    MaterialTheme.colorScheme.surface,
                                    MaterialTheme.colorScheme.outlineVariant,
                                    MaterialTheme.colorScheme.onSurface,
                                    null
                                )
                            }
                            isCorrectOption -> {
                                Quad(
                                    Color(0xFFE8F5E9), // Light green
                                    Color(0xFF4CAF50), // Green border
                                    Color(0xFF2E7D32), // Dark green text
                                    Icons.Default.CheckCircle
                                )
                            }
                            isSelected -> {
                                Quad(
                                    Color(0xFFFFEBEE), // Light red
                                    Color(0xFFEF5350), // Red border
                                    Color(0xFFC62828), // Dark red text
                                    Icons.Default.Cancel
                                )
                            }
                            else -> {
                                Quad(
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                    null
                                )
                            }
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !isAnswered) {
                                    selectedAnswerIndex = index
                                    isAnswered = true
                                    if (index == currentQuestion.correctAnswerIndex) {
                                        score++
                                    }
                                },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = cardColor),
                            border = BorderStroke(1.5.dp, borderColor)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Option Label index (A, B, C, D)
                                    val label = ('A'.code + index).toChar().toString()
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isSelected && isAnswered) borderColor else MaterialTheme.colorScheme.primaryContainer
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected && isAnswered) Color.White else MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Text(
                                        text = option,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium,
                                        color = textColor
                                    )
                                }

                                if (icon != null) {
                                    Icon(
                                        icon,
                                        contentDescription = null,
                                        tint = borderColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ─── Explanation slide-in ──────────────────
                AnimatedVisibility(
                    visible = isAnswered,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 })
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFF9C4).copy(alpha = 0.5f) // Soft gold
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFFBC02D).copy(alpha = 0.7f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                Icons.Default.Lightbulb,
                                contentDescription = "Giải thích",
                                tint = Color(0xFFF57F17),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    "Giải thích lý thuyết",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFF57F17),
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = currentQuestion.explanation,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF5D4037),
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // ─── Next Action Button ──────────────────────
                Button(
                    onClick = {
                        if (currentQuestionIndex + 1 < questions.size) {
                            currentQuestionIndex++
                            selectedAnswerIndex = null
                            isAnswered = false
                        } else {
                            showResults = true
                        }
                    },
                    enabled = isAnswered,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = if (currentQuestionIndex + 1 < questions.size) "Câu Tiếp Theo ➡️" else "Xem Kết Quả 🏆",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

/** Màn hình báo cáo kết quả trắc nghiệm */
@Composable
private fun QuizResultsView(
    score: Int,
    totalQuestions: Int,
    ruleTitle: String,
    paddingValues: PaddingValues,
    onRetry: () -> Unit,
    onFinish: () -> Unit
) {
    val percentage = (score.toFloat() / totalQuestions * 100).toInt()
    val feedbackText = when {
        percentage == 100 -> "Xuất sắc! Bạn đã làm đúng tuyệt đối! 🏆"
        percentage >= 70 -> "Làm tốt lắm! Bạn đã nắm bài rất chắc! 🎉"
        else -> "Cố gắng lên! Bạn nên ôn tập lại kiến thức bài học. 💪"
    }
    val gaugeColor = when {
        percentage == 100 -> Color(0xFF2E7D32)
        percentage >= 70 -> Color(0xFFF57F17)
        else -> Color(0xFFC62828)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Tên bài học
        item {
            Text(
                text = ruleTitle,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }

        // Circular score container
        item {
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(CircleShape)
                    .background(gaugeColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$score / $totalQuestions",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = gaugeColor,
                        fontSize = 36.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$percentage% Đúng",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = gaugeColor
                    )
                }
            }
        }

        // Đánh giá
        item {
            Text(
                text = feedbackText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // Card summary
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "Tóm tắt kết quả:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Số câu trả lời đúng:")
                        Text(
                            "$score câu",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Số câu sai/bỏ lỡ:")
                        Text(
                            "${totalQuestions - score} câu",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFC62828)
                        )
                    }
                }
            }
        }

        // Actions
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Retry Button
                OutlinedButton(
                    onClick = onRetry,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Làm Lại",
                        fontWeight = FontWeight.Bold
                    )
                }

                // Finish button
                Button(
                    onClick = onFinish,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Hoàn Thành",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/** DTO Quad đơn giản để map UI */
private data class Quad<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
