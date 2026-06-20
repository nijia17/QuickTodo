package com.example.quicktodo

import java.util.Calendar

class AIPriorityAnalyzer {

    private val urgentKeywords = listOf(
        "考试", "报告", "作业", "论文", "面试", "截止", "明天", "今天",
        "紧急", "马上", "立即", "必须", "重要", "关键", "答辩", "提交", "截止"
    )

    private val highPriorityKeywords = listOf(
        "作业", "作业", "实验", "项目", "课程", "复习", "预习", "考试",
        "报告", "论文", "答辩", "面试", "实习", "工作", "会议", "汇报"
    )

    fun analyzePriority(title: String, deadline: Long = 0): Priority {
        val urgencyScore = calculateUrgency(deadline)
        val importanceScore = calculateImportance(title)
        val totalScore = urgencyScore + importanceScore

        return when {
            totalScore >= 12 -> Priority.HIGH
            totalScore >= 7 -> Priority.MEDIUM
            else -> Priority.LOW
        }
    }

    private fun calculateUrgency(deadline: Long): Int {
        if (deadline == 0L) return 0

        val now = Calendar.getInstance().timeInMillis
        val diff = deadline - now

        val hours = diff / (1000 * 60 * 60)

        return when {
            hours <= 0 -> 6
            hours <= 6 -> 5
            hours <= 12 -> 4
            hours <= 24 -> 3
            hours <= 48 -> 2
            hours <= 72 -> 1
            else -> 0
        }
    }

    private fun calculateImportance(title: String): Int {
        var score = 0

        urgentKeywords.forEach { keyword ->
            if (title.contains(keyword)) {
                score += 3
            }
        }

        highPriorityKeywords.forEach { keyword ->
            if (title.contains(keyword)) {
                score += 2
            }
        }

        if (title.length > 20) {
            score += 1
        }

        return score.coerceAtMost(8)
    }

    fun analyzeCategory(title: String): Category {
        val studyKeywords = listOf("作业", "考试", "复习", "预习", "论文", "报告", "实验", "课程", "答辩")
        val workKeywords = listOf("工作", "会议", "汇报", "面试", "实习", "项目")
        val lifeKeywords = listOf("购物", "吃饭", "运动", "休息", "娱乐", "电影", "游戏")

        return when {
            studyKeywords.any { title.contains(it) } -> Category.STUDY
            workKeywords.any { title.contains(it) } -> Category.WORK
            lifeKeywords.any { title.contains(it) } -> Category.LIFE
            else -> Category.OTHER
        }
    }

    fun getPrioritySuggestion(task: Task): String {
        return when (task.priority) {
            Priority.HIGH.name -> "🎯 高优先级：建议立即处理"
            Priority.MEDIUM.name -> "📋 中优先级：今天内完成"
            Priority.LOW.name -> "☕ 低优先级：有空再做"
            else -> "📋 普通任务"
        }
    }
}