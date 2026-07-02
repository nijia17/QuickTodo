@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.quicktodo

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.util.Calendar
import java.util.Locale

class MainActivity : ComponentActivity() {
    lateinit var vm: TaskViewModel

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        NotificationHelper.createNotificationChannel(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        val db = AppDatabase.getInstance(this)
        val repo = TaskRepo(db.taskDao())
        vm = ViewModelProvider(this, TaskVMFactory(repo, this))[TaskViewModel::class.java]

        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = Color(0xFF2D2D2D),
                    secondary = Color(0xFFF5F5F5),
                    background = Color(0xFFFAFAFA),
                    surface = Color.White,
                    onPrimary = Color.White,
                    onSecondary = Color(0xFF2D2D2D),
                    onBackground = Color(0xFF2D2D2D),
                    onSurface = Color(0xFF2D2D2D),
                    outline = Color(0xFFE8E8E8)
                )
            ) {
                TodoPage(vm)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::vm.isInitialized) {
            vm.loadStats()
        }
    }
}

@Composable
fun TodoPage(vm: TaskViewModel) {
    val taskList by vm.allTask.collectAsStateWithLifecycle(initialValue = emptyList())

    var showSettingsDialog by remember { mutableStateOf(false) }
    var showDetailDialog by remember { mutableStateOf(false) }
    var selectedTask by remember { mutableStateOf<Task?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var taskToDelete by remember { mutableStateOf<Task?>(null) }
    var showCompleted by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        vm.loadStats()
    }

    val requestDelete: (Task) -> Unit = { task ->
        taskToDelete = task
        showDeleteDialog = true
    }

    val confirmDelete: () -> Unit = {
        taskToDelete?.let { vm.removeTask(it) }
        showDeleteDialog = false
        taskToDelete = null
        showDetailDialog = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.title_today),
                        fontWeight = FontWeight.Light,
                        fontSize = 22.sp,
                        letterSpacing = 2.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color(0xFF2D2D2D),
                    actionIconContentColor = Color(0xFF2D2D2D)
                ),
                actions = {
                    IconButton(onClick = { context.startActivity(Intent(context, AddTaskActivity::class.java)) }) {
                        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.title_add_task))
                    }
                    IconButton(onClick = { context.startActivity(Intent(context, SettingsActivity::class.java)) }) {
                        Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.text_settings))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().padding(horizontal = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${taskList.count { !it.isFinish }} ${stringResource(R.string.text_today).lowercase()}",
                    fontSize = 14.sp,
                    color = Color(0xFF999999)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.text_ai_sorted),
                        fontSize = 12.sp,
                        color = Color(0xFF2D2D2D),
                        modifier = Modifier.padding(end = 12.dp)
                    )
                    Text(
                        text = "${taskList.count { it.isFinish }} ${stringResource(R.string.text_completed).lowercase()}",
                        fontSize = 14.sp,
                        color = Color(0xFF999999),
                        modifier = Modifier.clickable { context.startActivity(Intent(context, StatisticsActivity::class.java)) }
                    )
                }
            }

            val unfinishedTasks = taskList.filter { !it.isFinish }
            val completedTasks = taskList.filter { it.isFinish }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (taskList.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 80.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                stringResource(R.string.text_no_tasks),
                                fontSize = 16.sp,
                                color = Color(0xFFCCCCCC),
                                fontWeight = FontWeight.Light
                            )
                        }
                    }
                } else {
                    items(unfinishedTasks, key = { it.id }) { item ->
                        TaskCard(
                            task = item,
                            onToggle = { vm.toggleTask(item) },
                            onDelete = { requestDelete(item) },
                            onClick = {
                                selectedTask = item
                                showDetailDialog = true
                            }
                        )
                    }

                    if (completedTasks.isNotEmpty()) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showCompleted = !showCompleted }
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${stringResource(R.string.text_finished)} (${completedTasks.size})",
                                    fontSize = 14.sp,
                                    color = Color(0xFF999999),
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = stringResource(if (showCompleted) R.string.text_collapse else R.string.text_expand),
                                    fontSize = 14.sp,
                                    color = Color(0xFF2D2D2D)
                                )
                            }
                        }

                        if (showCompleted) {
                            items(completedTasks, key = { "completed_${it.id}" }) { item ->
                                TaskCard(
                                    task = item,
                                    onToggle = { vm.toggleTask(item) },
                                    onDelete = { requestDelete(item) },
                                    onClick = {
                                        selectedTask = item
                                        showDetailDialog = true
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showDetailDialog && selectedTask != null) {
            TaskDetailDialog(
                task = selectedTask!!,
                onClose = { showDetailDialog = false },
                onDelete = { requestDelete(selectedTask!!) },
                onComplete = { vm.toggleTask(selectedTask!!); showDetailDialog = false },
                onEdit = {
                    showDetailDialog = false
                }
            )
        }

        if (showDeleteDialog && taskToDelete != null) {
            AlertDialog(
                onDismissRequest = {
                    showDeleteDialog = false
                    taskToDelete = null
                },
                title = { Text(stringResource(R.string.text_delete_confirm_title), fontWeight = FontWeight.Light) },
                text = { Text(stringResource(R.string.text_delete_confirm_message), fontSize = 14.sp, color = Color(0xFF666666)) },
                confirmButton = {
                    TextButton(onClick = confirmDelete) {
                        Text(stringResource(R.string.btn_confirm), color = Color(0xFFE53935))
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showDeleteDialog = false
                        taskToDelete = null
                    }) {
                        Text(stringResource(R.string.btn_cancel), color = Color(0xFF999999))
                    }
                },
                containerColor = Color.White,
                shape = RoundedCornerShape(20.dp)
            )
        }

        if (showSettingsDialog) {
            AlertDialog(
                onDismissRequest = { showSettingsDialog = false }
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        stringResource(R.string.text_settings),
                        fontWeight = FontWeight.Light,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Text(
                        stringResource(R.string.text_language),
                        fontSize = 14.sp,
                        color = Color(0xFF999999),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = {
                                setAppLanguage(context, "zh")
                                showSettingsDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (Locale.getDefault().language == "zh") Color(0xFF2D2D2D) else Color(0xFFFAFAFA),
                                contentColor = if (Locale.getDefault().language == "zh") Color.White else Color(0xFF2D2D2D)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(stringResource(R.string.text_chinese))
                        }
                        Button(
                            onClick = {
                                setAppLanguage(context, "en")
                                showSettingsDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (Locale.getDefault().language == "en") Color(0xFF2D2D2D) else Color(0xFFFAFAFA),
                                contentColor = if (Locale.getDefault().language == "en") Color.White else Color(0xFF2D2D2D)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(stringResource(R.string.text_english))
                        }
                    }
                    TextButton(
                        onClick = { showSettingsDialog = false },
                        modifier = Modifier.padding(top = 16.dp).align(Alignment.End)
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
}

fun setAppLanguage(context: android.content.Context, language: String) {
    val locale = Locale(language)
    Locale.setDefault(locale)
    val resources = context.resources
    val config = android.content.res.Configuration()
    config.setLocale(locale)
    resources.updateConfiguration(config, resources.displayMetrics)

    val intent = android.content.Intent(context, MainActivity::class.java)
    intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
    context.startActivity(intent)
}

@Composable
fun TaskCard(task: Task, onToggle: () -> Unit, onDelete: () -> Unit, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .clickable { onToggle() },
                    contentAlignment = Alignment.Center
                ) {
                    if (task.isFinish) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF2D2D2D)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = stringResource(R.string.text_completed),
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .border(1.5.dp, Color(0xFFE0E0E0))
                        )
                    }
                }

                val priorityIcon = when (task.priority) {
                    Priority.HIGH.name -> "🚨"
                    Priority.MEDIUM.name -> "⚠️"
                    else -> "🟢"
                }

                if (!task.isFinish) {
                    Text(
                        text = priorityIcon,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Text(
                    text = task.title,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = if (!task.isFinish) 6.dp else 12.dp),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Light,
                    color = if (task.isFinish) Color(0xFFCCCCCC) else Color(0xFF2D2D2D),
                    textDecoration = if (task.isFinish) TextDecoration.LineThrough else null,
                    letterSpacing = 0.5.sp
                )

                IconButton(
                    onClick = { onDelete() },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = stringResource(R.string.btn_delete),
                        tint = Color(0xFFCCCCCC),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            if (task.deadline > 0) {
                val date = Calendar.getInstance()
                date.timeInMillis = task.deadline

                val now = Calendar.getInstance()
                val isToday = date.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR) &&
                        date.get(Calendar.YEAR) == now.get(Calendar.YEAR)

                val deadlineText = if (isToday) {
                    stringResource(R.string.text_tonight) + " ${date.get(Calendar.HOUR)}:${String.format("%02d", date.get(Calendar.MINUTE))}"
                } else {
                    "${date.get(Calendar.MONTH) + 1}/${date.get(Calendar.DAY_OF_MONTH)}"
                }

                Row(
                    modifier = Modifier.padding(top = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = deadlineText,
                        fontSize = 11.sp,
                        color = Color(0xFFCCCCCC),
                        modifier = Modifier.padding(start = 36.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TaskDetailDialog(
    task: Task,
    onClose: () -> Unit,
    onDelete: () -> Unit,
    onComplete: () -> Unit,
    onEdit: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onClose,
        title = {
            Text(
                task.title,
                fontWeight = FontWeight.Light,
                fontSize = 18.sp
            )
        },
        text = {
            Column {
                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(R.string.label_priority),
                        fontSize = 14.sp,
                        color = Color(0xFF999999),
                        modifier = Modifier.weight(1f)
                    )
                    val priorityLabel = when (task.priority) {
                        Priority.HIGH.name -> stringResource(R.string.label_high)
                        Priority.MEDIUM.name -> stringResource(R.string.label_medium)
                        else -> stringResource(R.string.label_low)
                    }
                    Text(
                        priorityLabel,
                        fontSize = 14.sp,
                        color = when (task.priority) {
                            Priority.HIGH.name -> Color(0xFFE53935)
                            Priority.MEDIUM.name -> Color(0xFFFF9800)
                            else -> Color(0xFF4CAF50)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(R.string.label_deadline),
                        fontSize = 14.sp,
                        color = Color(0xFF999999),
                        modifier = Modifier.weight(1f)
                    )
                    if (task.deadline > 0) {
                        val date = Calendar.getInstance()
                        date.timeInMillis = task.deadline
                        Text(
                            "${date.get(Calendar.MONTH) + 1}/${date.get(Calendar.DAY_OF_MONTH)} ${date.get(Calendar.HOUR)}:${date.get(Calendar.MINUTE)}",
                            fontSize = 14.sp,
                            color = Color(0xFF2D2D2D)
                        )
                    } else {
                        Text(
                            stringResource(R.string.text_none),
                            fontSize = 14.sp,
                            color = Color(0xFFCCCCCC)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(R.string.label_category),
                        fontSize = 14.sp,
                        color = Color(0xFF999999),
                        modifier = Modifier.weight(1f)
                    )
                    val categoryLabel = when (task.category) {
                        Category.STUDY.name -> stringResource(R.string.label_study)
                        Category.WORK.name -> stringResource(R.string.label_work)
                        Category.LIFE.name -> stringResource(R.string.label_life)
                        else -> stringResource(R.string.label_other)
                    }
                    Text(
                        categoryLabel,
                        fontSize = 14.sp,
                        color = Color(0xFF2D2D2D)
                    )
                }
            }
        },
        confirmButton = {
            Row {
                TextButton(onClick = onEdit) {
                    Text(stringResource(R.string.btn_edit), fontSize = 14.sp, color = Color(0xFF2D2D2D))
                }
                if (!task.isFinish) {
                    TextButton(onClick = onComplete) {
                        Text(stringResource(R.string.btn_complete), fontSize = 14.sp, color = Color(0xFF4CAF50))
                    }
                }
                TextButton(onClick = onDelete) {
                    Text(stringResource(R.string.btn_delete), fontSize = 14.sp, color = Color(0xFFE53935))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onClose) {
                Text(stringResource(R.string.btn_close), fontSize = 14.sp, color = Color(0xFF999999))
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(20.dp)
    )
}
