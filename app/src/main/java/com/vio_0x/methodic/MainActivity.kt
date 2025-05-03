package com.vio_0x.methodic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.automirrored.outlined.PlaylistAddCheck
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vio_0x.methodic.ui.theme.MethodicTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// --- Activity ---
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MethodicTheme {
                ToDoApp() // ViewModel is provided by default by viewModel()
            }
        }
    }
}

// --- Main App Composable ---

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ToDoApp(viewModel: MainViewModel = MainViewModel()) {
    // Get state from ViewModel
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Filter tasks based on ViewModel state (derived state in UI)
    val filteredTasks = remember(uiState.items, uiState.filter, uiState.showCompleted) {
        uiState.items.filter { item ->
            val matchesFilter = when (uiState.filter) {
                TaskFilter.ALL -> true
                TaskFilter.HIGH -> item.priority == TaskPriority.HIGH
                TaskFilter.MEDIUM -> item.priority == TaskPriority.MEDIUM
                TaskFilter.LOW -> item.priority == TaskPriority.LOW
            }

            val showBasedOnCompletion = if (uiState.showCompleted) {
                true
            } else {
                !item.isCompleted
            }

            matchesFilter && showBasedOnCompletion
        }
    }

    // Grouped tasks: active first, then completed (Sorted by creation date within groups)
    val groupedTasks = remember(filteredTasks) {
        filteredTasks.sortedBy { it.createdAt }.groupBy { it.isCompleted }
    }

    // Count tasks by status (derived from filtered list for consistency with display)
    // Note: These counts reflect the *filtered* list, not the total list.
    // If total counts are needed, derive them directly from uiState.items
    val activeTasks = filteredTasks.count { !it.isCompleted }
    val completedTasks = filteredTasks.count { it.isCompleted }

    // Bottom sheet state remains local to the UI
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // SnackBar host state remains local to the UI
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope() // Scope for launching snackbars

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Methodic",
                            fontWeight = FontWeight.Bold
                        )
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
                    // Toggle Completed Visibility Button
                    IconButton(onClick = { viewModel.toggleShowCompleted() }) {
                        Icon(
                            imageVector = if (uiState.showCompleted) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = "Toggle Completed Tasks",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Filter Dropdown Menu
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
                    // Task statistics (reflecting filtered list)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$completedTasks done",
                            style = MaterialTheme.typography.bodySmall
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$activeTasks active",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showAddTaskSheet() }, // Use ViewModel action
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Task"
                )
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
            // --- Empty States ---
            if (filteredTasks.isEmpty() && uiState.items.isNotEmpty()) {
                // Empty state when filters match no tasks
                EmptyState(
                    icon = Icons.Outlined.FilterAltOff,
                    title = "No matching tasks",
                    message = "Adjust your filters or add new tasks to see them here."
                )
            } else if (uiState.items.isEmpty()) {
                // Empty state when there are no tasks at all
                EmptyState(
                    icon = Icons.AutoMirrored.Outlined.PlaylistAddCheck,
                    title = "Your task list is empty",
                    message = "Tap the + button to add your first task."
                )
            }
            // --- Task List ---
            else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp) // Padding for FAB overlap
                ) {
                    // Active tasks section
                    val activeList = groupedTasks[false] ?: emptyList()
                    if (activeList.isNotEmpty()) {
                        item {
                            ListHeader("Active Tasks (${activeList.size})")
                        }
                        itemsIndexed(
                            items = activeList,
                            key = { _, item -> "active-${item.id}" }
                        ) { _, item ->
                            ToDoListItem(
                                modifier = Modifier.animateItem(),
                                item = item,
                                onToggleComplete = {
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
                                onDeleteItem = {
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

                    // Completed tasks section
                    val completedList = groupedTasks[true] ?: emptyList()
                    if (uiState.showCompleted && completedList.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            ListHeader("Completed Tasks (${completedList.size})")
                        }
                        itemsIndexed(
                            items = completedList,
                            key = { _, item -> "completed-${item.id}" }
                        ) { _, item ->
                            ToDoListItem(
                                modifier = Modifier.animateItem(),
                                item = item,
                                onToggleComplete = {
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
                                onDeleteItem = {
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

    // --- Add Task Bottom Sheet ---
    if (uiState.showAddSheet) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.hideAddTaskSheet() },
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .padding(WindowInsets.navigationBars.asPaddingValues()) // Handle insets
            ) {
                Text(
                    text = "New Task",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Task Title Input
                OutlinedTextField(
                    value = uiState.newTaskText,
                    onValueChange = { viewModel.updateNewTaskText(it) },
                    label = { Text("Task Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Task Description Input
                OutlinedTextField(
                    value = uiState.newTaskDescription,
                    onValueChange = { viewModel.updateNewTaskDescription(it) },
                    label = { Text("Description (Optional)") },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    leadingIcon = { Icon(Icons.AutoMirrored.Filled.Notes, contentDescription = null, modifier = Modifier.padding(top = 12.dp)) },
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Priority Selection
                Text("Priority", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TaskPriority.entries.forEach { priority ->
                        PriorityChip(
                            priority = priority,
                            selected = uiState.newTaskPriority == priority,
                            onClick = { viewModel.updateNewTaskPriority(priority) },
                            modifier = Modifier.weight(1f) // Distribute space evenly
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Add Task Button
                Button(
                    onClick = {
                        viewModel.addTask()
                        // Sheet hiding is handled in ViewModel
                    },
                    enabled = uiState.newTaskText.isNotBlank(), // Enable only if title is present
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Add Task")
                }

                Spacer(modifier = Modifier.height(8.dp)) // Bottom padding
            }
        }
    }
}

// --- UI Helper Composables ---

@Composable
fun EmptyState(icon: ImageVector, title: String, message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(80.dp).alpha(0.6f),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun ListHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        modifier = modifier.padding(vertical = 8.dp)
    )
}


@Composable
fun ToDoListItem(
    item: ToDoItem,
    onToggleComplete: () -> Unit,
    onDeleteItem: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val rotationState by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f, label = "Rotation"
    )
    val alphaState by animateFloatAsState(
        targetValue = if (item.isCompleted) 0.6f else 1f, label = "Alpha"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .alpha(alphaState)
            .clickable { expanded = !expanded }, // Expand/collapse on click
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Checkbox
                Checkbox(
                    checked = item.isCompleted,
                    onCheckedChange = { onToggleComplete() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.primary,
                        uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Task Text and Priority Badge
                Column(modifier = Modifier.weight(1.0f)) {
                    Text(
                        text = item.text,
                        style = MaterialTheme.typography.bodyLarge,
                        textDecoration = if (item.isCompleted) TextDecoration.LineThrough else null,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    // Priority Badge
                    val priority = item.priority // Get from item
                    if (priority != TaskPriority.MEDIUM) { // Only show non-medium
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = priority.icon,
                                contentDescription = "Priority: ${priority.displayName}",
                                tint = priority.color,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = priority.displayName,
                                style = MaterialTheme.typography.bodySmall,
                                color = priority.color
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Expand/Collapse Icon
                IconButton(
                    onClick = { expanded = !expanded },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        modifier = Modifier.rotate(rotationState),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Delete Button
                IconButton(
                    onClick = onDeleteItem,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete Task",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            // --- Expanded Content ---
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 8.dp, start = 48.dp)) { // Indent
                    // Description
                    if (!item.description.isNullOrBlank()) {
                        Text(
                            text = item.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    // Dates
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Created: ${formatDate(item.createdAt)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                        if (item.isCompleted && item.completedAt != null) {
                            Text(
                                text = "Completed: ${formatDate(item.completedAt)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PriorityChip(
    priority: TaskPriority,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color = priority.color
    val icon = priority.icon
    val text = priority.displayName

    val targetBackgroundColor = if (selected) color.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
    val animatedBackgroundColor by animateColorAsState(targetBackgroundColor, label = "Chip Background ${priority.name}")

    val targetBorderColor = if (selected) color else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    val animatedBorderColor by animateColorAsState(targetBorderColor, label = "Chip Border ${priority.name}")

    val targetTextColor = if (selected) color else MaterialTheme.colorScheme.onSurfaceVariant
    val animatedTextColor by animateColorAsState(targetTextColor, label = "Chip Text ${priority.name}")

    Surface(
        modifier = modifier.height(36.dp),
        onClick = onClick,
        shape = CircleShape,
        color = animatedBackgroundColor,
        border = BorderStroke(1.dp, animatedBorderColor),
        tonalElevation = if (selected) 2.dp else 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = animatedTextColor,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = text,
                color = animatedTextColor,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}


// --- Utility Functions ---

fun formatDate(date: Date): String {
    val today = Calendar.getInstance()
    val otherDay = Calendar.getInstance().apply { time = date }

    val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    val fullFormatter = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())

    return when {
        isSameDay(today, otherDay) -> "Today, ${timeFormatter.format(date)}"
        isYesterday(today, otherDay) -> "Yesterday, ${timeFormatter.format(date)}"
        else -> fullFormatter.format(date)
    }
}

fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
           cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

fun isYesterday(today: Calendar, otherDay: Calendar): Boolean {
    val yesterday = Calendar.getInstance().apply {
        time = today.time
        add(Calendar.DAY_OF_YEAR, -1)
    }
    return isSameDay(yesterday, otherDay)
}


// --- Previews ---
// Previews need state provided manually since viewModel() doesn't work directly.

@Preview(showBackground = true, widthDp = 360, heightDp = 740)
@Composable
fun ToDoAppPreview() {
    MethodicTheme {
        // Placeholder for preview - requires manual state or a fake ViewModel
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("ToDo App Preview (ViewModel Required)")
        }
        // Example: To make this work, you might extract the Scaffold content
        // into a separate composable that accepts uiState and lambdas for actions.
        // ToDoAppContent(uiState = MainUiState(...), onAction = { /* handle actions */ })
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 740)
@Composable
fun ToDoAppEmptyPreview() {
     MethodicTheme {
         // Placeholder for empty state preview
         Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("ToDo App Empty Preview (ViewModel Required)")
        }
         // ToDoAppContent(uiState = MainUiState(items = emptyList()), onAction = { })
     }
}


@Preview(showBackground = true)
@Composable
fun ToDoListItemPreview() {
    MethodicTheme {
        ToDoListItem(
            item = ToDoItem(1, "Buy groceries", "Milk, Bread, Eggs", isCompleted = false, priority = TaskPriority.HIGH),
            onToggleComplete = {},
            onDeleteItem = {}
        )
    }
}
@Preview(showBackground = true)
@Composable
fun ToDoListItemCompletedPreview() {
    MethodicTheme {
        ToDoListItem(
            item = ToDoItem(2, "Walk the dog", isCompleted = true, priority = TaskPriority.LOW, createdAt = Date(System.currentTimeMillis() - 86400000), completedAt = Date()),
            onToggleComplete = {},
            onDeleteItem = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PriorityChipPreview() {
    MethodicTheme {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(8.dp)) {
            PriorityChip(priority = TaskPriority.HIGH, selected = true, onClick = {})
            PriorityChip(priority = TaskPriority.MEDIUM, selected = false, onClick = {})
            PriorityChip(priority = TaskPriority.LOW, selected = false, onClick = {})
        }
    }
}