package com.example.quicktodo

import java.util.Calendar

object RecurringTaskHelper {

    fun generateNextTask(original: Task): Task? {
        if (!original.isRecurring || original.recurType == RecurType.NONE.name) return null

        val baseDeadline = if (original.deadline > 0) original.deadline else System.currentTimeMillis()
        val calendar = Calendar.getInstance().apply { timeInMillis = baseDeadline }

        when (original.recurType) {
            RecurType.DAILY.name -> {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
            RecurType.WEEKDAY.name -> {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
                while (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY ||
                    calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                }
            }
            RecurType.WEEKLY.name -> {
                calendar.add(Calendar.WEEK_OF_YEAR, 1)
            }
            else -> return null
        }

        return original.copy(
            id = 0,
            isFinish = false,
            deadline = calendar.timeInMillis
        )
    }
}
