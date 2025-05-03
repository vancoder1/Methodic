package com.vio_0x.methodic

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.KeyboardDoubleArrowUp
import androidx.compose.material.icons.filled.Remove
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Date

// --- ENUM DEFINITIONS ---
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

// --- UI State ---
data class MainUiState(
    val items: List<ToDoItem> = emptyList(),
    val filter: TaskFilter = TaskFilter.ALL,
    val showCompleted: Boolean = true,
    val showAddSheet: Boolean = false,
    val nextId: Int = 1,
    val newTaskText: String = "",
    val newTaskDescription: String = "",
    val newTaskPriority: TaskPriority = TaskPriority.MEDIUM
)

class MainViewModel : ViewModel() {

    // --- Private Mutable State ---
    private val _uiState = MutableStateFlow(MainUiState())

    // --- Public Immutable State ---
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

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
        _uiState.update { it.copy(showAddSheet = false) }
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
                    createdAt = Date() // Use current date
                )
                currentState.copy(
                    items = currentState.items + newItem,
                    nextId = currentState.nextId + 1,
                    // Reset add sheet fields
                    newTaskText = "",
                    newTaskDescription = "",
                    newTaskPriority = TaskPriority.MEDIUM,
                    showAddSheet = false // Hide sheet after adding
                )
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

    // Helper for Undo Delete
    fun addTaskFromItem(item: ToDoItem) {
        viewModelScope.launch {
            _uiState.update { currentState ->
                // Avoid adding duplicates if the item somehow already exists
                if (currentState.items.any { it.id == item.id }) {
                    currentState
                } else {
                    // Re-add the item
                    currentState.copy(items = currentState.items + item)
                }
            }
        }
    }
}