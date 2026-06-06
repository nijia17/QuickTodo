package com.example.locktodo

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "task_table")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,          //待办标题
    val isUrgent: Boolean = false, //是否紧急
    val isFinish: Boolean = false   //是否完成
)