package kmp.project.schedule.data

import kmp.project.schedule.util.getDayTimestamp


data class ScheduleData(
    val title: String = "",
    val content: String = "",
    val date: Long = getDayTimestamp(),
    val repeatMode: Int = 0,
    val location: String = "",
)
