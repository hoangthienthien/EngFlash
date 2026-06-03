package com.example.engflash.ui.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.engflash.domain.model.GrammarRule
import com.example.engflash.domain.model.Vocabulary
import com.example.engflash.ui.navigation.Routes

private val PageBg @Composable get() = MaterialTheme.colorScheme.background
private val CardBg @Composable get() = MaterialTheme.colorScheme.surface
private val TextPrimary @Composable get() = MaterialTheme.colorScheme.onBackground
private val TextSecondary @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
private val PurplePrimary @Composable get() = MaterialTheme.colorScheme.primary
private val PurpleLight @Composable get() = MaterialTheme.colorScheme.primaryContainer
private val GreenAccent   = Color(0xFF2DBD78)
private val GreenLight    = Color(0xFFE6F9F0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavController,
    viewModel: SearchViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = uiState.query,
                        onValueChange = { viewModel.onQueryChange(it) },
                        placeholder = {
                            Text(
                                "Tìm từ vựng, ngữ pháp...",
                                color = TextSecondary.copy(alpha = 0.6f),
                                fontSize = 15.sp
                            )
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary)
                        },
                        trailingIcon = {
                            AnimatedVisibility(
                                visible = uiState.query.isNotEmpty(),
                                enter = fadeIn(),
                                exit = fadeOut()
                            ) {
                                IconButton(onClick = { viewModel.onQueryChange("") }) {
                                    Icon(Icons.Default.Close, contentDescription = "Xóa", tint = TextSecondary)
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PurplePrimary,
                            unfocusedBorderColor = Color(0xFFE0DDF0),
                            focusedContainerColor = CardBg,
                            unfocusedContainerColor = CardBg
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PageBg)
            )
        },
        containerColor = PageBg
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Loading
            if (uiState.isSearching) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = PurplePrimary
                )
            }

            // Empty state
            else if (uiState.query.isBlank()) {
                EmptySearchState()
            }

            // No results
            else if (uiState.vocabularies.isEmpty() && uiState.grammarRules.isEmpty()) {
                NoResultsState(query = uiState.query)
            }

            // Results
            else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // ─── Vocabulary Results ───────────────────────
                    if (uiState.vocabularies.isNotEmpty()) {
                        item {
                            SectionHeader(
                                icon = Icons.Default.Book,
                                title = "Từ Vựng",
                                count = uiState.vocabularies.size,
                                iconTint = PurplePrimary,
                                bgColor = PurpleLight
                            )
                        }
                        items(uiState.vocabularies) { vocab ->
                            VocabResultCard(
                                vocab = vocab,
                                onClick = {
                                    // Navigate to vocabulary list of that topic
                                    navController.navigate(Routes.vocabularyList(vocab.topic))
                                }
                            )
                        }
                    }

                    // ─── Spacing between sections ─────────────────
                    if (uiState.vocabularies.isNotEmpty() && uiState.grammarRules.isNotEmpty()) {
                        item { Spacer(modifier = Modifier.height(8.dp)) }
                    }

                    // ─── Grammar Results ──────────────────────────
                    if (uiState.grammarRules.isNotEmpty()) {
                        item {
                            SectionHeader(
                                icon = Icons.Default.School,
                                title = "Ngữ Pháp",
                                count = uiState.grammarRules.size,
                                iconTint = GreenAccent,
                                bgColor = GreenLight
                            )
                        }
                        items(uiState.grammarRules) { rule ->
                            GrammarResultCard(
                                rule = rule,
                                onClick = {
                                    navController.navigate(Routes.grammarDetail(rule.id))
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── Section Header ─────────────────────────────────
@Composable
private fun SectionHeader(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    count: Int,
    iconTint: Color,
    bgColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
        }
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.weight(1f))
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = bgColor
        ) {
            Text(
                text = "$count kết quả",
                fontSize = 12.sp,
                color = iconTint,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
    }
}

// ─── Vocabulary Result Card ──────────────────────────
@Composable
private fun VocabResultCard(vocab: Vocabulary, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(PurpleLight),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = vocab.word.take(2).uppercase(),
                    color = PurplePrimary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = vocab.word,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TextPrimary
                )
                Text(
                    text = vocab.meaning,
                    fontSize = 13.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = PurpleLight
            ) {
                Text(
                    text = vocab.topic,
                    fontSize = 11.sp,
                    color = PurplePrimary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

// ─── Grammar Result Card ─────────────────────────────
@Composable
private fun GrammarResultCard(rule: GrammarRule, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(GreenLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.School,
                    contentDescription = null,
                    tint = GreenAccent,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = rule.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TextPrimary
                )
                Text(
                    text = rule.explanation,
                    fontSize = 13.sp,
                    color = TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ─── Empty Search State ──────────────────────────────
@Composable
private fun EmptySearchState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Search,
            contentDescription = null,
            tint = TextSecondary.copy(alpha = 0.35f),
            modifier = Modifier.size(80.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Tìm kiếm từ vựng & ngữ pháp",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = TextPrimary
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Nhập từ khoá để tìm kiếm trong toàn bộ bộ sưu tập của bạn.",
            fontSize = 14.sp,
            color = TextSecondary,
            lineHeight = 20.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

// ─── No Results State ────────────────────────────────
@Composable
private fun NoResultsState(query: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.SearchOff,
            contentDescription = null,
            tint = TextSecondary.copy(alpha = 0.35f),
            modifier = Modifier.size(80.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Không tìm thấy kết quả",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = TextPrimary
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Không có kết quả nào cho \"$query\".\nThử tìm với từ khóa khác.",
            fontSize = 14.sp,
            color = TextSecondary,
            lineHeight = 20.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
