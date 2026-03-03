package kmp.project.schedule.viewModel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kmp.project.schedule.database.Schedule
import kmp.project.schedule.entity.ScheduleEntity
import kmp.project.schedule.domain.repository.LocalRepositoryImpl
import kmp.project.schedule.domain.sync.SyncManager
import kmp.project.schedule.domain.sync.SyncStatus
import kmp.project.schedule.entity.RepeatMode
import kmp.project.schedule.util.DeviceUtil
import kmp.project.schedule.util.timeUtil.getTimestamp
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.todayIn
import kotlin.time.Clock

class ScheduleViewModel(
    private val repository: LocalRepositoryImpl,
    private val syncManager: SyncManager
): ViewModel() {
    val id = mutableIntStateOf(-1)
    val uuid = mutableStateOf("")
    val userName = mutableStateOf("")
    val title = mutableStateOf("")
    val content = mutableStateOf("")
    val date = mutableStateOf( Clock.System.todayIn(TimeZone.currentSystemDefault()) )
    val repeatMode = mutableStateOf(RepeatMode.NONE)
    val location = mutableStateOf("未设定")
    val sequence = mutableIntStateOf(0)
    val finished = mutableStateOf(false)
    val device = DeviceUtil.getDeviceName()
    val syncStatus = mutableStateOf(SyncStatus.PENDING)
    var schedules = mutableStateListOf<Schedule>()
    val schedulesToDelete = mutableStateListOf<String>()

    var transportUuid = ""

    fun onSave(
        currentDate: LocalDate,
        showSnackBar: (String) -> Unit
    ) {
        val schedule = Schedule(
            id = id.intValue.toLong(),
            uuid = uuid.value,
            username = userName.value,
            title = title.value,
            content = content.value,
            date = date.value.toEpochDays(),
            repeatMode = repeatMode.value.toString(),
            location = location.value,
            sequence = sequence.intValue.toLong(),
            finished = finished.value.toString(),
            timestamp = 0,
            device = device,
            sync_status = syncStatus.toString()
        )
        if (loadScheduleByUUID(uuid.value) != null) {
            updateSchedule(
                schedule = schedule.copy(timestamp = getTimestamp()),
                currentDate = currentDate,
                showSnackBar = showSnackBar
            )
        } else {
            val uuid = repository.insertSchedule(
                username = userName.value,
                title = title.value,
                content = content.value,
                date = date.value.toEpochDays(),
                repeatMode = repeatMode.value.toString(),
                location = location.value,
                sequence = id.intValue,
                finished = finished.value.toString(),
                device = device,
                syncStatus = syncStatus.toString()
            )
            if (userName.value != "") {
                viewModelScope.launch {
                    val result = syncManager.syncAddSchedule(
                        scheduleToEntity(schedule.copy(uuid = uuid, timestamp = getTimestamp()))
                    )
                    if (result.isSuccess) {
                        repository.updateScheduleSyncStatus(uuid, SyncStatus.SYNCED)
                        showSnackBar("日程 ${schedule.title} 已上传")
                    } else if (result.isFailure) {
                        repository.updateScheduleSyncStatus(uuid, SyncStatus.PENDING)
                        showSnackBar("日程 ${schedule.title} 上传失败：${result.exceptionOrNull()?.message}")
                    }
                }
            }
            if (date.value.toEpochDays() == currentDate.toEpochDays()) {
                schedules.add(0, schedule.copy(uuid = uuid))
            }
        }

        reset()
    }

    fun reset() {
        id.intValue = -1
        uuid.value = ""
        userName.value = ""
        title.value = ""
        content.value = ""
        date.value = Clock.System.todayIn(TimeZone.currentSystemDefault())
        repeatMode.value = RepeatMode.NONE
        location.value = "未设定"
        finished.value = false
    }

    fun addScheduleFromSseServer(
        scheduleEntity: ScheduleEntity,
        currentDate: LocalDate
    ) {
        val updated = syncManager.handleSseSchedule(scheduleEntity, currentDate)
        if (updated) {
            val schedule = entityToSchedule(scheduleEntity)
            if (schedule.date == currentDate.toEpochDays()) {
                val existingIndex = schedules.indexOfFirst { it.uuid == schedule.uuid }
                if (existingIndex >= 0) {
                    schedules[existingIndex] = schedule
                } else {
                    schedules.add(0, schedule)
                }
            }
        }
    }

    fun loadScheduleByUUID(uuid: String): Schedule? {
        return repository.getScheduleByUuid(uuid)
    }

    fun loadSchedules(userName: String, date: MutableState<LocalDate>) {
        schedules.clear()
        schedules.addAll(loadTodaySchedules(userName, date))
    }

    private fun loadTodaySchedules(userName: String, date: MutableState<LocalDate>): List<Schedule> {
        val userSchedule = repository.getAllSchedulesByUsername(userName)
        val todaySchedules = userSchedule.filter { it.date == date.value.toEpochDays() }
        val repeatSchedule = userSchedule.filter { it.date != date.value.toEpochDays() }
            .filter { it.repeatMode != RepeatMode.NONE.toString() }
            .filter { schedule ->
                val scheduleDate = LocalDate.fromEpochDays(schedule.date.toInt())
                val today = date.value
                when (RepeatMode.valueOf(schedule.repeatMode)) {
                    RepeatMode.DAILY -> true
                    RepeatMode.WEEKLY -> scheduleDate.dayOfWeek == today.dayOfWeek
                    RepeatMode.MONTHLY -> scheduleDate.day == today.day
                    RepeatMode.YEARLY -> scheduleDate.month.number == today.month.number &&
                            scheduleDate.day == today.day
                    else -> false
                }
            }
        return (todaySchedules + repeatSchedule).filter { it.sync_status != SyncStatus.DELETED_PENDING.toString() }
            .sortedWith(
                compareBy<Schedule> { it.sequence }.thenByDescending { it.timestamp }
            )
    }

    fun deleteSchedule(
        uuid: String,
        userName: String,
        showSnackBar: (String) -> Unit
    ) {
        if (userName != "") {
            viewModelScope.launch {
                val result = syncManager.syncDeleteSchedule(uuid)
                if (result.isSuccess) {
                    repository.deleteSchedule(uuid)
                    showSnackBar("已同步删除云端日程")
                } else {
                    repository.updateScheduleSyncStatus(uuid, SyncStatus.DELETED_PENDING)
                    showSnackBar("云端日程删除失败：${result.exceptionOrNull()?.message}")
                }
            }
        }
        schedules.removeIf { it.uuid == uuid }
    }

    fun deleteSchedules(userName: String, showSnackBar: (String) -> Unit) {
        if (userName != "") {
            viewModelScope.launch {
                val result = syncManager.syncDeleteSchedules(schedulesToDelete)
                if (result.isSuccess) {
                    val successCount = result.getOrThrow()
                    val failureCount = schedulesToDelete.size - successCount
                    val message: String = if (failureCount == 0) {
                        "日程同步删除成功${successCount}条"
                    } else {
                        "日程同步删除成功${successCount}条，失败${failureCount}条\n" +
                                "原因：待删除日程中包含离线日程，此类日程不存在于云端"
                    }
                    showSnackBar(message)
                    deleteLocalSchedules()
                } else {
                    schedulesToDelete.forEach { scheduleUuid ->
                        repository.updateScheduleSyncStatus(scheduleUuid, SyncStatus.DELETED_PENDING)
                        schedules.removeIf { it.uuid == scheduleUuid}
                    }
                    showSnackBar("云端日程删除失败：${result.exceptionOrNull()?.message}")
                }
            }
        } else {
            deleteLocalSchedules()
        }
    }

    fun deleteSchedulesFromSSEServer(uuids: List<String>) {
        syncManager.handleSseDelete(uuids)
        uuids.forEach {
            schedules.removeIf { schedule -> schedule.uuid == it }
        }
    }

    fun finishSchedule(
        schedule: Schedule,
        showSnackBar: (String) -> Unit
    ) {
        updateSchedule(
            schedule = schedule.copy(
                finished = schedule.finished.toBoolean().not().toString(),
                timestamp = getTimestamp()
            ),
            showSnackBar = showSnackBar
        )
    }

    @Suppress("SuspiciousIndentation")
    private fun updateSchedule(
        schedule: Schedule,
        currentDate: LocalDate = LocalDate.fromEpochDays(schedule.date.toInt()),
        showSnackBar: (String) -> Unit
    ) {
        val index = schedules.indexOfFirst { it.uuid == schedule.uuid }
        if (schedule.username != "") {
            viewModelScope.launch {
                val result = syncManager.syncUpdateSchedule(scheduleToEntity(schedule))
                if (result.isSuccess) {
                    repository.updateScheduleSyncStatus(schedule.uuid, SyncStatus.SYNCED)
                    showSnackBar("云端更新成功")
                } else {
                    repository.updateScheduleSyncStatus(schedule.uuid, SyncStatus.FAILED)
                    showSnackBar("云端更新失败：${result.exceptionOrNull()?.message}")
                }
            }
        }
        repository.updateSchedule(schedule)
        if (schedule.date == currentDate.toEpochDays()) {
            //更新当前日期的日程列表
            schedules.set(index = index, element = schedule)
        } else {
            schedules.removeIf { schedule.uuid == it.uuid }
        }
    }

    private fun updateSchedules(
        schedules: List<Schedule>,
        showSnackBar: (String) -> Unit
    ) {
        if (schedules.isEmpty()) {
            return
        }
        if (schedules[0].username != "") {
            viewModelScope.launch {
                val result = syncManager.syncUpdateSchedules(schedules.map { scheduleToEntity(it) })
                if (result.isSuccess) {
                    schedules.forEach { schedule ->
                        repository.updateScheduleSyncStatus(schedule.uuid, SyncStatus.SYNCED)
                    }
                    showSnackBar("云端批量更新成功")
                } else {
                    schedules.forEach { schedule ->
                        repository.updateScheduleSyncStatus(schedule.uuid, SyncStatus.SYNCED)
                    }
                    showSnackBar("云端批量更新失败：${result.exceptionOrNull()?.message}")
                }
            }
        }
    }

    // SSE更新逻辑已移至SyncManager.handleSseSchedule


    fun reorderSchedules(
        showSnackBar: (String) -> Unit
    ) {
        //判断是否需要更新
        var needUpdate = false
        for (i in schedules.indices) {
            if (schedules[i].sequence != i.toLong()) {
                needUpdate = true
                break
            }
        }
        if (needUpdate) {
            val updatedSchedules = schedules.mapIndexed{ index, schedule ->
                schedule.copy(sequence = index.toLong()).copy(timestamp = getTimestamp())
            }
            updateSchedules(updatedSchedules, showSnackBar)
            repository.updateSchedules(updatedSchedules)
            //更新视图
            for (i in schedules.indices) {
                schedules[i] = updatedSchedules[i]
            }
        } else {
            return
        }
    }

    /**
     * 增量同步日程
     * @param userName 当前用户名
     * @param showSnackBar 显示提示信息
     * @param currentDate 当前日期
     * 通过对比本地和云端的时间戳，进行增量同步日程
     */
    fun syncDataIncrementally(
        userName: String,
        showSnackBar: (String) -> Unit,
        currentDate: LocalDate
    ) {
        if (userName.isBlank())
            return

        viewModelScope.launch {
            try {
                val result = syncManager.syncDataIncrementally(userName, currentDate)
                if (result.isSuccess) {
                    val successCount = result.getOrThrow()
                    // 重新加载当前日期的日程，确保视图同步
                    loadSchedules(userName, mutableStateOf(currentDate))
                    showSnackBar("${successCount}条日程已同步")
                } else {
                    showSnackBar("日程同步失败：${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                showSnackBar("日程同步失败：${e.message}")
            }
        }
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
            timestamp = schedule.timestamp,
            device = schedule.device
        )
    }

    private fun entityToSchedule(scheduleEntity: ScheduleEntity): Schedule {
        return Schedule (
            id = 0,
            uuid = scheduleEntity.uuid,
            username = scheduleEntity.userName,
            title = scheduleEntity.title,
            content = scheduleEntity.content,
            date = scheduleEntity.date,
            repeatMode = scheduleEntity.repeatMode.toString(),
            location = scheduleEntity.location,
            sequence = scheduleEntity.sequence.toLong(),
            finished = scheduleEntity.finished.toString(),
            timestamp = scheduleEntity.timestamp,
            device = scheduleEntity.device,
            sync_status = SyncStatus.SYNCED.toString()
        )
    }

    private fun deleteLocalSchedules() {
        schedulesToDelete.forEach {
            repository.deleteSchedule(it)
            schedules.removeIf { schedule -> schedule.uuid == it }
        }
        schedulesToDelete.clear()
    }
}