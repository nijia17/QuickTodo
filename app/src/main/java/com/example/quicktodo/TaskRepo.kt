package com.example.quicktodo

import kotlinx.coroutines.flow.Flow

class TaskRepo(private val dao: TaskDao) {
    val unFinishList: Flow<List<Task>> = dao.getUnFinishTask()
    val allTaskList: Flow<List<Task>> = dao.getAllTask()

    suspend fun insert(task: Task): Long = dao.insertTask(task)
    suspend fun update(task: Task) = dao.updateTask(task)
    suspend fun delete(task: Task) = dao.deleteTask(task)

    suspend fun getCompletedCount(): Int = dao.getCompletedCount()
    suspend fun getTotalCount(): Int = dao.getTotalCount()
    suspend fun getUnfinishedCount(): Int = dao.getUnfinishedCount()
    suspend fun getCompletedTodayCount(start: Long, end: Long): Int = dao.getCompletedTodayCount(start, end)
}