package kmp.project.schedule.model

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import kmp.project.schedule.ScheduleSDK
import kmp.project.schedule.database.Schedule
import kotlinx.datetime.LocalDate

class ScheduleViewModel: ViewModel() {
    val schedules = mutableStateListOf<Schedule>()

    fun loadSchedules(
        sdk: ScheduleSDK,
        date: MutableState<LocalDate>
    ) {
        schedules.clear()
        schedules.addAll(sdk.getScheduleByDate(date.value.toEpochDays().toLong()))
    }

    fun deleteSchedule(
        sdk: ScheduleSDK,
        uuid: String
    ) {
        sdk.deleteSchedule(uuid)
        schedules.removeIf { it.uuid == uuid }
    }
}