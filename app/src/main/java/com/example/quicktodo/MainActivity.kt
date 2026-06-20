package com.example.quicktodo

import android.os.Bundle
import android.widget.DatePicker
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.ui.text.TextStyle
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoPage(vm: TaskViewModel) {
    val taskList by vm.allTask.collectAsStateWithLifecycle(initialValue = emptyList())
    val completedCount by vm.completedCount.collectAsStateWithLifecycle(initialValue = 0)
    val totalCount by vm.totalCount.collectAsStateWithLifecycle(initialValue = 0)

    var showAddDialog by remember { mutableStateOf(false) }
    var inputTitle by remember { mutableStateOf("") }
    var selectedDeadline by remember { mutableStateOf(0L) }
    var selectedCategory by remember { mutableStateOf(Category.OTHER.name) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        vm.loadStats()
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
                    IconButton(onClick = { showSettingsDialog = true }) {
                        Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.text_settings))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Color(0xFF2D2D2D),
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                elevation = FloatingActionButtonDefaults.elevation(0.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Task")
            }
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
                    text = "${taskList.count { !it.isFinish }} ${stringResource(R.string.title_today).lowercase()}",
                    fontSize = 14.sp,
                    color = Color(0xFF999999)
                )
                Text(
                    text = "${taskList.count { it.isFinish }} ${stringResource(R.string.text_completed).lowercase()}",
                    fontSize = 14.sp,
                    color = Color(0xFF999999)
                )
            }

            if (totalCount > 0) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.text_stats),
                            fontSize = 14.sp,
                            color = Color(0xFF999999),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2D2D2D)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${if (totalCount > 0) (completedCount * 100 / totalCount) else 0}%",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Column(modifier = Modifier.padding(start = 16.dp)) {
                                Text(
                                    text = stringResource(R.string.text_completion_rate),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Light
                                )
                                Text(
                                    text = "${completedCount}/${totalCount} ${stringResource(R.string.text_completed)}",
                                    fontSize = 12.sp,
                                    color = Color(0xFF999999)
                                )
                            }
                        }
                    }
                }
            }

            Text(
                text = stringResource(R.string.text_tasks),
                fontSize = 14.sp,
                color = Color(0xFF999999),
                modifier = Modifier.padding(bottom = 8.dp)
            )

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
                    items(taskList, key = { it.id }) { item ->
                        TaskCard(
                            task = item,
                            onToggle = { vm.toggleTask(item) },
                            onDelete = { vm.removeTask(item) },
                            prioritySuggestion = vm.getPrioritySuggestion(item)
                        )
                    }
                }
            }
        }

        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = {
                    showAddDialog = false
                    inputTitle = ""
                    selectedDeadline = 0L
                    selectedCategory = Category.OTHER.name
                },
                title = {
                    Text(
                        stringResource(R.string.title_add_task),
                        fontWeight = FontWeight.Light,
                        fontSize = 18.sp
                    )
                },
                text = {
                    Column {
                        TextField(
                            value = inputTitle,
                            onValueChange = { inputTitle = it },
                            placeholder = { Text(stringResource(R.string.hint_task_input), color = Color(0xFFCCCCCC)) },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = TextStyle(color = Color(0xFF2D2D2D)),
                            colors = TextFieldDefaults.colors(
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                cursorColor = Color(0xFF2D2D2D),
                                focusedContainerColor = Color(0xFFFAFAFA),
                                unfocusedContainerColor = Color(0xFFFAFAFA)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 12.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val categories = Category.values().map {
                                when (it) {
                                    Category.STUDY -> "📚 ${stringResource(R.string.label_study)}"
                                    Category.WORK -> "💼 ${stringResource(R.string.label_work)}"
                                    Category.LIFE -> "🏠 ${stringResource(R.string.label_life)}"
                                    Category.OTHER -> "📌 ${stringResource(R.string.label_other)}"
                                }
                            }
                            categories.forEachIndexed { index, cat ->
                                Button(
                                    onClick = { selectedCategory = Category.values()[index].name },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (selectedCategory == Category.values()[index].name) Color(0xFF2D2D2D) else Color(0xFFFAFAFA),
                                        contentColor = if (selectedCategory == Category.values()[index].name) Color.White else Color(0xFF2D2D2D)
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                                ) {
                                    Text(cat, fontSize = 10.sp)
                                }
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 12.dp).fillMaxWidth()
                        ) {
                            Text(
                                text = stringResource(R.string.label_deadline),
                                fontSize = 14.sp,
                                color = Color(0xFF999999),
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(onClick = {
                                val calendar = Calendar.getInstance()
                                android.app.DatePickerDialog(
                                    context,
                                    { _, year, month, day ->
                                        val selectedDate = Calendar.getInstance()
                                        selectedDate.set(year, month, day, 23, 59, 59)
                                        selectedDeadline = selectedDate.timeInMillis
                                    },
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH),
                                    calendar.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            }) {
                                Text(
                                    text = if (selectedDeadline > 0) {
                                        val date = Calendar.getInstance()
                                        date.timeInMillis = selectedDeadline
                                        "${date.get(Calendar.MONTH) + 1}/${date.get(Calendar.DAY_OF_MONTH)}"
                                    } else {
                                        stringResource(R.string.btn_select_date)
                                    },
                                    fontSize = 14.sp,
                                    color = Color(0xFF2D2D2D)
                                )
                            }
                        }

                        Text(
                            text = stringResource(R.string.text_ai_auto_analyze),
                            fontSize = 12.sp,
                            color = Color(0xFF666666),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (inputTitle.isNotBlank()) {
                                vm.addTaskWithAI(inputTitle.trim(), selectedDeadline)
                                showAddDialog = false
                                inputTitle = ""
                                selectedDeadline = 0L
                                selectedCategory = Category.OTHER.name
                            }
                        },
                        enabled = inputTitle.isNotBlank()
                    ) {
                        Text(
                            stringResource(R.string.btn_add),
                            fontSize = 14.sp,
                            color = Color(0xFF2D2D2D)
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showAddDialog = false
                        inputTitle = ""
                        selectedDeadline = 0L
                        selectedCategory = Category.OTHER.name
                    }) {
                        Text(
                            stringResource(R.string.btn_cancel),
                            fontSize = 14.sp,
                            color = Color(0xFF999999)
                        )
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
    
    // 重启Activity以应用语言变化
    val intent = android.content.Intent(context, MainActivity::class.java)
    intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
    context.startActivity(intent)
}

@Composable
fun TaskCard(task: Task, onToggle: () -> Unit, onDelete: () -> Unit, prioritySuggestion: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
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
                                contentDescription = "Completed",
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

                val priorityColor = when (task.priority) {
                    Priority.HIGH.name -> Color(0xFFE53935)
                    Priority.MEDIUM.name -> Color(0xFFFF9800)
                    else -> Color(0xFF4CAF50)
                }

                val priorityLabel = when (task.priority) {
                    Priority.HIGH.name -> "🚨 ${stringResource(R.string.label_high)}"
                    Priority.MEDIUM.name -> "⚠️ ${stringResource(R.string.label_medium)}"
                    else -> "📌 ${stringResource(R.string.label_low)}"
                }

                Text(
                    text = priorityLabel,
                    fontSize = 12.sp,
                    color = priorityColor,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(start = 12.dp)
                )

                val categoryIcon = when (task.category) {
                    Category.STUDY.name -> "📚"
                    Category.WORK.name -> "💼"
                    Category.LIFE.name -> "🏠"
                    else -> "📌"
                }

                Text(
                    text = categoryIcon,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(start = 8.dp)
                )

                Text(
                    text = task.title,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp),
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
                        contentDescription = "Delete",
                        tint = Color(0xFFCCCCCC),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            if (task.deadline > 0) {
                val date = Calendar.getInstance()
                date.timeInMillis = task.deadline
                val deadlineText = "${date.get(Calendar.MONTH) + 1}/${date.get(Calendar.DAY_OF_MONTH)}"
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📅 ${stringResource(R.string.label_deadline)}: $deadlineText",
                        fontSize = 12.sp,
                        color = Color(0xFF999999),
                        modifier = Modifier.padding(start = 36.dp)
                    )
                }
            }

            if (!task.isFinish) {
                Text(
                    text = prioritySuggestion,
                    fontSize = 12.sp,
                    color = Color(0xFF666666),
                    modifier = Modifier.padding(top = 4.dp).padding(start = 36.dp)
                )
            }
        }
    }
}