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
    // Collect the combined state which includes UI settings and the items list from DB
    val combinedState by viewModel.combinedState.collectAsStateWithLifecycle()
    val (uiState, items) = combinedState // Destructure the Pair

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Filter tasks based on the items list from combinedState and UI filter settings
    val filteredTasks = remember(items, uiState.filter, uiState.showCompleted) {
        items.filter { item -> // Use 'items' from combinedState
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
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CheckCircle,
                            null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("$completedTasks done", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.width(12.dp))
                        Icon(
                            Icons.Default.Schedule,
                            null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
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
            if (filteredTasks.isEmpty() && items.isNotEmpty()) { // Check 'items' from combinedState
                EmptyState(
                    icon = Icons.Outlined.FilterAltOff,
                    title = "No matching tasks",
                    message = "Adjust your filters or add new tasks."
                )
            } else if (items.isEmpty()) { // Check 'items' from combinedState
                EmptyState(
                    icon = Icons.AutoMirrored.Outlined.PlaylistAddCheck,
                    title = "Your task list is empty",
                    message = "Tap the + button to add your first task."
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp) // Padding for FAB
                ) {
                    val activeList = groupedTasks[false] ?: emptyList()
                    if (activeList.isNotEmpty()) {
                        item { ListHeader("Active Tasks (${activeList.size})") }
                        itemsIndexed(
                            items = activeList,
                            key = { _, item -> "active-${item.id}" }) { _, item ->
                            ToDoListItem(
                                modifier = Modifier.animateItem(),
                                item = item,
                                onToggleComplete = { toggledItem -> // Lambda now receives the item
                                    val originalStatus = toggledItem.isCompleted
                                    viewModel.toggleTaskCompletion(toggledItem) // Pass the whole item
                                    scope.launch {
                                        val message =
                                            if (!originalStatus) "Task '${toggledItem.text}' completed" else "Task '${toggledItem.text}' marked active"
                                        val result = snackbarHostState.showSnackbar(
                                            message,
                                            actionLabel = "Undo",
                                            duration = SnackbarDuration.Short
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            viewModel.toggleTaskCompletion(toggledItem) // Pass item to undo
                                        }
                                    }
                                },
                                onDeleteItem = { deletedItem -> // Lambda now receives the item
                                    viewModel.deleteTask(deletedItem.id)
                                    scope.launch {
                                        val result = snackbarHostState.showSnackbar(
                                            "Task '${deletedItem.text}' deleted",
                                            actionLabel = "Undo",
                                            duration = SnackbarDuration.Short
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            viewModel.reinsertItem(deletedItem) // Use reinsertItem to undo
                                        }
                                    }
                                }
                            )
                        }
                    }

                    val completedList = groupedTasks[true] ?: emptyList()
                    if (uiState.showCompleted && completedList.isNotEmpty()) {
                        item { Spacer(modifier = Modifier.height(16.dp)); ListHeader("Completed Tasks (${completedList.size})") }
                        itemsIndexed(
                            items = completedList,
                            key = { _, item -> "completed-${item.id}" }) { _, item ->
                            ToDoListItem(
                                modifier = Modifier.animateItem(),
                                item = item,
                                onToggleComplete = { toggledItem -> // Lambda now receives the item
                                    val originalStatus = toggledItem.isCompleted
                                    viewModel.toggleTaskCompletion(toggledItem) // Pass the whole item
                                    scope.launch {
                                        val message =
                                            if (!originalStatus) "Task '${toggledItem.text}' completed" else "Task '${toggledItem.text}' marked active"
                                        val result = snackbarHostState.showSnackbar(
                                            message,
                                            actionLabel = "Undo",
                                            duration = SnackbarDuration.Short
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            viewModel.toggleTaskCompletion(toggledItem) // Pass item to undo
                                        }
                                    }
                                },
                                onDeleteItem = { deletedItem -> // Lambda now receives the item
                                    viewModel.deleteTask(deletedItem.id)
                                    scope.launch {
                                        val result = snackbarHostState.showSnackbar(
                                            "Task '${deletedItem.text}' deleted",
                                            actionLabel = "Undo",
                                            duration = SnackbarDuration.Short
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            viewModel.reinsertItem(deletedItem) // Use reinsertItem to undo
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

// Preview needs adjustment as ViewModel now requires Application context
@Preview(showBackground = true, widthDp = 360, heightDp = 740)
@Composable
fun TodoListScreenPreview() {
    MethodicTheme {
        // Previewing components that require a ViewModel with Application context
        // is complex. Often, you'd preview smaller, stateless components or use
        // a fake ViewModel/DI framework for previews.
        // For now, just show a placeholder.
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("ToDo List Screen Preview (Requires ViewModel)")
        }
    }
}