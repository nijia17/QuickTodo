package com.example.quicktodo

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar

class AIAnalyzeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val title = intent.getStringExtra(EXTRA_TITLE) ?: ""
        val deadline = intent.getLongExtra(EXTRA_DEADLINE, 0L)
        val category = intent.getStringExtra(EXTRA_CATEGORY) ?: Category.OTHER.name
        val recurType = intent.getStringExtra(EXTRA_RECUR_TYPE) ?: RecurType.NONE.name

        setContent {
            AIAnalyzeScreen(
                title = title,
                deadline = deadline,
                category = category,
                recurType = recurType,
                onFinished = { result ->
                    setResult(Activity.RESULT_OK, Intent().apply {
                        putExtra(EXTRA_RESULT_TITLE, result.title)
                        putExtra(EXTRA_RESULT_DEADLINE, result.deadline)
                        putExtra(EXTRA_RESULT_CATEGORY, result.category)
                        putExtra(EXTRA_RESULT_PRIORITY, result.priority)
                        putExtra(EXTRA_RESULT_RECUR_TYPE, result.recurType)
                    })
                    finish()
                }
            )
        }
    }

    companion object {
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_DEADLINE = "extra_deadline"
        const val EXTRA_CATEGORY = "extra_category"
        const val EXTRA_RECUR_TYPE = "extra_recur_type"

        const val EXTRA_RESULT_TITLE = "extra_result_title"
        const val EXTRA_RESULT_DEADLINE = "extra_result_deadline"
        const val EXTRA_RESULT_CATEGORY = "extra_result_category"
        const val EXTRA_RESULT_PRIORITY = "extra_result_priority"
        const val EXTRA_RESULT_RECUR_TYPE = "extra_result_recur_type"
    }
}

data class AIAnalyzeResult(
    val title: String,
    val deadline: Long,
    val category: String,
    val priority: String,
    val recurType: String
)

@Composable
fun AIAnalyzeScreen(
    title: String,
    deadline: Long,
    category: String,
    recurType: String,
    onFinished: (AIAnalyzeResult) -> Unit
) {
    var analyzing by remember { mutableStateOf(true) }
    var currentStep by remember { mutableStateOf(0) }
    var resultPriority by remember { mutableStateOf(Priority.LOW) }
    var resultSuggestion by remember { mutableStateOf("") }

    val aiAnalyzer = remember { AIPriorityAnalyzer() }
    val steps = listOf(
        stringResource(R.string.text_ai_step_deadline),
        stringResource(R.string.text_ai_step_keywords),
        stringResource(R.string.text_ai_step_workload),
        stringResource(R.string.text_ai_step_existing_tasks)
    )

    LaunchedEffect(Unit) {
        resultPriority = aiAnalyzer.analyzePriority(title, deadline)
        resultSuggestion = aiAnalyzer.getPrioritySuggestion(
            Task(
                title = title,
                deadline = deadline,
                category = category,
                priority = resultPriority.name
            )
        )

        for (i in steps.indices) {
            kotlinx.coroutines.delay(600)
            currentStep = i + 1
        }
        kotlinx.coroutines.delay(400)
        analyzing = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            if (analyzing) {
                CircularProgressIndicator(
                    color = Color(0xFF2D2D2D),
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(48.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = stringResource(R.string.text_analyzing),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Light,
                    color = Color(0xFF2D2D2D)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.text_ai_evaluating),
                    fontSize = 14.sp,
                    color = Color(0xFF999999)
                )

                Spacer(modifier = Modifier.height(40.dp))

                steps.forEachIndexed { index, step ->
                    StepItem(
                        label = step,
                        checked = index < currentStep,
                        isLast = index == steps.size - 1
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color(0xFF2D2D2D), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = stringResource(R.string.text_ai_analysis_done),
                    fontSize = 14.sp,
                    color = Color(0xFF999999)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Light,
                    color = Color(0xFF2D2D2D)
                )

                Spacer(modifier = Modifier.height(24.dp))

                val priorityLabel = when (resultPriority) {
                    Priority.HIGH -> stringResource(R.string.label_high)
                    Priority.MEDIUM -> stringResource(R.string.label_medium)
                    Priority.LOW -> stringResource(R.string.label_low)
                }
                val priorityColor = when (resultPriority) {
                    Priority.HIGH -> Color(0xFFE53935)
                    Priority.MEDIUM -> Color(0xFFFF9800)
                    Priority.LOW -> Color(0xFF4CAF50)
                }
                val categoryLabel = when (category) {
                    Category.STUDY.name -> stringResource(R.string.label_study)
                    Category.WORK.name -> stringResource(R.string.label_work)
                    Category.LIFE.name -> stringResource(R.string.label_life)
                    else -> stringResource(R.string.label_other)
                }

                ResultItem(label = stringResource(R.string.label_deadline), value = formatDeadline(deadline))
                ResultItem(label = stringResource(R.string.label_category), value = categoryLabel)
                ResultItem(label = stringResource(R.string.label_priority), value = priorityLabel, valueColor = priorityColor)

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = resultSuggestion,
                    fontSize = 13.sp,
                    color = Color(0xFF666666)
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        onFinished(
                            AIAnalyzeResult(
                                title = title,
                                deadline = deadline,
                                category = category,
                                priority = resultPriority.name,
                                recurType = recurType
                            )
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2D2D2D),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        stringResource(R.string.btn_done),
                        fontSize = 16.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun StepItem(label: String, checked: Boolean, isLast: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        val alpha by animateFloatAsState(targetValue = if (checked) 1f else 0.3f)

        Box(
            modifier = Modifier
                .size(24.dp)
                .background(
                    if (checked) Color(0xFF2D2D2D) else Color(0xFFE0E0E0),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (checked) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = label,
            fontSize = 14.sp,
            color = Color(0xFF2D2D2D),
            modifier = Modifier.alpha(alpha)
        )
    }
}

@Composable
fun ResultItem(label: String, value: String, valueColor: Color = Color(0xFF2D2D2D)) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color(0xFF999999)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = valueColor,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun formatDeadline(deadline: Long): String {
    if (deadline <= 0) return "无"
    val date = Calendar.getInstance()
    date.timeInMillis = deadline
    val month = date.get(Calendar.MONTH) + 1
    val day = date.get(Calendar.DAY_OF_MONTH)
    val hour = String.format("%02d", date.get(Calendar.HOUR_OF_DAY))
    val minute = String.format("%02d", date.get(Calendar.MINUTE))
    return "${date.get(Calendar.YEAR)}-${month}-${day} ${hour}:${minute}"
}
