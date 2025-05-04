package com.vio_0x.methodic.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.PlaylistAddCheck
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.FilterAltOff
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vio_0x.methodic.data.TaskFilter
import com.vio_0x.methodic.ui.components.EmptyState
import com.vio_0x.methodic.ui.components.ListHeader
import com.vio_0x.methodic.ui.components.ToDoListItem
import com.vio_0x.methodic.ui.theme.MethodicTheme
import com.vio_0x.methodic.viewmodel.TaskListViewModel
import kotlinx.coroutines.launch

// Renamed from ToDoApp
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TodoListScreen(
    viewModel: TaskListViewModel = viewModel() // Use the renamed ViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Filter tasks based on ViewModel state
    val filteredTasks = remember(uiState.items, uiState.filter, uiState.showCompleted) {
        uiState.items.filter { item ->
            val matchesFilter = when (uiState.filter) {
                TaskFilter.ALL -> true
                TaskFilter.HIGH -> item.priority == com.vio_0x.methodic.data.TaskPriority.HIGH
                TaskFilter.MEDIUM -> item.priority == com.vio_0x.methodic.data.TaskPriority.MEDIUM
                TaskFilter.LOW -> item.priority == com.vio_0x.methodic.data.TaskPriority.LOW
            }
            val showBasedOnCompletion = if (uiState.showCompleted) true else !item.isCompleted
            matchesFilter && showBasedOnCompletion
        }
    }

    val groupedTasks = remember(filteredTasks) {
        filteredTasks.sortedBy { it.createdAt }.groupBy { it.isCompleted }
    }

    val activeTasks = filteredTasks.count { !it.isCompleted }
    val completedTasks = filteredTasks.count { it.isCompleted }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Methodic", fontWeight = FontWeight.Bold)
                        Text(
                            "Your smart task manager",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.alpha(0.7f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                actions = {
                    IconButton(onClick = { viewModel.toggleShowCompleted() }) {
                        Icon(
                            imageVector = if (uiState.showCompleted) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = "Toggle Completed Tasks",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    var showFilterMenu by remember { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = { showFilterMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Filter Tasks",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        DropdownMenu(
                            expanded = showFilterMenu,
                            onDismissRequest = { showFilterMenu = false }
                        ) {
                            TaskFilter.entries.forEach { filter ->
                                DropdownMenuItem(
                                    text = { Text(filter.displayName) },
                                    onClick = {
                                        viewModel.setFilter(filter)
                                        showFilterMenu = false
                                    },
                                    leadingIcon = {
                                        if (uiState.filter == filter) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        } else {
                                            Spacer(Modifier.size(24.dp)) // Keep alignment
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                tonalElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("$completedTasks done", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.width(12.dp))
                        Icon(Icons.Default.Schedule, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("$activeTasks active", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showAddTaskSheet() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Task")
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (filteredTasks.isEmpty() && uiState.items.isNotEmpty()) {
                EmptyState(
                    icon = Icons.Outlined.FilterAltOff,
                    title = "No matching tasks",
                    message = "Adjust your filters or add new tasks."
                )
            } else if (uiState.items.isEmpty()) {
                EmptyState(
                    icon = Icons.AutoMirrored.Outlined.PlaylistAddCheck,
                    title = "Your task list is empty",
                    message = "Tap the + button to add your first task."
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp) // Padding for FAB
                ) {
                    val activeList = groupedTasks[false] ?: emptyList()
                    if (activeList.isNotEmpty()) {
                        item { ListHeader("Active Tasks (${activeList.size})") }
                        itemsIndexed(items = activeList, key = { _, item -> "active-${item.id}" }) { _, item ->
                            ToDoListItem(
                                modifier = Modifier.animateItem(),
                                item = item,
                                onToggleComplete = { /* ... Snackbar logic ... */
                                    val originalStatus = item.isCompleted
                                    viewModel.toggleTaskCompletion(item.id)
                                    scope.launch {
                                        val message = if (!originalStatus) "Task '${item.text}' completed" else "Task '${item.text}' marked active"
                                        val result = snackbarHostState.showSnackbar(message, actionLabel = "Undo", duration = SnackbarDuration.Short)
                                        if (result == SnackbarResult.ActionPerformed) {
                                            viewModel.toggleTaskCompletion(item.id) // Undo
                                        }
                                    }
                                },
                                onDeleteItem = { /* ... Snackbar logic ... */
                                    val removedItem = item
                                    viewModel.deleteTask(item.id)
                                    scope.launch {
                                        val result = snackbarHostState.showSnackbar("Task '${removedItem.text}' deleted", actionLabel = "Undo", duration = SnackbarDuration.Short)
                                        if (result == SnackbarResult.ActionPerformed) {
                                            viewModel.addTaskFromItem(removedItem) // Undo
                                        }
                                    }
                                }
                            )
                        }
                    }

                    val completedList = groupedTasks[true] ?: emptyList()
                    if (uiState.showCompleted && completedList.isNotEmpty()) {
                        item { Spacer(modifier = Modifier.height(16.dp)); ListHeader("Completed Tasks (${completedList.size})") }
                        itemsIndexed(items = completedList, key = { _, item -> "completed-${item.id}" }) { _, item ->
                            ToDoListItem(
                                modifier = Modifier.animateItem(),
                                item = item,
                                onToggleComplete = { /* ... Snackbar logic ... */
                                    val originalStatus = item.isCompleted
                                    viewModel.toggleTaskCompletion(item.id)
                                    scope.launch {
                                        val message = if (!originalStatus) "Task '${item.text}' completed" else "Task '${item.text}' marked active"
                                        val result = snackbarHostState.showSnackbar(message, actionLabel = "Undo", duration = SnackbarDuration.Short)
                                        if (result == SnackbarResult.ActionPerformed) {
                                            viewModel.toggleTaskCompletion(item.id) // Undo
                                        }
                                    }
                                },
                                onDeleteItem = { /* ... Snackbar logic ... */
                                    val removedItem = item
                                    viewModel.deleteTask(item.id)
                                    scope.launch {
                                        val result = snackbarHostState.showSnackbar("Task '${removedItem.text}' deleted", actionLabel = "Undo", duration = SnackbarDuration.Short)
                                        if (result == SnackbarResult.ActionPerformed) {
                                            viewModel.addTaskFromItem(removedItem) // Undo
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Add Task Bottom Sheet Logic
    if (uiState.showAddSheet) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.hideAddTaskSheet() }, // Use VM action
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
        ) {
            // Content is now in AddTaskSheetContent
            AddTaskSheetContent(
                uiState = uiState, // Pass only the state needed
                onUpdateTaskText = viewModel::updateNewTaskText, // Pass VM functions
                onUpdateTaskDescription = viewModel::updateNewTaskDescription,
                onUpdateTaskPriority = viewModel::updateNewTaskPriority,
                onAddTask = viewModel::addTask
            )
        }
    }
}

// Preview might need a fake ViewModel or state for proper rendering
@Preview(showBackground = true, widthDp = 360, heightDp = 740)
@Composable
fun TodoListScreenPreview() {
    MethodicTheme {
        // This preview will likely show an empty state or require
        // providing a fake ViewModel/State for a more meaningful preview.
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("ToDo List Screen Preview (ViewModel Required)")
        }
        // Example of providing state (requires a mechanism to fake the ViewModel):
        // TodoListScreenContent(uiState = TaskListUiState(items = listOf(...)), onAction = {})
    }
}