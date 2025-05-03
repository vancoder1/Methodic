package com.vio_0x.methodic

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.KeyboardDoubleArrowUp
import androidx.compose.material.icons.filled.Remove
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import java.util.*

data class ToDoItem(
    val id: Int,
    val text: String,
    val description: String? = null,
    var isCompleted: Boolean = false,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val createdAt: Date = Date(),
    val dueDate: Date? = null,
    val completedAt: Date? = null,
    val tags: List<String> = emptyList()
)

enum class TaskPriority(
    val displayName: String,
    val color: Color,
    val icon: ImageVector
) {
    HIGH("High", Color(0xFFE57373), Icons.Default.KeyboardDoubleArrowUp),
    MEDIUM("Medium", Color(0xFFFFB74D), Icons.Default.Flag),
    LOW("Low", Color(0xFF81C784), Icons.Default.Remove)
}

enum class TaskFilter(val displayName: String) {
    ALL("All Tasks"),
    HIGH("High Priority"),
    MEDIUM("Medium Priority"),
    LOW("Low Priority")
}