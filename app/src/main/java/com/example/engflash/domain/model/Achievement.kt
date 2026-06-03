package com.example.engflash.domain.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class Achievement(
    val id: String,
    val label: String,
    val description: String,
    val icon: ImageVector,
    val tint: Color,
    val isUnlocked: Boolean,
    val progress: Float  // 0.0f → 1.0f
)
