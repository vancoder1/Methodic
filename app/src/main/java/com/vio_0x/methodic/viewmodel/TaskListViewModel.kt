package com.vio_0x.methodic.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vio_0x.methodic.data.TaskFilter
import com.vio_0x.methodic.data.TaskPriority
import com.vio_0x.methodic.data.ToDoItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date

// Renamed from MainViewModel
class TaskListViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(TaskListUiState())
    val uiState: StateFlow<TaskListUiState> = _uiState.asStateFlow()

    // --- Events / Actions ---

    fun updateNewTaskText(text: String) {
        _uiState.update { it.copy(newTaskText = text) }
    }

    fun updateNewTaskDescription(description: String) {
        _uiState.update { it.copy(newTaskDescription = description) }
    }

    fun updateNewTaskPriority(priority: TaskPriority) {
        _uiState.update { it.copy(newTaskPriority = priority) }
    }

    fun showAddTaskSheet() {
        _uiState.update { it.copy(showAddSheet = true) }
    }

    fun hideAddTaskSheet() {
        // Reset fields when hiding the sheet explicitly
        _uiState.update {
            it.copy(
                showAddSheet = false,
                newTaskText = "",
                newTaskDescription = "",
                newTaskPriority = TaskPriority.MEDIUM
            )
        }
    }

    fun addTask() {
        viewModelScope.launch {
            _uiState.update { currentState ->
                if (currentState.newTaskText.isBlank()) {
                    return@update currentState // Don't add empty tasks
                }
                val newItem = ToDoItem(
                    id = currentState.nextId,
                    text = currentState.newTaskText,
                    description = currentState.newTaskDescription.takeIf { it.isNotBlank() },
                    priority = currentState.newTaskPriority,
                    createdAt = Date()
                )
                currentState.copy(
                    items = currentState.items + newItem,
                    nextId = currentState.nextId + 1,
                    // Reset add sheet fields & hide sheet
                    newTaskText = "",
                    newTaskDescription = "",
                    newTaskPriority = TaskPriority.MEDIUM,
                    showAddSheet = false
                )
            }
        }
    }

    fun addTaskFromItem(item: ToDoItem) {
        viewModelScope.launch {
            _uiState.update { currentState ->
                if (currentState.items.any { it.id == item.id }) {
                    currentState // Avoid duplicates
                } else {
                    currentState.copy(items = currentState.items + item)
                    // Consider sorting or placing it back if needed
                }
            }
        }
    }

    fun toggleTaskCompletion(itemId: Int) {
        viewModelScope.launch {
            _uiState.update { currentState ->
                val updatedItems = currentState.items.map { item ->
                    if (item.id == itemId) {
                        item.copy(
                            isCompleted = !item.isCompleted,
                            completedAt = if (!item.isCompleted) Date() else null
                        )
                    } else {
                        item
                    }
                }
                currentState.copy(items = updatedItems)
            }
        }
    }

    fun deleteTask(itemId: Int) {
        viewModelScope.launch {
            _uiState.update { currentState ->
                val updatedItems = currentState.items.filterNot { it.id == itemId }
                currentState.copy(items = updatedItems)
            }
        }
    }

    fun setFilter(filter: TaskFilter) {
        _uiState.update { it.copy(filter = filter) }
    }

    fun toggleShowCompleted() {
        _uiState.update { it.copy(showCompleted = !it.showCompleted) }
    }
}