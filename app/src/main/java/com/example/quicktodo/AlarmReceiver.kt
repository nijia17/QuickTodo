package com.example.quicktodo

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.runBlocking

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getIntExtra(EXTRA_TASK_ID, -1)
        val notificationTitle = intent.getStringExtra(EXTRA_NOTIFICATION_TITLE) ?: return
        val notificationContent = intent.getStringExtra(EXTRA_NOTIFICATION_CONTENT) ?: return

        if (taskId == -1) return

        val db = AppDatabase.getInstance(context)
        val task = runBlocking { db.taskDao().getTaskById(taskId) } ?: return

        if (task.isFinish) return

        NotificationHelper.showNotification(
            context,
            taskId,
            notificationTitle,
            notificationContent
        )
    }

    companion object {
        const val EXTRA_TASK_ID = "extra_task_id"
        const val EXTRA_TASK_TITLE = "extra_task_title"
        const val EXTRA_NOTIFICATION_TITLE = "extra_notification_title"
        const val EXTRA_NOTIFICATION_CONTENT = "extra_notification_content"
    }
}
