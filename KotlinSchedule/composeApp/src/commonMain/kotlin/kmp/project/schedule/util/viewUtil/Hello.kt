package kmp.project.schedule.util.viewUtil

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

fun sayHello(): String {
    val currentHour = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).hour
    return when (currentHour) {
        in 0..4 -> "凌晨好"
        in 5..11 -> "早上好"
        in 12..13 -> "中午好"
        in 14..17 -> "下午好"
        else -> "晚上好"
    }
}

