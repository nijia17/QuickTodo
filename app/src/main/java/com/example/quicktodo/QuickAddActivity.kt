@file:OptIn(ExperimentalLayoutApi::class)

package com.example.quicktodo

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar

class QuickAddActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setLayout(
            (resources.displayMetrics.widthPixels * 0.88).toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        setContent {
            QuickAddScreen(
                onAdd = { title, deadline ->
                    addTask(title, deadline)
                },
                onCancel = { finish() }
            )
        }
    }

    private fun addTask(title: String, deadline: Long) {
        lifecycleScope.launch {
            val aiAnalyzer = AIPriorityAnalyzer()
            val priority = aiAnalyzer.analyzePriority(title, deadline)
            val category = aiAnalyzer.analyzeCategory(title)
            val db = AppDatabase.getInstance(this@QuickAddActivity)

            val task = Task(
                title = title,
                deadline = deadline,
                category = category.name,
                priority = priority.name
            )
            val id = db.taskDao().insertTask(task).toInt()
            AlarmScheduler.schedule(this@QuickAddActivity, task.copy(id = id))
            refreshWidget()
            finish()
        }
    }

    private fun refreshWidget() {
        val appWidgetManager = AppWidgetManager.getInstance(this)
        val componentName = ComponentName(this, QuickTodoWidget::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
        if (appWidgetIds.isNotEmpty()) {
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.lv_tasks)
            val intent = Intent(this, QuickTodoWidget::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
            }
            sendBroadcast(intent)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAddScreen(
    onAdd: (String, Long) -> Unit,
    onCancel: () -> Unit
) {
    var text by remember { mutableStateOf("") }
    var selectedDeadline by remember { mutableStateOf(0L) }
    var selectedDeadlineLabel by remember { mutableStateOf<String?>(null) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var pendingCustomDate by remember { mutableStateOf<Calendar?>(null) }
    val aiAnalyzer = remember { AIPriorityAnalyzer() }
    val context = androidx.compose.ui.platform.LocalContext.current

    fun setDeadline(label: String, deadline: Long) {
        selectedDeadlineLabel = label
        selectedDeadline = deadline
    }

    fun clearDeadline() {
        selectedDeadlineLabel = null
        selectedDeadline = 0L
    }

    fun showCustomTimePicker() {
        val now = Calendar.getInstance()
        android.app.DatePickerDialog(
            context,
            R.style.TimePickerDialogTheme,
            { _, year, month, day ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, month, day, 0, 0, 0)
                pendingCustomDate = selectedDate
                showTimePicker = true
            },
            now.get(Calendar.YEAR),
            now.get(Calendar.MONTH),
            now.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    if (showTimePicker && pendingCustomDate != null) {
        SimpleTimePickerDialog(
            initialHour = pendingCustomDate!!.get(Calendar.HOUR_OF_DAY),
            initialMinute = pendingCustomDate!!.get(Calendar.MINUTE),
            onConfirm = { hour, minute ->
                val calendar = pendingCustomDate!!.clone() as Calendar
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                if (calendar.timeInMillis < System.currentTimeMillis()) {
                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                }
                setDeadline(context.getString(R.string.deadline_custom), calendar.timeInMillis)
                showTimePicker = false
                pendingCustomDate = null
            },
            onDismiss = {
                showTimePicker = false
                pendingCustomDate = null
            }
        )
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = stringResource(R.string.title_quick_add),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Light,
                    color = Color(0xFF2D2D2D)
                )

                Spacer(modifier = Modifier.height(12.dp))

                TextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { Text(stringResource(R.string.hint_task_input), color = Color(0xFFCCCCCC)) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(color = Color(0xFF2D2D2D), fontSize = 16.sp),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color(0xFF2D2D2D),
                        unfocusedIndicatorColor = Color(0xFFE8E8E8),
                        cursorColor = Color(0xFF2D2D2D),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                AnimatedVisibility(visible = text.isBlank()) {
                    Text(
                        text = stringResource(R.string.text_empty_input_hint),
                        fontSize = 12.sp,
                        color = Color(0xFFCCCCCC),
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                SectionLabel(stringResource(R.string.label_deadline))

                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DeadlineChip(
                        label = stringResource(R.string.deadline_ten_min),
                        selected = selectedDeadlineLabel == context.getString(R.string.deadline_ten_min),
                        onClick = {
                            val calendar = Calendar.getInstance()
                            calendar.add(Calendar.MINUTE, 10)
                            setDeadline(context.getString(R.string.deadline_ten_min), calendar.timeInMillis)
                        }
                    )
                    DeadlineChip(
                        label = stringResource(R.string.deadline_half_hour),
                        selected = selectedDeadlineLabel == context.getString(R.string.deadline_half_hour),
                        onClick = {
                            val calendar = Calendar.getInstance()
                            calendar.add(Calendar.MINUTE, 30)
                            setDeadline(context.getString(R.string.deadline_half_hour), calendar.timeInMillis)
                        }
                    )
                    DeadlineChip(
                        label = stringResource(R.string.deadline_one_hour),
                        selected = selectedDeadlineLabel == context.getString(R.string.deadline_one_hour),
                        onClick = {
                            val calendar = Calendar.getInstance()
                            calendar.add(Calendar.HOUR_OF_DAY, 1)
                            setDeadline(context.getString(R.string.deadline_one_hour), calendar.timeInMillis)
                        }
                    )
                    DeadlineChip(
                        label = stringResource(R.string.deadline_tonight),
                        selected = selectedDeadlineLabel == context.getString(R.string.deadline_tonight),
                        onClick = {
                            val calendar = Calendar.getInstance()
                            calendar.set(Calendar.HOUR_OF_DAY, 23)
                            calendar.set(Calendar.MINUTE, 59)
                            calendar.set(Calendar.SECOND, 59)
                            setDeadline(context.getString(R.string.deadline_tonight), calendar.timeInMillis)
                        }
                    )
                    DeadlineChip(
                        label = stringResource(R.string.deadline_custom),
                        selected = selectedDeadlineLabel == context.getString(R.string.deadline_custom),
                        onClick = { showCustomTimePicker() }
                    )
                }

                AnimatedVisibility(visible = selectedDeadlineLabel != null) {
                    TextButton(onClick = { clearDeadline() }) {
                        Text(
                            stringResource(R.string.text_clear_deadline),
                            fontSize = 12.sp,
                            color = Color(0xFF999999)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (text.isNotBlank() && !isAnalyzing) {
                            isAnalyzing = true
                            onAdd(text.trim(), selectedDeadline)
                        }
                    },
                    enabled = text.isNotBlank() && !isAnalyzing,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2D2D2D),
                        contentColor = Color.White,
                        disabledContainerColor = Color(0xFFE0E0E0),
                        disabledContentColor = Color(0xFF999999)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isAnalyzing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        stringResource(R.string.btn_add),
                        fontSize = 16.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = onCancel,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        stringResource(R.string.btn_cancel),
                        fontSize = 14.sp,
                        color = Color(0xFF999999)
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        fontSize = 12.sp,
        color = Color(0xFF999999)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeadlineChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, fontSize = 12.sp) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = Color(0xFF2D2D2D),
            selectedLabelColor = Color.White,
            containerColor = Color(0xFFFAFAFA),
            labelColor = Color(0xFF2D2D2D)
        )
    )
}
