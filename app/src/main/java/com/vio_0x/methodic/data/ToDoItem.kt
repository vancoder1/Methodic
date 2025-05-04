package com.vio_0x.methodic.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.vio_0x.methodic.data.converters.DateConverter
import com.vio_0x.methodic.data.converters.StringListConverter
import java.util.Date

@Entity(tableName = "todo_items")
@TypeConverters(DateConverter::class, StringListConverter::class)
data class ToDoItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0, // Default value needed for autoGenerate
    val text: String,
    val description: String? = null,
    var isCompleted: Boolean = false,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val createdAt: Date = Date(),
    val dueDate: Date? = null,
    val completedAt: Date? = null,
    val tags: List<String> = emptyList()
)