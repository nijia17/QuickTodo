package com.example.quicktodo

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

class TaskWidgetFactory(private val context: Context) : RemoteViewsService.RemoteViewsFactory {
    private var taskList: List<Task> = emptyList()
    private lateinit var db: AppDatabase

    override fun onCreate() {
        db = AppDatabase.getInstance(context)
    }

    override fun onDataSetChanged() {
        try {
            taskList = runBlocking {
                db.taskDao().getAllTaskList()
            }.filter { !it.isFinish }
        } catch (e: Exception) {
            Log.e("TaskWidgetFactory", "Error loading tasks", e)
            taskList = emptyList()
        }
    }

    override fun onDestroy() {
        // Clean up resources
    }

    override fun getCount(): Int = taskList.size.coerceAtMost(6)

    override fun getViewAt(position: Int): RemoteViews {
        return try {
            val task = taskList.getOrNull(position) ?: return RemoteViews(context.packageName, R.layout.widget_task_item)

            val rv = RemoteViews(context.packageName, R.layout.widget_task_item)

            if (task.isFinish) {
                rv.setTextViewText(R.id.tv_checkbox, "\u2713")
                rv.setTextColor(R.id.tv_task_title, context.getColor(android.R.color.darker_gray))
                rv.setInt(R.id.tv_task_title, "setPaintFlags", android.graphics.Paint.STRIKE_THRU_TEXT_FLAG)
            } else {
                rv.setTextViewText(R.id.tv_checkbox, "\u25CB")
                rv.setTextColor(R.id.tv_task_title, context.getColor(android.R.color.black))
                rv.setInt(R.id.tv_task_title, "setPaintFlags", 0)
            }

            rv.setTextViewText(R.id.tv_task_title, task.title)

            when (task.priority) {
                Priority.HIGH.name -> {
                    rv.setTextViewText(R.id.tv_priority, "\uD83D\uDEA8")
                    rv.setTextColor(R.id.tv_priority, context.getColor(android.R.color.holo_red_dark))
                    rv.setViewVisibility(R.id.tv_priority, android.view.View.VISIBLE)
                }
                Priority.MEDIUM.name -> {
                    rv.setTextViewText(R.id.tv_priority, "\u26A0\uFE0F")
                    rv.setTextColor(R.id.tv_priority, context.getColor(android.R.color.holo_orange_dark))
                    rv.setViewVisibility(R.id.tv_priority, android.view.View.VISIBLE)
                }
                else -> {
                    rv.setTextViewText(R.id.tv_priority, "")
                    rv.setViewVisibility(R.id.tv_priority, android.view.View.GONE)
                }
            }

            val fillInIntent = Intent().apply {
                putExtra("task_id", task.id)
            }
            rv.setOnClickFillInIntent(R.id.item_container, fillInIntent)

            rv
        } catch (e: Exception) {
            Log.e("TaskWidgetFactory", "Error rendering widget item at $position", e)
            RemoteViews(context.packageName, R.layout.widget_task_item)
        }
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long {
        val task = taskList.getOrNull(position)
        return task?.id?.toLong() ?: position.toLong()
    }

    override fun hasStableIds(): Boolean = true
}
