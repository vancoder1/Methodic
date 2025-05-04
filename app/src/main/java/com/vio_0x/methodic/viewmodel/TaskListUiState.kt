package com.vio_0x.methodic.viewmodel

import com.vio_0x.methodic.data.TaskFilter
import com.vio_0x.methodic.data.TaskPriority
import com.vio_0x.methodic.data.ToDoItem

// Renamed from MainUiState
data class TaskListUiState(
    val items: List<ToDoItem> = emptyList(),
    val filter: TaskFilter = TaskFilter.ALL,
    val showCompleted: Boolean = true,
    val showAddSheet: Boolean = false,
    val nextId: Int = 1,
    // Add sheet specific state
    val newTaskText: String = "",
    val newTaskDescription: String = "",
    val newTaskPriority: TaskPriority = TaskPriority.MEDIUM
)