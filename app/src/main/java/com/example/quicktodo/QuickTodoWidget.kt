package com.example.quicktodo

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.RemoteViews
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class QuickTodoWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val aiAnalyzer = AIPriorityAnalyzer()
        val suggestion = try {
            val db = AppDatabase.getInstance(context)
            val tasks = runBlocking { db.taskDao().getAllTaskList() }
            aiAnalyzer.getWidgetSuggestion(tasks)
        } catch (e: Exception) {
            Log.e("QuickTodoWidget", "Error loading AI suggestion", e)
            ""
        }

        appWidgetIds.forEach { appWidgetId ->
            try {
                val views = RemoteViews(context.packageName, R.layout.widget_layout)

                views.setTextViewText(R.id.tv_ai_suggestion, suggestion)

                val intent = Intent(context, TaskWidgetService::class.java)
                views.setRemoteAdapter(R.id.lv_tasks, intent)

                views.setEmptyView(R.id.lv_tasks, android.R.id.empty)

                val clickIntent = Intent(context, QuickTodoWidget::class.java).apply {
                    action = "com.example.quicktodo.TOGGLE_TASK"
                }

                val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                } else {
                    PendingIntent.FLAG_UPDATE_CURRENT
                }

                val clickPendingIntent = PendingIntent.getBroadcast(context, 0, clickIntent, flags)
                views.setPendingIntentTemplate(R.id.lv_tasks, clickPendingIntent)

                val appIntent = Intent(context, MainActivity::class.java)
                appIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                val appPendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    appIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.tv_title, appPendingIntent)

                val addIntent = Intent(context, QuickAddActivity::class.java)
                addIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                val addPendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    addIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                )
                views.setOnClickPendingIntent(R.id.btn_add, addPendingIntent)

                appWidgetManager.updateAppWidget(appWidgetId, views)
            } catch (e: Exception) {
                Log.e("QuickTodoWidget", "Error updating widget $appWidgetId", e)
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        try {
            super.onReceive(context, intent)

            if (intent.action == "com.example.quicktodo.TOGGLE_TASK") {
                val taskId = intent.getIntExtra("task_id", -1)
                if (taskId != -1) {
                    toggleTask(context, taskId)
                }
            } else if (intent.action == "com.example.quicktodo.WIDGET_UPDATE") {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val componentName = ComponentName(context, QuickTodoWidget::class.java)
                appWidgetManager.notifyAppWidgetViewDataChanged(
                    appWidgetManager.getAppWidgetIds(componentName),
                    R.id.lv_tasks
                )
            }
        } catch (e: Exception) {
            Log.e("QuickTodoWidget", "Error receiving widget intent", e)
        }
    }

    private fun toggleTask(context: Context, taskId: Int) {
        val db = AppDatabase.getInstance(context)
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val task = db.taskDao().getTaskById(taskId)
                if (task != null) {
                    val updatedTask = task.copy(isFinish = !task.isFinish)
                    db.taskDao().updateTask(updatedTask)

                    if (updatedTask.isFinish) {
                        RecurringTaskHelper.generateNextTask(updatedTask)?.let { nextTask ->
                            val nextId = db.taskDao().insertTask(nextTask).toInt()
                            AlarmScheduler.schedule(context, nextTask.copy(id = nextId))
                        }
                    }

                    val appWidgetManager = AppWidgetManager.getInstance(context)
                    val componentName = ComponentName(context, QuickTodoWidget::class.java)
                    appWidgetManager.notifyAppWidgetViewDataChanged(
                        appWidgetManager.getAppWidgetIds(componentName),
                        R.id.lv_tasks
                    )
                }
            } catch (e: Exception) {
                Log.e("QuickTodoWidget", "Error toggling task $taskId", e)
            }
        }
    }
}
