package com.example.engflash.ui.onboarding

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.engflash.R
import com.example.engflash.ui.navigation.Routes
import kotlinx.coroutines.launch

data class OnboardingPage(
    val title: String,
    val description: String,
    val iconId: Int // Placeholder cho icon/image, tạm dùng icon vector hoặc drawable có sẵn
)

private val pages = listOf(
    OnboardingPage(
        title = "Học từ vựng hiệu quả",
        description = "Sử dụng Flashcard 3D sinh động giúp bạn nhớ từ mới nhanh chóng và lâu dài.",
        iconId = android.R.drawable.ic_menu_agenda // Placeholder
    ),
    OnboardingPage(
        title = "Ngữ pháp vững chắc",
        description = "Luyện tập ngữ pháp với các bài quiz tương tác từ cơ bản đến nâng cao.",
        iconId = android.R.drawable.ic_menu_sort_by_size // Placeholder
    ),
    OnboardingPage(
        title = "Theo dõi tiến độ",
        description = "Đạt các thành tựu mới, duy trì chuỗi ngày học để tạo động lực mỗi ngày.",
        iconId = android.R.drawable.ic_menu_today // Placeholder
    )
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    val PurplePrimary = Color(0xFF5E3CB3)
    val PageBg = Color(0xFFF9F9FB)

    fun finishOnboarding() {
        val prefs = context.getSharedPreferences("engflash_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("has_seen_onboarding", true).apply()
        
        navController.navigate(Routes.LOGIN) {
            popUpTo(Routes.ONBOARDING) { inclusive = true }
        }
    }

    Scaffold(containerColor = PageBg) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { position ->
                OnboardingPageContent(page = pages[position])
            }

            // Indicator & Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Page Indicator
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(pages.size) { index ->
                        val isSelected = pagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .height(8.dp)
                                .width(if (isSelected) 24.dp else 8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isSelected) PurplePrimary else Color.LightGray)
                        )
                    }
                }

                // Next / Finish Button
                Button(
                    onClick = {
                        if (pagerState.currentPage < pages.lastIndex) {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        } else {
                            finishOnboarding()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = if (pagerState.currentPage == pages.lastIndex) "Bắt đầu" else "Tiếp",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White)
                }
            }
        }
    }
}

@Composable
fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Placeholder for illustration
        Box(
            modifier = Modifier
                .size(240.dp)
                .clip(CircleShape)
                .background(Color(0xFFEAE5FF)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = page.iconId),
                contentDescription = null,
                modifier = Modifier.size(100.dp),
                tint = Color(0xFF5E3CB3)
            )
        }

        Spacer(Modifier.height(48.dp))

        Text(
            text = page.title,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF1E1640),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = page.description,
            fontSize = 16.sp,
            color = Color(0xFF7D7799),
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
    }
}
