package kmp.project.schedule.navigation.key

import kmp.project.schedule.database.Schedule

data object ScheduleList

data class ScheduleDetail(
    val schedule: Schedule
)

data object CreateSchedule