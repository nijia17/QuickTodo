package com.example.locktodo

import kotlinx.coroutines.flow.Flow

class TaskRepo(private val dao: TaskDao) {
    val unFinishList: Flow<List<Task>> = dao.getUnFinishTask()
    val allTaskList: Flow<List<Task>> = dao.getAllTask()

    suspend fun insert(task: Task) = dao.insertTask(task)
    suspend fun update(task: Task) = dao.updateTask(task)
    suspend fun delete(task: Task) = dao.deleteTask(task)
}