package com.example.quicktodo

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TaskViewModel(private val repo: TaskRepo, private val context: Context) : ViewModel() {
    val unFinishTask = repo.unFinishList
    val allTask = repo.allTaskList

    private val aiAnalyzer = AIPriorityAnalyzer()

    private val _completedCount = MutableStateFlow(0)
    val completedCount: StateFlow<Int> = _completedCount

    private val _totalCount = MutableStateFlow(0)
    val totalCount: StateFlow<Int> = _totalCount

    fun loadStats() = viewModelScope.launch {
        _completedCount.value = repo.getCompletedCount()
        _totalCount.value = repo.getTotalCount()
    }

    fun addTaskWithAI(title: String, deadline: Long = 0) = viewModelScope.launch {
        val priority = aiAnalyzer.analyzePriority(title, deadline)
        val category = aiAnalyzer.analyzeCategory(title)

        val task = Task(
            title = title,
            deadline = deadline,
            category = category.name,
            priority = priority.name
        )
        repo.insert(task)
        notifyWidget()
        loadStats()
    }

    fun addTask(task: Task) = viewModelScope.launch {
        repo.insert(task)
        notifyWidget()
        loadStats()
    }

    fun editTask(task: Task) = viewModelScope.launch {
        repo.update(task)
        notifyWidget()
        loadStats()
    }

    fun removeTask(task: Task) = viewModelScope.launch {
        repo.delete(task)
        notifyWidget()
        loadStats()
    }

    fun toggleTask(task: Task) = viewModelScope.launch {
        val updatedTask = task.copy(isFinish = !task.isFinish)
        repo.update(updatedTask)
        notifyWidget()
        loadStats()
    }

    fun analyzePriority(task: Task): Priority {
        return aiAnalyzer.analyzePriority(task.title, task.deadline)
    }

    fun getPrioritySuggestion(task: Task): String {
        return aiAnalyzer.getPrioritySuggestion(task)
    }

    private fun notifyWidget() {
        val intent = Intent("com.example.quicktodo.WIDGET_UPDATE").apply {
            `package` = context.packageName
        }
        context.sendBroadcast(intent)
    }
}

class TaskVMFactory(private val repo: TaskRepo, private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            return TaskViewModel(repo, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}