package com.example.locktodo

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class TaskViewModel(private val repo: TaskRepo, private val context: Context):ViewModel(){
    val unFinishTask = repo.unFinishList
    val allTask = repo.allTaskList

    fun addTask(task: Task)=viewModelScope.launch {
        repo.insert(task)
    }
    fun editTask(task: Task)=viewModelScope.launch {
        repo.update(task)
    }
    fun removeTask(task: Task)=viewModelScope.launch {
        repo.delete(task)
        notifyWidget()
    }

    fun toggleTask(task: Task)=viewModelScope.launch {
        val updatedTask = task.copy(isFinish = !task.isFinish)
        repo.update(updatedTask)
        notifyWidget()
    }

    private fun notifyWidget() {
        val intent = Intent("com.example.locktodo.WIDGET_UPDATE").apply {
            `package` = context.packageName
        }
        context.sendBroadcast(intent)
    }
}

//ViewModel工厂
class TaskVMFactory(private val repo: TaskRepo, private val context: Context):ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(TaskViewModel::class.java)){
            return TaskViewModel(repo, context) as T
        }
        throw IllegalArgumentException("未知ViewModel")
    }
}