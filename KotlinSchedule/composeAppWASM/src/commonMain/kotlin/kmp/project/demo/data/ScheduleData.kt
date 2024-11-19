package kmp.project.demo.data

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn


data class ScheduleData(
    val title: String = "",
    val content: String = "",
    val date: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
    val repeatMode: Int = 0,
    val location: String = "",
)
