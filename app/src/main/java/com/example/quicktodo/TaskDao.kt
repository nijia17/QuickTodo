package com.example.quicktodo

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("SELECT * FROM task_table WHERE isFinish = 0 ORDER BY priority DESC, deadline ASC")
    fun getUnFinishTask(): Flow<List<Task>>

    @Query("SELECT * FROM task_table WHERE isFinish = 0 ORDER BY priority DESC, deadline ASC")
    suspend fun getUnFinishTaskList(): List<Task>

    @Query("SELECT * FROM task_table ORDER BY isFinish ASC, priority DESC, deadline ASC")
    fun getAllTask(): Flow<List<Task>>

    @Query("SELECT * FROM task_table ORDER BY isFinish ASC, priority DESC, deadline ASC")
    suspend fun getAllTaskList(): List<Task>

    @Query("SELECT * FROM task_table WHERE id = :taskId")
    suspend fun getTaskById(taskId: Int): Task?

    @Query("SELECT COUNT(*) FROM task_table WHERE isFinish = 1")
    suspend fun getCompletedCount(): Int

    @Query("SELECT COUNT(*) FROM task_table")
    suspend fun getTotalCount(): Int
}