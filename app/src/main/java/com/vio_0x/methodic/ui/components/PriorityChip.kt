package com.vio_0x.methodic.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vio_0x.methodic.data.TaskPriority
import com.vio_0x.methodic.ui.theme.MethodicTheme

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

    val targetBackgroundColor =
        if (selected) color.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceColorAtElevation(
            1.dp
        )
    val animatedBackgroundColor by animateColorAsState(
        targetBackgroundColor,
        label = "Chip Background ${priority.name}"
    )

    val targetBorderColor =
        if (selected) color else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    val animatedBorderColor by animateColorAsState(
        targetBorderColor,
        label = "Chip Border ${priority.name}"
    )

    val targetTextColor = if (selected) color else MaterialTheme.colorScheme.onSurfaceVariant
    val animatedTextColor by animateColorAsState(
        targetTextColor,
        label = "Chip Text ${priority.name}"
    )

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