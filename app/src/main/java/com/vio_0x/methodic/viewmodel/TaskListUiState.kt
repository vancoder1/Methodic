package com.vio_0x.methodic.viewmodel

import com.vio_0x.methodic.data.TaskFilter
import com.vio_0x.methodic.data.TaskPriority

// Renamed from MainUiState
data class TaskListUiState(
    // items list is now observed from the database Flow
    val filter: TaskFilter = TaskFilter.ALL,
    val showCompleted: Boolean = true,
    val showAddSheet: Boolean = false,
    // nextId is handled by Room's autoGenerate
    // Add sheet specific state
    val newTaskText: String = "",
    val newTaskDescription: String = "",
    val newTaskPriority: TaskPriority = TaskPriority.MEDIUM
)