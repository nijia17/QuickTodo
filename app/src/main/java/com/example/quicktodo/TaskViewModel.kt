package com.example.quicktodo

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

class TaskViewModel(private val repo: TaskRepo, private val context: Context) : ViewModel() {
    val unFinishTask = repo.unFinishList
    val allTask = repo.allTaskList

    private val aiAnalyzer = AIPriorityAnalyzer()
    private val prefs: SharedPreferences = context.getSharedPreferences("quicktodo_stats", Context.MODE_PRIVATE)

    private val _completedCount = MutableStateFlow(0)
    val completedCount: StateFlow<Int> = _completedCount

    private val _totalCount = MutableStateFlow(0)
    val totalCount: StateFlow<Int> = _totalCount

    private val _todayCompletedCount = MutableStateFlow(0)
    val todayCompletedCount: StateFlow<Int> = _todayCompletedCount

    private val _todayTotalCount = MutableStateFlow(0)
    val todayTotalCount: StateFlow<Int> = _todayTotalCount

    private val _currentStreak = MutableStateFlow(0)
    val currentStreak: StateFlow<Int> = _currentStreak

    fun loadStats() = viewModelScope.launch {
        _completedCount.value = repo.getCompletedCount()
        _totalCount.value = repo.getTotalCount()
        val (todayStart, todayEnd) = getTodayRange()
        _todayCompletedCount.value = repo.getCompletedTodayCount(todayStart, todayEnd)
        _todayTotalCount.value = repo.getUnfinishedCount()
        _currentStreak.value = calculateStreak()
    }

    private fun getTodayRange(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val start = calendar.timeInMillis
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val end = calendar.timeInMillis
        return start to end
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
        val now = System.currentTimeMillis()
        val updatedTask = if (!task.isFinish) {
            task.copy(isFinish = true, completedAt = now)
        } else {
            task.copy(isFinish = false, completedAt = 0)
        }
        repo.update(updatedTask)
        if (updatedTask.isFinish) {
            recordCompletionToday()
            RecurringTaskHelper.generateNextTask(updatedTask)?.let { nextTask ->
                val nextId = repo.insert(nextTask).toInt()
                AlarmScheduler.schedule(context, nextTask.copy(id = nextId))
            }
        }
        notifyWidget()
        loadStats()
    }

    fun analyzePriority(task: Task): Priority {
        return aiAnalyzer.analyzePriority(task.title, task.deadline)
    }

    fun getPrioritySuggestion(task: Task): String {
        return aiAnalyzer.getPrioritySuggestion(task)
    }

    private fun recordCompletionToday() {
        val today = Calendar.getInstance()
        val dateKey = "${today.get(Calendar.YEAR)}-${today.get(Calendar.MONTH) + 1}-${today.get(Calendar.DAY_OF_MONTH)}"
        prefs.edit().putBoolean("completed_$dateKey", true).apply()
    }

    private fun calculateStreak(): Int {
        val calendar = Calendar.getInstance()
        var streak = 0
        val maxDays = 365
        while (streak < maxDays) {
            val dateKey = "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH) + 1}-${calendar.get(Calendar.DAY_OF_MONTH)}"
            if (prefs.getBoolean("completed_$dateKey", false)) {
                streak++
                calendar.add(Calendar.DAY_OF_YEAR, -1)
            } else if (streak == 0 && isToday(calendar)) {
                calendar.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                break
            }
        }
        return streak
    }

    private fun isToday(calendar: Calendar): Boolean {
        val today = Calendar.getInstance()
        return calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
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
