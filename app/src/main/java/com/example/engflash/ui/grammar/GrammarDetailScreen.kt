package com.example.engflash.ui.grammar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engflash.domain.model.GrammarExample
import com.example.engflash.ui.grammar.components.AccordionSection
import com.example.engflash.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrammarDetailScreen(
    grammarId: String,
    viewModel: GrammarViewModel,
    onStartQuiz: (String) -> Unit,
    onBack: () -> Unit
) {
    val grammarRule by viewModel.getGrammarById(grammarId)
        .collectAsState(initial = null)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết ngữ pháp") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại"
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
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ─── Title Header ────────────────────────
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.School,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    "Ngữ pháp",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = rule.title,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                // ─── 1. Giải thích ───────────────────────
                item {
                    AccordionSection(
                        title = "Giải thích",
                        icon = Icons.Default.MenuBook,
                        initialExpanded = true
                    ) {
                        Text(
                            text = rule.explanation,
                            style = MaterialTheme.typography.bodyLarge,
                            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
                        )
                    }
                }

                // ─── 2. Cấu trúc ────────────────────────
                item {
                    AccordionSection(
                        title = "Cấu trúc",
                        icon = Icons.Default.Architecture
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                                    alpha = 0.5f
                                )
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = rule.structure,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(14.dp),
                                lineHeight = 32.sp
                            )
                        }
                    }
                }

                // ─── 3. Cách dùng ────────────────────────
                item {
                    AccordionSection(
                        title = "Cách dùng",
                        icon = Icons.Default.Lightbulb
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            rule.usage.forEachIndexed { index, usage ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(MaterialTheme.colorScheme.primaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "${index + 1}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = usage,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }

                // ─── 4. Ví dụ ────────────────────────────
                item {
                    AccordionSection(
                        title = "Ví dụ",
                        icon = Icons.Default.FormatQuote
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            rule.examples.forEach { example ->
                                ExampleCard(example)
                            }
                        }
                    }
                }

                // ─── 5. Ghi chú ──────────────────────────
                if (!rule.notes.isNullOrBlank()) {
                    item {
                        AccordionSection(
                            title = "Ghi chú",
                            icon = Icons.Default.StickyNote2
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = rule.notes ?: "",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(14.dp),
                                    lineHeight = 26.sp
                                )
                            }
                        }
                    }
                }

                // ─── 6. Bắt đầu làm Quiz ───────────────────
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Quiz,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Đã hiểu bài học?",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Text(
                                "Thử thách ngay với 3 câu hỏi trắc nghiệm thực hành để kiểm tra kiến thức của bạn nhé!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { onStartQuiz(rule.id) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.tertiary
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    "Bắt Đầu Làm Quiz 🚀",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onTertiary
                                )
                            }
                        }
                    }
                }

                // Bottom spacer
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

/** Hiển thị 1 ví dụ: tiếng Anh (nổi bật) + tiếng Việt. */
@Composable
private fun ExampleCard(example: GrammarExample) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "🇬🇧  ${example.english}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "🇻🇳  ${example.vietnamese}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

