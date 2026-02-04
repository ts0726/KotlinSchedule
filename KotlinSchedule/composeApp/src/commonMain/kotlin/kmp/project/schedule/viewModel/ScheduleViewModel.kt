package kmp.project.schedule.viewModel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kmp.project.schedule.database.Schedule
import kmp.project.schedule.entity.ScheduleEntity
import kmp.project.schedule.net.ApiResult
import kmp.project.schedule.net.scheduleApi
import kmp.project.schedule.sdk.ScheduleSDK
import kmp.project.schedule.util.DeviceUtil
import kmp.project.schedule.util.timeUtil.getTimestamp
import kotlinx.coroutines.launch
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
    val device = DeviceUtil.getDeviceName()
    var schedules = mutableStateListOf<Schedule>()
    val schedulesToDelete = mutableStateListOf<String>()

    fun onSave(
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
            timestamp = 0,
            device = device
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
                finished = finished.value.toString(),
                device = device
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

    fun addScheduleFromSseServer(
        scheduleEntity: ScheduleEntity,
        currentDate: LocalDate
    ) {
        val schedule = entityToSchedule(scheduleEntity)
        if (loadScheduleByUUID(schedule.uuid)?.timestamp == schedule.timestamp) {
            return
        } else if (loadScheduleByUUID(schedule.uuid) != null) {
            updateScheduleFromSseServer(schedule, currentDate)
            return
        }
        sdk.insertSchedule(schedule)
        println("currentDate: $currentDate, schedule.date: ${LocalDate.fromEpochDays(schedule.date.toInt())}")
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
        schedules.sortWith(
            compareBy<Schedule> { it.sequence }
                .thenByDescending { it.timestamp }
        )
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

    fun deleteSchedulesFromSSEServer(uuids: List<String>) {
        uuids.forEach {
            sdk.deleteSchedule(it)
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
                val result = scheduleApi.updateSchedule(scheduleToEntity(schedule))
                if (result is ApiResult.Success) {
                    showSnackBar("云端更新成功")
                } else if (result is ApiResult.Error) {
                    showSnackBar("云端更新失败：${result.message}")
                }
            }
        }
        sdk.updateSchedule(schedule)
        if (schedule.date.toInt() == currentDate.toEpochDays()) {
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
                val result = scheduleApi.updateSchedules(schedules.map { scheduleToEntity(it) })
                if (result is ApiResult.Success) {
                    showSnackBar("云端批量更新成功")
                } else if (result is ApiResult.Error) {
                    showSnackBar("云端批量更新失败：${result.message}")
                }
            }
        }
    }

    private fun updateScheduleFromSseServer(
        schedule: Schedule,
        currentDate: LocalDate
    ){
        val index = schedules.indexOfFirst { it.uuid == schedule.uuid }
        sdk.updateSchedule(schedule)
        if (schedule.date.toInt() == currentDate.toEpochDays()) {
            schedules.set(index = index, element = schedule)
        } else {
            schedules.removeIf { schedule.uuid == it.uuid }
        }
        schedules.sortWith(
            compareBy<Schedule> { it.sequence }
                .thenByDescending { it.timestamp }
        )
    }

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
            sdk.updateSchedules(updatedSchedules)
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
     * 1. 获取云端所有日程的uuid和时间戳
     * 2. 获取本地所有日程的uuid和时间戳
     * 3. 对比uuid，找出新增、更新和删除的日程
     * 4. 对于新增和更新的日程，获取其详细信息并更新本地数据库
     * 5. 对于删除的日程，从本地数据库中删除
     * 6. 更新视图中的日程列表
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
                val syncResult = scheduleApi.syncSchedules()
                if (syncResult is ApiResult.Success) {
                    val syncList = syncResult.data
                    val localSchedules = sdk.getAllSchedulesByUsername(userName)
                    //获取本地和云端的uuid列表
                    val localUuids = localSchedules.map { it.uuid }
                    val syncUuids = syncList.map { it.uuids }
                    //获取新增日程
                    val schedulesToAdd = syncList.filter { !localUuids.contains(it.uuids) }
                    //根据时间戳获得更新日程
                    val schedulesToUpdate = syncList
                        .filter { localUuids.contains(it.uuids) }
                        .filter { syncEntity ->
                            val localSchedule = localSchedules.find { it.uuid == syncEntity.uuids }
                            localSchedule != null && syncEntity.timestamp > localSchedule.timestamp
                        }
                    //获取删除日程
                    val schedulesToDelete = localSchedules.filter { !syncUuids.contains(it.uuid) }
                    //执行增量更新
                    val changedUuids = (schedulesToAdd + schedulesToUpdate).map { it.uuids }
                    val scheduleDetailsResult = scheduleApi.getSchedulesByUuids(changedUuids)
                    if (changedUuids.isNotEmpty() && scheduleDetailsResult is ApiResult.Success) {
                        val scheduleEntities = scheduleDetailsResult.data
                        scheduleEntities.forEach { entity ->
                            val schedule = entityToSchedule(entity)
                            if (loadScheduleByUUID(schedule.uuid) != null) {    //如果存在则更新
                                sdk.updateSchedule(schedule)
                                val index = schedules.indexOfFirst { it.uuid == schedule.uuid }
                                //更新视图
                                if (schedule.date.toInt() == currentDate.toEpochDays()) {
                                    //更新当前日期的日程列表
                                    schedules.set(index = index, element = schedule)
                                } else {
                                    schedules.removeIf { schedule.uuid == it.uuid }
                                }
                            } else {    //不存在则插入
                                sdk.insertSchedule(schedule)
                                if (schedule.date.toInt() == currentDate.toEpochDays()) {
                                    schedules.add(0, schedule)
                                }
                            }
                        }
                    }
                    schedulesToDelete.forEach {schedule ->
                        sdk.deleteSchedule(schedule.uuid)
                        schedules.remove(schedule)
                    }
                    showSnackBar("日程同步完成：新增${schedulesToAdd.size}条，更新${schedulesToUpdate.size}条，删除${schedulesToDelete.size}条")
                }else if (syncResult is ApiResult.Error) {
                    showSnackBar("日程同步失败：${syncResult.message}")
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
            device = scheduleEntity.device
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