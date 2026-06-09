package com.example.locktodo

import android.content.Intent
import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import android.widget.EditText
import android.widget.CheckBox
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class AddTaskActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_task)

        val etTask = findViewById<EditText>(R.id.et_task)
        val cbUrgent = findViewById<CheckBox>(R.id.cb_urgent)
        val btnSave = findViewById<TextView>(R.id.btn_save)
        val btnCancel = findViewById<TextView>(R.id.btn_cancel)

        btnSave.setOnClickListener {
            val title = etTask.text.toString().trim()
            if (title.isNotEmpty()) {
                val task = Task(title = title, isFinish = false, isUrgent = cbUrgent.isChecked)
                GlobalScope.launch(Dispatchers.IO) {
                    val db = AppDatabase.getInstance(this@AddTaskActivity)
                    db.taskDao().insertTask(task)
                    
                    runOnUiThread {
                        refreshWidget()
                    }
                    finish()
                }
            }
        }

        btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun refreshWidget() {
        val appWidgetManager = AppWidgetManager.getInstance(this)
        val componentName = ComponentName(this, LockTodoWidget::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
        
        if (appWidgetIds.isNotEmpty()) {
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.lv_tasks)
            
            // 强制更新小组件
            val intent = Intent(this, LockTodoWidget::class.java)
            intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
            sendBroadcast(intent)
        }
    }
}