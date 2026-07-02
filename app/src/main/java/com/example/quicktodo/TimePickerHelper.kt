@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.quicktodo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun SimpleTimePickerDialog(
    initialHour: Int = 12,
    initialMinute: Int = 0,
    onConfirm: (hour: Int, minute: Int) -> Unit,
    onDismiss: () -> Unit
) {
    val hours = remember { (0..23).map { String.format("%02d", it) } }
    val minutes = remember { (0..59).map { String.format("%02d", it) } }

    var selectedHour by remember { mutableStateOf(String.format("%02d", initialHour)) }
    var selectedMinute by remember { mutableStateOf(minutes.minByOrNull { kotlin.math.abs(it.toInt() - initialMinute) } ?: "00") }

    var hourExpanded by remember { mutableStateOf(false) }
    var minuteExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = stringResource(R.string.title_select_time),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Light,
                    color = Color(0xFF2D2D2D)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DropdownSelector(
                        label = stringResource(R.string.label_hour),
                        options = hours,
                        selected = selectedHour,
                        expanded = hourExpanded,
                        onExpandedChange = { hourExpanded = it },
                        onSelect = {
                            selectedHour = it
                            hourExpanded = false
                        },
                        modifier = Modifier.weight(1f)
                    )

                    Text(":", fontSize = 20.sp, color = Color(0xFF2D2D2D))

                    DropdownSelector(
                        label = stringResource(R.string.label_minute),
                        options = minutes,
                        selected = selectedMinute,
                        expanded = minuteExpanded,
                        onExpandedChange = { minuteExpanded = it },
                        onSelect = {
                            selectedMinute = it
                            minuteExpanded = false
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.btn_cancel), color = Color(0xFF999999))
                    }
                    TextButton(
                        onClick = {
                            onConfirm(selectedHour.toIntOrNull() ?: 0, selectedMinute.toIntOrNull() ?: 0)
                        }
                    ) {
                        Text(stringResource(R.string.btn_confirm), color = Color(0xFF2D2D2D))
                    }
                }
            }
        }
    }
}

@Composable
private fun DropdownSelector(
    label: String,
    options: List<String>,
    selected: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = onExpandedChange,
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label, fontSize = 12.sp) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor(),
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 16.sp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF2D2D2D),
                unfocusedBorderColor = Color(0xFFE8E8E8),
                cursorColor = Color(0xFF2D2D2D)
            )
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, fontSize = 15.sp) },
                    onClick = { onSelect(option) }
                )
            }
        }
    }
}
