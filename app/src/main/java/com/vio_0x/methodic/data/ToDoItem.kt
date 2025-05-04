package com.vio_0x.methodic.data

import java.util.Date

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