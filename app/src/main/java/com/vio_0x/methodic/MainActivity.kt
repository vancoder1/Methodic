package com.vio_0x.methodic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vio_0x.methodic.ui.theme.MethodicTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.BorderStroke // <-- ADDED Import for BorderStroke
import androidx.compose.material.icons.automirrored.outlined.PlaylistAddCheck


// --- Activity ---
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MethodicTheme {
                ToDoApp()
            }
        }
    }
}

// --- Main App Composable ---

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ToDoApp() {
    // State for the app
    var newTodoText by rememberSaveable { mutableStateOf("") }
    var newTodoDescription by rememberSaveable { mutableStateOf("") }
    var selectedPriority by rememberSaveable { mutableStateOf(TaskPriority.MEDIUM) }
    var todoItems = remember { mutableStateListOf<ToDoItem>() }
    var nextId by rememberSaveable { mutableStateOf(1) }
    var showAddSheet by rememberSaveable { mutableStateOf(false) }
    var showCompletedTasks by rememberSaveable { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    var selectedFilter by rememberSaveable { mutableStateOf(TaskFilter.ALL) }

    // Filter tasks based on selected filter
    val filteredTasks = remember(todoItems.toList(), selectedFilter, showCompletedTasks) { // Use toList() for stability
        todoItems.filter { item ->
            val matchesFilter = when (selectedFilter) {
                TaskFilter.ALL -> true
                TaskFilter.HIGH -> item.priority == TaskPriority.HIGH
                TaskFilter.MEDIUM -> item.priority == TaskPriority.MEDIUM
                TaskFilter.LOW -> item.priority == TaskPriority.LOW
            }

            val showBasedOnCompletion = if (showCompletedTasks) {
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

    // Count tasks by status
    val activeTasks = todoItems.count { !it.isCompleted }
    val completedTasks = todoItems.count { it.isCompleted }

    // Bottom sheet for adding new tasks
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // SnackBar host state
    val snackbarHostState = remember { SnackbarHostState() }

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
                    // Filter button
                    IconButton(onClick = { showCompletedTasks = !showCompletedTasks }) {
                        Icon(
                            imageVector = if (showCompletedTasks) Icons.Filled.Visibility else Icons.Filled.VisibilityOff, // Changed icons
                            contentDescription = "Toggle Completed Tasks",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Filter dropdown menu
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
                                        selectedFilter = filter
                                        showFilterMenu = false
                                    },
                                    leadingIcon = {
                                        if (selectedFilter == filter) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected", // Better description
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        } else {
                                            // Add spacer to align items when icon is not present
                                            Spacer(Modifier.size(24.dp)) // Adjust size to match icon
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
                contentColor = MaterialTheme.colorScheme.onSurface, // Set default content color
                tonalElevation = 4.dp // Add some elevation
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Task statistics
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
                    // Add Task FAB is automatically positioned by Scaffold when using BottomAppBar
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSheet = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Task"
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End // Keep if you specifically want it anchored to the end
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background) // Use background color
        ) {
            // Main content - Task list
            if (filteredTasks.isEmpty() && todoItems.isNotEmpty()) { // Adjusted empty state logic
                // Empty state when filters result in no tasks
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.FilterAltOff, // Different icon
                        contentDescription = null,
                        modifier = Modifier
                            .size(80.dp)
                            .alpha(0.6f),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No matching tasks",
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Adjust your filters or add new tasks to see them here.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            } else if (todoItems.isEmpty()) {
                // Empty state when there are no tasks at all
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.PlaylistAddCheck, // Different icon
                        contentDescription = null,
                        modifier = Modifier
                            .size(80.dp)
                            .alpha(0.6f),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Your task list is empty",
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tap the + button to add your first task.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
            else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp) // Add bottom padding for FAB
                ) {
                    // Active tasks section
                    val activeList = groupedTasks[false] ?: emptyList()
                    if (activeList.isNotEmpty()) {
                        item {
                            Text(
                                text = "Active Tasks (${activeList.size})", // Show count
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }

                        itemsIndexed(
                            items = activeList,
                            key = { _, item -> "active-${item.id}" } // More specific key
                        ) { _, item -> // Removed index as it's not used directly
                            ToDoListItem(
                                modifier = Modifier.animateItemPlacement(),
                                item = item,
                                onToggleComplete = {
                                    val index = todoItems.indexOfFirst { it.id == item.id } // Safer find
                                    if (index != -1) {
                                        todoItems[index] = item.copy(isCompleted = !item.isCompleted)
                                        scope.launch {
                                            val result = snackbarHostState.showSnackbar(
                                                message = "Task '${item.text}' completed",
                                                actionLabel = "Undo",
                                                duration = SnackbarDuration.Short
                                            )
                                            if (result == SnackbarResult.ActionPerformed) {
                                                // Find again in case list changed
                                                val undoIndex = todoItems.indexOfFirst { it.id == item.id }
                                                if (undoIndex != -1) {
                                                    todoItems[undoIndex] = todoItems[undoIndex].copy(isCompleted = false)
                                                }
                                            }
                                        }
                                    }
                                },
                                onDeleteItem = {
                                    val removedItem = item
                                    val index = todoItems.indexOfFirst { it.id == item.id } // Safer find
                                    if (index != -1) {
                                        todoItems.removeAt(index)
                                        scope.launch {
                                            val result = snackbarHostState.showSnackbar(
                                                message = "Task '${removedItem.text}' deleted",
                                                actionLabel = "Undo",
                                                duration = SnackbarDuration.Short
                                            )
                                            if (result == SnackbarResult.ActionPerformed) {
                                                // Try to add back at original position if possible
                                                todoItems.add(index.coerceAtMost(todoItems.size), removedItem)
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }

                    // Completed tasks section
                    val completedList = groupedTasks[true] ?: emptyList()
                    if (showCompletedTasks && completedList.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(16.dp)) // Add space between sections
                            Text(
                                text = "Completed Tasks (${completedList.size})", // Show count
                                style = MaterialTheme.typography.titleMedium,
                                // *** FIXED PADDING ***
                                modifier = Modifier.padding(vertical = 8.dp) // Use named params
                            )
                        }

                        itemsIndexed(
                            items = completedList,
                            key = { _, item -> "completed-${item.id}" } // More specific key
                        ) { _, item -> // Removed index as it's not used directly
                            ToDoListItem(
                                modifier = Modifier.animateItemPlacement(),
                                item = item,
                                onToggleComplete = {
                                    val index = todoItems.indexOfFirst { it.id == item.id } // Safer find
                                    if (index != -1) {
                                        todoItems[index] = item.copy(isCompleted = !item.isCompleted)
                                        scope.launch {
                                            val result = snackbarHostState.showSnackbar(
                                                message = "Task '${item.text}' marked active",
                                                actionLabel = "Undo",
                                                duration = SnackbarDuration.Short
                                            )
                                            if (result == SnackbarResult.ActionPerformed) {
                                                // Find again in case list changed
                                                val undoIndex = todoItems.indexOfFirst { it.id == item.id }
                                                if (undoIndex != -1) {
                                                    todoItems[undoIndex] = todoItems[undoIndex].copy(isCompleted = true)
                                                }
                                            }
                                        }
                                    }
                                },
                                onDeleteItem = {
                                    val removedItem = item
                                    val index = todoItems.indexOfFirst { it.id == item.id } // Safer find
                                    if (index != -1) {
                                        todoItems.removeAt(index)
                                        scope.launch {
                                            val result = snackbarHostState.showSnackbar(
                                                message = "Task '${removedItem.text}' deleted",
                                                actionLabel = "Undo",
                                                duration = SnackbarDuration.Short
                                            )
                                            if (result == SnackbarResult.ActionPerformed) {
                                                // Try to add back at original position if possible
                                                todoItems.add(index.coerceAtMost(todoItems.size), removedItem)
                                            }
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

    // Bottom sheet for adding new tasks
    if (showAddSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddSheet = false },
            sheetState = sheetState,
            // Improve appearance
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    // Add padding for system bars if using edge-to-edge
                    .padding(WindowInsets.navigationBars.asPaddingValues())
            ) {
                Text(
                    text = "New Task",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally) // Center title
                )

                Spacer(modifier = Modifier.height(24.dp)) // Increased spacing

                // Task title
                OutlinedTextField(
                    value = newTodoText,
                    onValueChange = { newTodoText = it },
                    label = { Text("Task Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Default.Assignment, contentDescription = null)
                    },
                    shape = RoundedCornerShape(12.dp) // Consistent rounding
                )

                Spacer(modifier = Modifier.height(12.dp)) // Adjusted spacing

                // Task description
                OutlinedTextField(
                    value = newTodoDescription,
                    onValueChange = { newTodoDescription = it },
                    label = { Text("Description (Optional)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    leadingIcon = {
                        Icon(Icons.Default.Notes, contentDescription = null, modifier=Modifier.padding(top=12.dp)) // Align icon better
                    },
                    shape = RoundedCornerShape(12.dp) // Consistent rounding
                )

                Spacer(modifier = Modifier.height(20.dp)) // Adjusted spacing

                // Priority selection
                Text(
                    text = "Priority",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TaskPriority.values().forEach { priority ->
                        PriorityChip(
                            priority = priority,
                            selected = priority == selectedPriority,
                            onClick = { selectedPriority = priority },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp)) // Increased spacing

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End, // Keep buttons to the end
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { showAddSheet = false },
                        shape = RoundedCornerShape(50) // Pill shape
                    ) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (newTodoText.isNotBlank()) {
                                val newItem = ToDoItem(
                                    id = nextId++,
                                    text = newTodoText.trim(), // Trim whitespace
                                    description = newTodoDescription.trim().ifBlank { null }, // Trim and set null if blank
                                    priority = selectedPriority,
                                    createdAt = Date()
                                )
                                // Add new items to the top of the active list
                                todoItems.add(0, newItem)

                                // Reset form
                                newTodoText = ""
                                newTodoDescription = ""
                                selectedPriority = TaskPriority.MEDIUM
                                showAddSheet = false // Dismiss sheet

                                // Show confirmation
                                scope.launch {
                                    // Hide keyboard if necessary (requires Context)
                                    // val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                    // imm.hideSoftInputFromWindow(view.windowToken, 0)
                                    snackbarHostState.showSnackbar(
                                        message = "Task added successfully",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                        },
                        enabled = newTodoText.isNotBlank(),
                        shape = RoundedCornerShape(50) // Pill shape
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text("Add Task")
                    }
                }

                // Spacer(modifier = Modifier.height(16.dp)) // Add padding at the bottom, handled by navigationBars padding now
            }
        }
    }
}

// --- List Item Composable ---

@OptIn(ExperimentalMaterial3Api::class) // Needed for AssistChip
@Composable
fun ToDoListItem(
    item: ToDoItem,
    onToggleComplete: () -> Unit,
    onDeleteItem: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val rotationState by animateFloatAsState(
        targetValue = if (expanded && item.description != null) 180f else 0f, // Only rotate if expandable
        label = "Rotation Animation",
        animationSpec = tween(durationMillis = 300) // Smooth animation
    )
    val alphaState by animateFloatAsState(
        targetValue = if (item.isCompleted) 0.6f else 1f,
        label = "Alpha Animation"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .alpha(alphaState) // Apply alpha animation
            .clickable(enabled = item.description != null) { expanded = !expanded }, // Click whole card to expand if description exists
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp,
            draggedElevation = 6.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp), // Subtle elevation color
        )
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp) // Adjusted padding
        ) {
            // Main row with checkbox, title and actions
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp), // Inner padding
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Priority indicator
                Box(
                    modifier = Modifier
                        .size(8.dp) // Slightly smaller
                        .clip(CircleShape)
                        .background(item.priority.color.copy(alpha = if (item.isCompleted) 0.4f else 0.9f)) // Adjusted alpha
                )

                // Checkbox with animation
                Checkbox(
                    checked = item.isCompleted,
                    onCheckedChange = { onToggleComplete() },
                    modifier = Modifier.size(40.dp), // Make checkbox tap target bigger
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.primary,
                        uncheckedColor = MaterialTheme.colorScheme.outline
                    )
                )

                // Spacer(modifier = Modifier.width(4.dp)) // Reduced space

                // Task title with decoration for completed tasks
                Text(
                    text = item.text,
                    modifier = Modifier.weight(1f),
                    maxLines = 1, // Keep title single line here
                    overflow = TextOverflow.Ellipsis,
                    style = if (item.isCompleted) {
                        MaterialTheme.typography.bodyLarge.copy(
                            textDecoration = TextDecoration.LineThrough,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    } else {
                        MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium) // Slightly bolder active task
                    }
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Due date indicator if available
                item.dueDate?.let {
                    val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())
                    val formattedDate = dateFormat.format(it)
                    val isPastDue = it.before(Calendar.getInstance().time) && !item.isCompleted // Use Calendar for consistency

                    // *** FIXED CHIP ***
                    AssistChip( // Use AssistChip for displaying info
                        onClick = { /* Maybe show calendar or details? */ },
                        modifier = Modifier.height(28.dp), // Control chip height
                        label = {
                            Text(
                                text = formattedDate,
                                style = MaterialTheme.typography.labelSmall // Use labelSmall style
                                // Color is handled by chip colors
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = if (isPastDue) "Date Past Due" else "Due Date",
                                modifier = Modifier.size(16.dp)
                                // Tint is handled by chip colors
                            )
                        },
                        shape = RoundedCornerShape(8.dp), // Rounded chip
                        border = null, // Remove default border or customize
                        colors = AssistChipDefaults.assistChipColors( // Use AssistChipDefaults
                            containerColor = if (isPastDue)
                                MaterialTheme.colorScheme.errorContainer.copy(alpha=0.7f)
                            else
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.7f),
                            labelColor = if (isPastDue)
                                MaterialTheme.colorScheme.onErrorContainer
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            leadingIconContentColor = if (isPastDue)
                                MaterialTheme.colorScheme.onErrorContainer
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant // Match label color or use primary
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }

                // Delete icon
                IconButton(
                    onClick = onDeleteItem,
                    modifier = Modifier.size(40.dp) // Increase tap target
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline, // Use outlined version
                        contentDescription = "Delete Task",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Expand/collapse icon for description (moved to end)
                if (item.description != null) {
                    // IconButton placed inside the clickable area of the row already handles expand/collapse
                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        // *** FIXED ROTATE ***
                        modifier = Modifier
                            .size(24.dp)
                            .rotate(rotationState), // Apply rotation modifier
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    // Add spacer if no expand icon to align delete button consistently
                    Spacer(modifier = Modifier.size(24.dp))
                }

            }

            // Expandable content with description
            AnimatedVisibility(
                visible = expanded && item.description != null,
                enter = expandVertically(animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)),
                exit = shrinkVertically(animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        // Consistent padding start with checkbox + priority
                        .padding(start = 12.dp + 40.dp, top = 4.dp, end = 16.dp, bottom = 8.dp)
                ) {
                    // Display full title again if it was truncated
                    Text(
                        text = item.text,
                        style = if (item.isCompleted) {
                            MaterialTheme.typography.bodyLarge.copy(
                                textDecoration = TextDecoration.LineThrough,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                fontWeight = FontWeight.Medium
                            )
                        } else {
                            MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                        },
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    // Description
                    Text(
                        text = item.description ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f) // Slightly more emphasis
                    )

                    // Created date
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.EditCalendar, // Different Icon
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), // Adjusted alpha
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Created: ${formatDate(item.createdAt)}", // Add label
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), // Adjusted alpha
                            // fontSize = 10.sp // Use theme typography
                        )
                    }
                }
            }
        }
    }
}

// --- Priority Chip Composable ---

@Composable
fun PriorityChip(
    priority: TaskPriority,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val targetBackgroundColor = if (selected) {
        priority.color.copy(alpha = 0.2f)
    } else {
        MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp) // Use surface elevation
    }
    val backgroundColor by animateColorAsState(targetBackgroundColor, label = "PriorityChipBackground")


    val targetBorderColor = if (selected) {
        priority.color
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    }
    val borderColor by animateColorAsState(targetBorderColor, label = "PriorityChipBorder")

    val targetTextColor = if (selected) {
        priority.color
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val textColor by animateColorAsState(targetTextColor, label = "PriorityChipText")

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = backgroundColor,
        // *** FIXED BORDERSTROKE ***
        border = BorderStroke(1.dp, borderColor), // BorderStroke is in androidx.compose.foundation
        modifier = modifier
            .height(40.dp) // Give fixed height for consistency
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp) // Vertical padding handled by fixed height
                .fillMaxSize(), // Fill height
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = priority.icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(18.dp) // Slightly larger icon
            )
            Spacer(modifier = Modifier.width(6.dp)) // Adjusted spacing
            Text(
                text = priority.displayName,
                style = MaterialTheme.typography.labelMedium,
                color = textColor,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

// --- Helper Functions ---

// Helper function to format date
fun formatDate(date: Date): String {
    val now = Calendar.getInstance()
    val then = Calendar.getInstance().apply { time = date }

    return when {
        isSameDay(now, then) -> "today"
        isYesterday(now, then) -> "yesterday"
        now.get(Calendar.YEAR) == then.get(Calendar.YEAR) -> SimpleDateFormat("MMM d", Locale.getDefault()).format(date)
        else -> SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(date)
    }
}

fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
    // Simplified check
    return cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) &&
            cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

fun isYesterday(today: Calendar, otherDay: Calendar): Boolean {
    val yesterday = Calendar.getInstance().apply {
        timeInMillis = today.timeInMillis // Start from today
        add(Calendar.DAY_OF_YEAR, -1)
    }
    return isSameDay(yesterday, otherDay)
}

// --- Preview ---

@Preview(showBackground = true, widthDp = 360, heightDp = 740)
@Composable
fun ToDoAppPreview() {
    MethodicTheme {
        ToDoApp()
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 740)
@Composable
fun ToDoAppPreviewDark() {
    MethodicTheme(darkTheme = true) {
        ToDoApp()
    }
}

@Preview(showBackground = true)
@Composable
fun ToDoListItemPreview() {
    MethodicTheme {
        Column {
            ToDoListItem(
                item = ToDoItem(1, "Active Task with Description", "This is the description.", priority = TaskPriority.HIGH, dueDate = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 1) }.time),
                onToggleComplete = {},
                onDeleteItem = {}
            )
            ToDoListItem(
                item = ToDoItem(2, "Completed Task", isCompleted = true, priority = TaskPriority.LOW),
                onToggleComplete = {},
                onDeleteItem = {}
            )
            ToDoListItem(
                item = ToDoItem(3, "Past Due Task", priority = TaskPriority.MEDIUM, dueDate = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -2) }.time),
                onToggleComplete = {},
                onDeleteItem = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PriorityChipPreview() {
    MethodicTheme {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PriorityChip(priority = TaskPriority.LOW, selected = false, onClick = {})
            PriorityChip(priority = TaskPriority.MEDIUM, selected = true, onClick = {})
            PriorityChip(priority = TaskPriority.HIGH, selected = false, onClick = {})
        }
    }
}