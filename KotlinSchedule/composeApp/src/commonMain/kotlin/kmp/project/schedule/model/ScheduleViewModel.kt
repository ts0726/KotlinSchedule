package kmp.project.schedule.model

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import kmp.project.schedule.ScheduleSDK
import kmp.project.schedule.database.Schedule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

class ScheduleViewModel(private val sdk: ScheduleSDK): ViewModel() {
    val id = mutableStateOf(0)
    val uuid = mutableStateOf("")
    val title = mutableStateOf("")
    val content = mutableStateOf("")
    val date = mutableStateOf( Clock.System.todayIn(TimeZone.currentSystemDefault()) )
    val repeatMode = mutableStateOf(0)
    val location = mutableStateOf("未设定")

    val schedules = mutableStateListOf<Schedule>()
    val schedulesToDelete = mutableStateListOf<String>()

    suspend fun onSave(navController: NavController, currentDate: LocalDate) {
        val schedule = Schedule(
            id = id.value.toLong(),
            uuid = uuid.value,
            title = title.value,
            content = content.value,
            date = date.value.toEpochDays().toLong(),
            repeatMode = repeatMode.value.toLong(),
            location = location.value
        )
        if (loadScheduleByUUID(uuid.value) != null) {
            updateSchedule(schedule, currentDate)
        } else {
            val uuid = sdk.insertSchedule(
                title = title.value,
                content = content.value,
                date = date.value.toEpochDays().toLong(),
                repeatMode = repeatMode.value,
                location = location.value
            )
            if (date.value.toEpochDays() == currentDate.toEpochDays()) {
                schedules.add(0, schedule.copy(uuid = uuid))
            }
        }

        reset()
        withContext(Dispatchers.Main) {
            navController.popBackStack()
        }
    }

    fun reset() {
        id.value = 0
        uuid.value = ""
        title.value = ""
        content.value = ""
        date.value = Clock.System.todayIn(TimeZone.currentSystemDefault())
        repeatMode.value = 0
        location.value = "未设定"
    }

    fun loadScheduleByUUID(uuid: String): Schedule? {
        return sdk.getScheduleByUuid(uuid)
    }

    fun loadSchedules(date: MutableState<LocalDate>) {
        schedules.clear()
        schedules.addAll(sdk.getScheduleByDate(date.value.toEpochDays().toLong()))
    }

    fun deleteSchedule(uuid: String) {
        sdk.deleteSchedule(uuid)
        schedules.removeIf { it.uuid == uuid }
    }

    fun deleteSchedules() {
        schedulesToDelete.forEach {
            sdk.deleteSchedule(it)
            schedules.removeIf { schedule -> schedule.uuid == it }
        }
        schedulesToDelete.clear()
    }

    private fun updateSchedule(schedule: Schedule, currentDate: LocalDate) {
        val index = schedules.indexOfFirst { it.uuid == schedule.uuid }
        sdk.updateSchedule(schedule)
        if (date.value.toEpochDays() == currentDate.toEpochDays()) {
            println("TEST")
            schedules.set(index = index, element = schedule)
        } else {
            println("new date: " + LocalDate.fromEpochDays(schedule.date.toInt()))
            println("current date: " + currentDate)
            schedules.removeIf { schedule.uuid == it.uuid }
        }

    }
}