package com.example.quicktodo

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class Priority {
    HIGH, MEDIUM, LOW
}

enum class Category {
    STUDY, WORK, LIFE, OTHER
}

@Entity(tableName = "task_table")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val isUrgent: Boolean = false,
    val isFinish: Boolean = false,
    val deadline: Long = 0,
    val category: String = Category.OTHER.name,
    val priority: String = Priority.LOW.name
)