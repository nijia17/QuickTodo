package com.example.quicktodo

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SettingsScreen(
                onBack = { finish() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences("quicktodo_settings", Context.MODE_PRIVATE) }

    var widgetEnabled by remember { mutableStateOf(true) }
    var aiSortEnabled by remember { mutableStateOf(true) }
    var reminderEnabled by remember { mutableStateOf(prefs.getBoolean("reminder_enabled", true)) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.title_settings),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Light,
                        color = Color(0xFF2D2D2D)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = Color(0xFF2D2D2D)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.label_feature_settings),
                fontSize = 12.sp,
                color = Color(0xFF999999),
                modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    SwitchSettingItem(
                        title = stringResource(R.string.label_widget),
                        desc = stringResource(R.string.desc_widget),
                        checked = widgetEnabled,
                        onCheckedChange = { widgetEnabled = it }
                    )
                    Divider(color = Color(0xFFE8E8E8))
                    SwitchSettingItem(
                        title = stringResource(R.string.label_ai_sort),
                        desc = stringResource(R.string.desc_ai_sort),
                        checked = aiSortEnabled,
                        onCheckedChange = { aiSortEnabled = it }
                    )
                    Divider(color = Color(0xFFE8E8E8))
                    SwitchSettingItem(
                        title = stringResource(R.string.label_reminder),
                        desc = stringResource(R.string.desc_reminder),
                        checked = reminderEnabled,
                        onCheckedChange = {
                            reminderEnabled = it
                            prefs.edit().putBoolean("reminder_enabled", it).apply()
                        }
                    )

                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.label_about),
                fontSize = 12.sp,
                color = Color(0xFF999999),
                modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.label_version),
                        fontSize = 16.sp,
                        color = Color(0xFF2D2D2D)
                    )
                    Text(
                        text = "1.0.0",
                        fontSize = 14.sp,
                        color = Color(0xFF999999)
                    )
                }
            }
        }
    }
}

@Composable
private fun SwitchSettingItem(
    title: String,
    desc: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                color = Color(0xFF2D2D2D)
            )
            Text(
                text = desc,
                fontSize = 12.sp,
                color = Color(0xFF999999)
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF2D2D2D),
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFFCCCCCC)
            )
        )
    }
}
