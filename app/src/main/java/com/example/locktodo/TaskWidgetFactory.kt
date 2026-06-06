package com.example.locktodo

import android.content.Context
import android.content.Intent
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
        taskList = runBlocking {
            db.taskDao().getAllTaskList()
        }
    }

    override fun onDestroy() {
        // 清理资源
    }

    override fun getCount(): Int = taskList.size.coerceAtMost(6)

    override fun getViewAt(position: Int): RemoteViews {
        val task = taskList.getOrNull(position) ?: return RemoteViews(context.packageName, R.layout.widget_task_item)

        val rv = RemoteViews(context.packageName, R.layout.widget_task_item)

        // 设置复选框显示
        if (task.isFinish) {
            rv.setTextViewText(R.id.tv_checkbox, "✓")
            rv.setTextColor(R.id.tv_task_title, context.getColor(android.R.color.darker_gray))
            rv.setInt(R.id.tv_task_title, "setPaintFlags", android.graphics.Paint.STRIKE_THRU_TEXT_FLAG)
        } else {
            rv.setTextViewText(R.id.tv_checkbox, "○")
            rv.setTextColor(R.id.tv_task_title, context.getColor(android.R.color.black))
            rv.setInt(R.id.tv_task_title, "setPaintFlags", 0)
        }

        // 设置任务标题
        rv.setTextViewText(R.id.tv_task_title, task.title)

        // 设置紧急标记
        if (task.isUrgent && !task.isFinish) {
            rv.setTextViewText(R.id.tv_urgent, "!")
        } else {
            rv.setTextViewText(R.id.tv_urgent, "")
        }

        // 创建 fillInIntent 用于传递 task_id，设置在整个列表项容器上
        val fillInIntent = Intent().apply {
            putExtra("task_id", task.id)
        }
        rv.setOnClickFillInIntent(R.id.item_container, fillInIntent)

        return rv
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long {
        val task = taskList.getOrNull(position)
        return task?.id?.toLong() ?: position.toLong()
    }

    override fun hasStableIds(): Boolean = true
}
