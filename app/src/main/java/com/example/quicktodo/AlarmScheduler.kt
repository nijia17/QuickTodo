package com.example.quicktodo

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

object AlarmScheduler {

    fun schedule(context: Context, task: Task) {
        cancel(context, task.id)

        if (task.deadline <= 0 || task.isFinish) return

        val prefs = context.getSharedPreferences("quicktodo_settings", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("reminder_enabled", true)) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            if (!notificationManager.areNotificationsEnabled()) return
        }

        val now = System.currentTimeMillis()

        // 普通任务：截止前 15 分钟提醒一次
        val normalPreTime = task.deadline - 15 * 60 * 1000
        if (normalPreTime > now) {
            setAlarm(context, task.id, task.title, normalPreTime, "⏰ 任务即将到期", "${task.title} 将在 15 分钟后截止", alarmManager)
        }

        // 高优先级任务：额外在截止前 30 分钟再提醒一次
        if (task.priority == Priority.HIGH.name) {
            val highPreTime = task.deadline - 30 * 60 * 1000
            if (highPreTime > now) {
                setAlarm(context, task.id, task.title, highPreTime, "🚨 高优先级任务即将到期", "${task.title} 将在 30 分钟后截止，建议立即处理", alarmManager)
            }
        }
    }

    fun cancel(context: Context, taskId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)

        listOf(taskId, taskId * 100000).forEach { requestCode ->
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    private fun setAlarm(
        context: Context,
        taskId: Int,
        title: String,
        triggerTime: Long,
        notificationTitle: String,
        notificationContent: String,
        alarmManager: AlarmManager
    ) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(AlarmReceiver.EXTRA_TASK_ID, taskId)
            putExtra(AlarmReceiver.EXTRA_TASK_TITLE, title)
            putExtra(AlarmReceiver.EXTRA_NOTIFICATION_TITLE, notificationTitle)
            putExtra(AlarmReceiver.EXTRA_NOTIFICATION_CONTENT, notificationContent)
        }
        val requestCode = "$taskId-$notificationTitle".hashCode()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
}
