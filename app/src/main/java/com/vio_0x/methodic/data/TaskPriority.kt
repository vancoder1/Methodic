package com.vio_0x.methodic.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.KeyboardDoubleArrowUp
import androidx.compose.material.icons.filled.Remove
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class TaskPriority(
    val displayName: String,
    val color: Color,
    val icon: ImageVector
) {
    HIGH("High", Color(0xFFE57373), Icons.Default.KeyboardDoubleArrowUp),
    MEDIUM("Medium", Color(0xFFFFB74D), Icons.Default.Flag),
    LOW("Low", Color(0xFF81C784), Icons.Default.Remove)
}