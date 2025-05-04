package com.vio_0x.methodic.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vio_0x.methodic.data.TaskPriority
import com.vio_0x.methodic.ui.components.PriorityChip
import com.vio_0x.methodic.viewmodel.TaskListUiState

@Composable
fun AddTaskSheetContent(
    uiState: TaskListUiState, // Receive relevant part of state
    onUpdateTaskText: (String) -> Unit,
    onUpdateTaskDescription: (String) -> Unit,
    onUpdateTaskPriority: (TaskPriority) -> Unit,
    onAddTask: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
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

        OutlinedTextField(
            value = uiState.newTaskText,
            onValueChange = onUpdateTaskText,
            label = { Text("Task Title") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = { Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = null) },
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = uiState.newTaskDescription,
            onValueChange = onUpdateTaskDescription,
            label = { Text("Description (Optional)") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.AutoMirrored.Filled.Notes, contentDescription = null) },
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

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
                    onClick = { onUpdateTaskPriority(priority) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onAddTask,
            enabled = uiState.newTaskText.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Add Task")
        }

        Spacer(modifier = Modifier.height(8.dp)) // Bottom padding
    }
}