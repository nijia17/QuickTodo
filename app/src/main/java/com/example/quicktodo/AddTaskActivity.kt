package com.example.quicktodo

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.util.Calendar

class AddTaskActivity : ComponentActivity() {

    private val aiAnalyzeLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            val title = data?.getStringExtra(AIAnalyzeActivity.EXTRA_RESULT_TITLE) ?: ""
            val deadline = data?.getLongExtra(AIAnalyzeActivity.EXTRA_RESULT_DEADLINE, 0L) ?: 0L
            val category = data?.getStringExtra(AIAnalyzeActivity.EXTRA_RESULT_CATEGORY) ?: Category.OTHER.name
            val priority = data?.getStringExtra(AIAnalyzeActivity.EXTRA_RESULT_PRIORITY) ?: Priority.LOW.name
            val recurType = data?.getStringExtra(AIAnalyzeActivity.EXTRA_RESULT_RECUR_TYPE) ?: RecurType.NONE.name
            saveTask(title, deadline, category, priority, recurType)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AddTaskScreen(
                onCancel = { finish() },
                onSave = { title, deadline, category, recurType ->
                    val intent = Intent(this, AIAnalyzeActivity::class.java).apply {
                        putExtra(AIAnalyzeActivity.EXTRA_TITLE, title)
                        putExtra(AIAnalyzeActivity.EXTRA_DEADLINE, deadline)
                        putExtra(AIAnalyzeActivity.EXTRA_CATEGORY, category)
                        putExtra(AIAnalyzeActivity.EXTRA_RECUR_TYPE, recurType)
                    }
                    aiAnalyzeLauncher.launch(intent)
                }
            )
        }
    }

    private fun saveTask(title: String, deadline: Long, category: String, priority: String, recurType: String) {
        lifecycleScope.launch {
            val db = AppDatabase.getInstance(this@AddTaskActivity)
            val task = Task(
                title = title,
                deadline = deadline,
                category = category,
                priority = priority,
                isRecurring = recurType != RecurType.NONE.name,
                recurType = recurType
            )
            val id = db.taskDao().insertTask(task).toInt()
            AlarmScheduler.schedule(this@AddTaskActivity, task.copy(id = id))
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
fun AddTaskScreen(
    onCancel: () -> Unit,
    onSave: (String, Long, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedDeadline by remember { mutableStateOf(0L) }
    var selectedCategory by remember { mutableStateOf(Category.OTHER.name) }
    var selectedRecurType by remember { mutableStateOf(RecurType.NONE.name) }
    var showDiscardDialog by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var pendingDateCalendar by remember { mutableStateOf<Calendar?>(null) }

    val hasUnsavedChanges = title.isNotBlank() || selectedDeadline > 0 || selectedCategory != Category.OTHER.name || selectedRecurType != RecurType.NONE.name

    BackHandler(enabled = hasUnsavedChanges) {
        showDiscardDialog = true
    }

    val handleCancel: () -> Unit = {
        if (hasUnsavedChanges) {
            showDiscardDialog = true
        } else {
            onCancel()
        }
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text(stringResource(R.string.text_discard_confirm_title), fontWeight = FontWeight.Light) },
            text = { Text(stringResource(R.string.text_discard_confirm_message), fontSize = 14.sp, color = Color(0xFF666666)) },
            confirmButton = {
                TextButton(onClick = onCancel) {
                    Text(stringResource(R.string.btn_confirm), color = Color(0xFFE53935))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text(stringResource(R.string.btn_cancel), color = Color(0xFF999999))
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.title_add_task),
                        fontWeight = FontWeight.Light,
                        fontSize = 22.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color(0xFF2D2D2D)
                ),
                navigationIcon = {
                    TextButton(onClick = handleCancel) {
                        Text(
                            stringResource(R.string.btn_cancel),
                            fontSize = 14.sp,
                            color = Color(0xFF999999)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color.White)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.label_task),
                fontSize = 12.sp,
                color = Color(0xFF999999)
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = title,
                onValueChange = { title = it },
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

            AnimatedVisibility(visible = title.isBlank()) {
                Text(
                    text = stringResource(R.string.text_empty_input_hint),
                    fontSize = 12.sp,
                    color = Color(0xFFCCCCCC),
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.label_deadline),
                fontSize = 12.sp,
                color = Color(0xFF999999)
            )

            Spacer(modifier = Modifier.height(8.dp))

            val context = LocalContext.current
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val calendar = Calendar.getInstance()
                        android.app.DatePickerDialog(
                            context,
                            R.style.TimePickerDialogTheme,
                            { _, year, month, day ->
                                val selectedDate = Calendar.getInstance()
                                val now = Calendar.getInstance()
                                selectedDate.set(year, month, day,
                                    now.get(Calendar.HOUR_OF_DAY),
                                    now.get(Calendar.MINUTE),
                                    now.get(Calendar.SECOND))
                                pendingDateCalendar = selectedDate
                                showTimePicker = true
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    }
            ) {
                Text(
                    text = if (selectedDeadline > 0) {
                        val date = Calendar.getInstance()
                        date.timeInMillis = selectedDeadline
                        val month = date.get(Calendar.MONTH) + 1
                        val day = date.get(Calendar.DAY_OF_MONTH)
                        val hour = String.format("%02d", date.get(Calendar.HOUR_OF_DAY))
                        val minute = String.format("%02d", date.get(Calendar.MINUTE))
                        "${date.get(Calendar.YEAR)}-${month}-${day} ${hour}:${minute}"
                    } else {
                        stringResource(R.string.text_select_deadline_hint)
                    },
                    fontSize = 16.sp,
                    color = if (selectedDeadline > 0) Color(0xFF2D2D2D) else Color(0xFFCCCCCC),
                    modifier = Modifier.weight(1f)
                )
            }

            if (showTimePicker && pendingDateCalendar != null) {
                SimpleTimePickerDialog(
                    initialHour = pendingDateCalendar!!.get(Calendar.HOUR_OF_DAY),
                    initialMinute = pendingDateCalendar!!.get(Calendar.MINUTE),
                    onConfirm = { hour, minute ->
                        pendingDateCalendar!!.set(Calendar.HOUR_OF_DAY, hour)
                        pendingDateCalendar!!.set(Calendar.MINUTE, minute)
                        pendingDateCalendar!!.set(Calendar.SECOND, 0)
                        selectedDeadline = pendingDateCalendar!!.timeInMillis
                        showTimePicker = false
                        pendingDateCalendar = null
                    },
                    onDismiss = {
                        showTimePicker = false
                        pendingDateCalendar = null
                    }
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(top = 8.dp),
                color = Color(0xFFE8E8E8)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.label_category),
                fontSize = 12.sp,
                color = Color(0xFF999999)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Category.values().forEach { cat ->
                    val label = when (cat) {
                        Category.STUDY -> stringResource(R.string.label_study)
                        Category.WORK -> stringResource(R.string.label_work)
                        Category.LIFE -> stringResource(R.string.label_life)
                        Category.OTHER -> stringResource(R.string.label_other)
                    }
                    FilterChip(
                        selected = selectedCategory == cat.name,
                        onClick = { selectedCategory = cat.name },
                        label = { Text(label, fontSize = 14.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF2D2D2D),
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFFFAFAFA),
                            labelColor = Color(0xFF2D2D2D)
                        ),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.label_recur),
                fontSize = 12.sp,
                color = Color(0xFF999999)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                RecurType.values().forEach { recur ->
                    val label = when (recur) {
                        RecurType.NONE -> stringResource(R.string.label_recur_none)
                        RecurType.DAILY -> stringResource(R.string.label_recur_daily)
                        RecurType.WEEKDAY -> stringResource(R.string.label_recur_weekday)
                        RecurType.WEEKLY -> stringResource(R.string.label_recur_weekly)
                    }
                    FilterChip(
                        selected = selectedRecurType == recur.name,
                        onClick = { selectedRecurType = recur.name },
                        label = { Text(label, fontSize = 14.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF2D2D2D),
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFFFAFAFA),
                            labelColor = Color(0xFF2D2D2D)
                        ),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            HorizontalDivider(color = Color(0xFFE8E8E8))

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.text_ai_smart_sort),
                    fontSize = 16.sp,
                    color = Color(0xFF2D2D2D)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.text_ai_auto_analyze),
                    fontSize = 12.sp,
                    color = Color(0xFF999999),
                    lineHeight = 18.sp
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(title.trim(), selectedDeadline, selectedCategory, selectedRecurType)
                    }
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2D2D2D),
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFFE0E0E0),
                    disabledContentColor = Color(0xFF999999)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    stringResource(R.string.btn_save),
                    fontSize = 16.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
