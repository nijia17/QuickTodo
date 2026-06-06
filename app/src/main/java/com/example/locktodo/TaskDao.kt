package com.example.locktodo

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    //新增任务
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)
    //修改任务
    @Update
    suspend fun updateTask(task: Task)
    //删除任务
    @Delete
    suspend fun deleteTask(task: Task)
    //查询所有未完成任务（小组件用）- Flow版本
    @Query("SELECT * FROM task_table WHERE isFinish = 0 ORDER BY isUrgent DESC")
    fun getUnFinishTask(): Flow<List<Task>>
    //查询所有未完成任务（小组件用）- List版本
    @Query("SELECT * FROM task_table WHERE isFinish = 0 ORDER BY isUrgent DESC")
    suspend fun getUnFinishTaskList(): List<Task>
    //查询全部任务（未完成置顶，紧急任务优先，已完成置底）
    @Query("SELECT * FROM task_table ORDER BY isFinish ASC, isUrgent DESC")
    fun getAllTask(): Flow<List<Task>>
    //查询全部任务（小组件用）- List版本（未完成置顶，紧急任务优先，已完成置底）
    @Query("SELECT * FROM task_table ORDER BY isFinish ASC, isUrgent DESC")
    suspend fun getAllTaskList(): List<Task>
    //根据ID查询任务
    @Query("SELECT * FROM task_table WHERE id = :taskId")
    suspend fun getTaskById(taskId: Int): Task?
}