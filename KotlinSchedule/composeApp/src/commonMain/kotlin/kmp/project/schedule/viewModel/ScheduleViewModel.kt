package kmp.project.schedule.viewModel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import kmp.project.schedule.database.Schedule
import kmp.project.schedule.entity.ScheduleEntity
import kmp.project.schedule.net.ApiResult
import kmp.project.schedule.net.scheduleApi
import kmp.project.schedule.sdk.ScheduleSDK
import kmp.project.schedule.util.timeUtil.getTimestamp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

class ScheduleViewModel(private val sdk: ScheduleSDK): ViewModel() {
    val id = mutableStateOf(-1)
    val uuid = mutableStateOf("")
    val userName = mutableStateOf("")
    val title = mutableStateOf("")
    val content = mutableStateOf("")
    val date = mutableStateOf( Clock.System.todayIn(TimeZone.currentSystemDefault()) )
    val repeatMode = mutableStateOf(RepeatMode.NONE)
    val location = mutableStateOf("未设定")
    val sequence = mutableStateOf(0)
    val finished = mutableStateOf(false)
    var schedules = mutableStateListOf<Schedule>()
    val schedulesToDelete = mutableStateListOf<String>()

    suspend fun onSave(
        navController: NavController,
        currentDate: LocalDate,
        showSnackBar: (String) -> Unit
    ) {
        val schedule = Schedule(
            id = id.value.toLong(),
            uuid = uuid.value,
            username = userName.value,
            title = title.value,
            content = content.value,
            date = date.value.toEpochDays().toLong(),
            repeatMode = repeatMode.value.toString(),
            location = location.value,
            sequence = sequence.value.toLong(),
            finished = finished.value.toString(),
            timestamp = 0
        )
        if (loadScheduleByUUID(uuid.value) != null) {
            updateSchedule(
                schedule = schedule.copy(timestamp = getTimestamp()),
                currentDate = currentDate,
                showSnackBar = showSnackBar
            )
        } else {
            val uuid = sdk.insertSchedule(
                username = userName.value,
                title = title.value,
                content = content.value,
                date = date.value.toEpochDays().toLong(),
                repeatMode = repeatMode.value.toString(),
                location = location.value,
                sequence = id.value,
                finished = finished.value.toString()
            )
            if (userName.value != "") {
                viewModelScope.launch {
                    val result = scheduleApi.addSchedule(
                        scheduleToEntity(schedule.copy(uuid = uuid, timestamp = getTimestamp()))
                    )
                    if (result is ApiResult.Success) {
                        showSnackBar("日程 ${schedule.title} 已上传")
                    } else if (result is ApiResult.Error) {
                        showSnackBar("日程 ${schedule.title} 上传失败：${result.message}")
                    }
                }
            }
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
        id.value = -1
        uuid.value = ""
        userName.value = ""
        title.value = ""
        content.value = ""
        date.value = Clock.System.todayIn(TimeZone.currentSystemDefault())
        repeatMode.value = RepeatMode.NONE
        location.value = "未设定"
        finished.value = false
    }

    fun addScheduleFromSseServer(scheduleEntity: ScheduleEntity) {
        val schedule = entityToSchedule(scheduleEntity)
        val currentDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
        sdk.insertSchedule(schedule)
        if (schedule.date.toInt() == currentDate.toEpochDays()) {
            schedules.add(0, schedule)
        }
    }

    fun loadScheduleByUUID(uuid: String): Schedule? {
        return sdk.getScheduleByUuid(uuid)
    }

    fun loadSchedules(userName: String, date: MutableState<LocalDate>) {
        schedules.clear()
        schedules.addAll(sdk.getScheduleByDate(userName, date.value.toEpochDays().toLong()))
    }

    fun deleteSchedule(
        uuid: String,
        userName: String,
        showSnackBar: (String) -> Unit
    ) {
        if (userName != "") {
            viewModelScope.launch {
                val result = scheduleApi.deleteSchedule(uuid)
                if (result is ApiResult.Success) {
                    showSnackBar("已同步删除云端日程")
                } else if (result is ApiResult.Error) {
                    showSnackBar("云端日程删除失败：${result.message}")
                }
            }
        }
        sdk.deleteSchedule(uuid)
        schedules.removeIf { it.uuid == uuid }
    }

    fun deleteSchedules(userName: String, showSnackBar: (String) -> Unit) {
        if (userName != "") {
            viewModelScope.launch {
                val result = scheduleApi.deleteSchedules(schedulesToDelete)
                if (result is ApiResult.Success) {
                    val message: String = if (result.data.failure == 0) {
                        "日程同步删除成功${result.data.success}条"
                    } else {
                        "日程同步删除成功${result.data.success}条，失败${result.data.failure}条\n" +
                                "原因：待删除日程中包含离线日程，此类日程不存在于云端"
                    }
                    showSnackBar(message)
                } else if (result is ApiResult.Error) {
                    showSnackBar("云端日程删除失败：${result.message}")
                }
            }
        }

        schedulesToDelete.forEach {
            sdk.deleteSchedule(it)
            schedules.removeIf { schedule -> schedule.uuid == it }
        }
        schedulesToDelete.clear()
    }

    private fun updateSchedule(
        schedule: Schedule,
        currentDate: LocalDate,
        showSnackBar: (String) -> Unit
    ) {
        val index = schedules.indexOfFirst { it.uuid == schedule.uuid }
        if (schedule.username != "")
        viewModelScope.launch {
            val result = scheduleApi.updateSchedules(scheduleToEntity(schedule))
            if (result is ApiResult.Success) {
                showSnackBar("云端更新成功")
            } else if (result is ApiResult.Error) {
                showSnackBar("云端更新失败：${result.message}")
            }
        }
        sdk.updateSchedule(schedule)
        if (date.value.toEpochDays() == currentDate.toEpochDays()) {
            schedules.set(index = index, element = schedule)
        } else {
            schedules.removeIf { schedule.uuid == it.uuid }
        }
    }

    fun reorderSchedules() {
        val updatedSchedules = schedules.mapIndexed{ index, schedule ->
            schedule.copy(sequence = index.toLong())
        }
        sdk.updateSchedules(updatedSchedules)
    }

    private fun scheduleToEntity(schedule: Schedule): ScheduleEntity {
        return ScheduleEntity(
            uuid = schedule.uuid,
            userName = schedule.username,
            title = schedule.title,
            content = schedule.content ?: "",
            date = schedule.date,
            repeatMode = RepeatMode.valueOf(schedule.repeatMode),
            location = schedule.location ?: "未设定",
            sequence = schedule.sequence.toInt(),
            finished = schedule.finished.toBoolean(),
            timestamp = schedule.timestamp
        )
    }

    private fun entityToSchedule(scheduleEntity: ScheduleEntity): Schedule {
        return Schedule (
            id = 0,
            uuid = scheduleEntity.uuid,
            username = scheduleEntity.userName,
            title = scheduleEntity.title,
            content = "来自SSE Server的日程：" + scheduleEntity.content,
            date = scheduleEntity.date,
            repeatMode = scheduleEntity.repeatMode.toString(),
            location = scheduleEntity.location,
            sequence = scheduleEntity.sequence.toLong(),
            finished = scheduleEntity.finished.toString(),
            timestamp = scheduleEntity.timestamp
        )
    }
}

enum class RepeatMode {
    NONE,
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY
}