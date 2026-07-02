package com.example.quicktodo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle

class StatisticsActivity : ComponentActivity() {
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
                StatisticsScreen(
                    vm = vm,
                    onBack = { finish() }
                )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(vm: TaskViewModel, onBack: () -> Unit) {
    val completedCount by vm.completedCount.collectAsStateWithLifecycle(initialValue = 0)
    val totalCount by vm.totalCount.collectAsStateWithLifecycle(initialValue = 0)
    val todayCompletedCount by vm.todayCompletedCount.collectAsStateWithLifecycle(initialValue = 0)
    val todayTotalCount by vm.todayTotalCount.collectAsStateWithLifecycle(initialValue = 0)
    val streak by vm.currentStreak.collectAsStateWithLifecycle(initialValue = 0)

    LaunchedEffect(Unit) {
        vm.loadStats()
    }

    val todayTotal = todayTotalCount
    val todayCompleted = todayCompletedCount
    val completionRate = if (todayTotal > 0) todayCompleted * 100 / todayTotal else 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.title_statistics),
                        fontWeight = FontWeight.Light,
                        fontSize = 22.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.btn_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color(0xFF2D2D2D),
                    navigationIconContentColor = Color(0xFF2D2D2D)
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFFAFAFA))
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatCard(
                    label = stringResource(R.string.text_today_tasks),
                    value = todayTotal.toString(),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(16.dp))
                StatCard(
                    label = stringResource(R.string.text_completed),
                    value = todayCompleted.toString(),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressLabel(
                        progress = completionRate / 100f,
                        label = stringResource(R.string.text_completion_rate),
                        value = "$completionRate%"
                    )

                    CircularProgressLabel(
                        progress = if (streak > 0) 1f else 0f,
                        label = stringResource(R.string.text_current_streak),
                        value = "$streak ${stringResource(R.string.text_days)}"
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = stringResource(R.string.text_ai_insight),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Light,
                        color = Color(0xFF2D2D2D)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = when {
                            completionRate >= 80 -> stringResource(R.string.text_ai_insight_great)
                            completionRate >= 50 -> stringResource(R.string.text_ai_insight_good)
                            totalCount == 0 -> stringResource(R.string.text_ai_insight_start)
                            else -> stringResource(R.string.text_ai_insight_keep_going)
                        },
                        fontSize = 14.sp,
                        color = Color(0xFF666666)
                    )
                }
            }
        }
    }
}

@Composable
fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color(0xFF999999)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 32.sp,
                fontWeight = FontWeight.Light,
                color = Color(0xFF2D2D2D)
            )
        }
    }
}

@Composable
fun CircularProgressLabel(progress: Float, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(100.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                progress = { 1f },
                modifier = Modifier.fillMaxSize(),
                color = Color(0xFFF0F0F0),
                strokeWidth = 8.dp,
                trackColor = Color.Transparent
            )
            CircularProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxSize(),
                color = Color(0xFF2D2D2D),
                strokeWidth = 8.dp,
                trackColor = Color.Transparent
            )
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Light,
                color = Color(0xFF2D2D2D)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF999999)
        )
    }
}
