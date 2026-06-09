package com.example.locktodo

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class LockTodoWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { appWidgetId ->
            val views = RemoteViews(context.packageName, R.layout.widget_layout)

            // 设置 ListView 的 RemoteViewsService
            val intent = Intent(context, TaskWidgetService::class.java)
            views.setRemoteAdapter(R.id.lv_tasks, intent)

            // 设置空视图
            views.setEmptyView(R.id.lv_tasks, android.R.id.empty)

            // 设置列表项点击的 PendingIntentTemplate
            val clickIntent = Intent(context, LockTodoWidget::class.java).apply {
                action = "com.example.locktodo.TOGGLE_TASK"
            }
            
            // Android 12+ 需要使用 FLAG_MUTABLE 才能使用 fillInIntent
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            
            val clickPendingIntent = PendingIntent.getBroadcast(context, 0, clickIntent, flags)
            views.setPendingIntentTemplate(R.id.lv_tasks, clickPendingIntent)

            // 添加点击打开应用的意图（点击标题区域）
            val appIntent = Intent(context, MainActivity::class.java)
            appIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            val appPendingIntent = PendingIntent.getActivity(
                context,
                0,
                appIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.tv_title, appPendingIntent)

            // 添加按钮点击事件 - 打开添加任务Activity
            val addIntent = Intent(context, AddTaskActivity::class.java)
            addIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            val addPendingIntent = PendingIntent.getActivity(
                context,
                0,
                addIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
            views.setOnClickPendingIntent(R.id.btn_add, addPendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        
        if (intent.action == "com.example.locktodo.TOGGLE_TASK") {
            val taskId = intent.getIntExtra("task_id", -1)
            if (taskId != -1) {
                toggleTask(context, taskId)
            }
        } else if (intent.action == "com.example.locktodo.WIDGET_UPDATE") {
            // 收到APP的更新通知，刷新小组件
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, LockTodoWidget::class.java)
            appWidgetManager.notifyAppWidgetViewDataChanged(
                appWidgetManager.getAppWidgetIds(componentName),
                R.id.lv_tasks
            )
        }
    }

    private fun toggleTask(context: Context, taskId: Int) {
        val db = AppDatabase.getInstance(context)
        GlobalScope.launch(Dispatchers.IO) {
            val task = db.taskDao().getTaskById(taskId)
            if (task != null) {
                val updatedTask = task.copy(isFinish = !task.isFinish)
                db.taskDao().updateTask(updatedTask)
                
                // 刷新小组件
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val componentName = ComponentName(context, LockTodoWidget::class.java)
                appWidgetManager.notifyAppWidgetViewDataChanged(
                    appWidgetManager.getAppWidgetIds(componentName),
                    R.id.lv_tasks
                )
            }
        }
    }
}
