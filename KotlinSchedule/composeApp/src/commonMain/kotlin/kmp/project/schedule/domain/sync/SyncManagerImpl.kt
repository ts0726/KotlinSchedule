package kmp.project.schedule.domain.sync

import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.mutableIntStateOf
import kmp.project.schedule.database.Schedule
import kmp.project.schedule.domain.repository.LocalRepository
import kmp.project.schedule.entity.RepeatMode
import kmp.project.schedule.entity.ScheduleEntity
import kmp.project.schedule.net.ApiResult
import kmp.project.schedule.net.ScheduleApi
import kotlinx.datetime.LocalDate

class SyncManagerImpl(
    private val localRepository: LocalRepository,
    private val scheduleApi: ScheduleApi
) : SyncManager {

    override suspend fun syncAddSchedule(scheduleEntity: ScheduleEntity): Result<Unit> {
        return when (val result = scheduleApi.addSchedule(scheduleEntity)) {
            is ApiResult.Success -> Result.success(Unit)
            is ApiResult.Error -> Result.failure(Exception(result.message))
        }
    }

    override suspend fun syncUpdateSchedule(scheduleEntity: ScheduleEntity): Result<Unit> {
        return when (val result = scheduleApi.updateSchedule(scheduleEntity)) {
            is ApiResult.Success -> Result.success(Unit)
            is ApiResult.Error -> Result.failure(Exception(result.message))
        }
    }

    override suspend fun syncDeleteSchedule(uuid: String): Result<Unit> {
        return when (val result = scheduleApi.deleteSchedule(uuid)) {
            is ApiResult.Success -> Result.success(Unit)
            is ApiResult.Error -> Result.failure(Exception(result.message))
        }
    }

    override suspend fun syncDeleteSchedules(uuids: List<String>): Result<Int> {
        return when (val result = scheduleApi.deleteSchedules(uuids)) {
            is ApiResult.Success -> Result.success(result.data.success)
            is ApiResult.Error -> Result.failure(Exception(result.message))
        }
    }

    override suspend fun syncUpdateSchedules(scheduleEntities: List<ScheduleEntity>): Result<Unit> {
        return when (val result = scheduleApi.updateSchedules(scheduleEntities)) {
            is ApiResult.Success -> Result.success(Unit)
            is ApiResult.Error -> Result.failure(Exception(result.message))
        }
    }

    override suspend fun syncDataIncrementally(
        userName: String,
        currentDate: LocalDate
    ): Result<Int> {
        return try {
            val syncSuccessSchedulesCount = mutableIntStateOf(0)
            val localSchedules = localRepository.getAllSchedulesByUsername(userName)
            
            //先将本地待同步的日程上传到服务器，确保服务器端数据是最新的
            syncFromLocalToServer(localSchedules, syncSuccessSchedulesCount)

            val syncResult = scheduleApi.syncSchedules()
            when (syncResult) {
                is ApiResult.Success -> {
                    val syncList = syncResult.data

                    // 获取本地和云端的uuid列表
                    val localUuids = localSchedules.map { it.uuid }
                    val syncUuids = syncList.map { it.uuids }

                    // 获取新增日程
                    val schedulesToAdd = syncList.filter { !localUuids.contains(it.uuids) }
                    // 根据时间戳获得更新日程
                    val schedulesToUpdate = syncList
                        .filter { localUuids.contains(it.uuids) }
                        .filter { syncEntity ->
                            val localSchedule = localSchedules.find { it.uuid == syncEntity.uuids }
                            localSchedule != null && syncEntity.timestamp > localSchedule.timestamp
                        }
                    // 获取删除日程
                    val schedulesToDelete = localSchedules.filter { !syncUuids.contains(it.uuid) }

                    // 执行增量更新
                    val changedUuids = (schedulesToAdd + schedulesToUpdate).map { it.uuids }
                    if (changedUuids.isNotEmpty()) {
                        val scheduleDetailsResult = scheduleApi.getSchedulesByUuids(changedUuids)
                        if (scheduleDetailsResult is ApiResult.Success) {
                            val scheduleEntities = scheduleDetailsResult.data
                            scheduleEntities.forEach { entity ->
                                val schedule = entityToSchedule(entity)
                                if (localRepository.getScheduleByUuid(schedule.uuid) != null) {
                                    // 如果存在则更新
                                    localRepository.updateSchedule(schedule)
                                    localRepository.updateScheduleSyncStatus(schedule.uuid, SyncStatus.SYNCED)
                                    syncSuccessSchedulesCount.intValue++
                                } else {
                                    // 不存在则插入
                                    localRepository.insertSchedule(schedule)
                                    localRepository.updateScheduleSyncStatus(schedule.uuid, SyncStatus.SYNCED)
                                    syncSuccessSchedulesCount.intValue++
                                }
                            }
                        }
                    }

                    // 删除本地多余的日程
                    schedulesToDelete.forEach {
                        localRepository.deleteSchedule(it.uuid)
                        syncSuccessSchedulesCount.intValue++
                    }

                    Result.success(syncSuccessSchedulesCount.intValue)
                }

                is ApiResult.Error -> {
                    Result.failure(Exception(syncResult.message))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun handleSseSchedule(scheduleEntity: ScheduleEntity, currentDate: LocalDate): Boolean {
        val schedule = entityToSchedule(scheduleEntity)
        if (localRepository.getScheduleByUuid(schedule.uuid)?.timestamp == schedule.timestamp) {
            return false
        } else if (localRepository.getScheduleByUuid(schedule.uuid) != null) {
            // 更新现有日程
            localRepository.updateSchedule(schedule)
            localRepository.updateScheduleSyncStatus(schedule.uuid, SyncStatus.SYNCED)
            return true
        }
        // 插入新日程
        localRepository.insertSchedule(schedule)
        localRepository.updateScheduleSyncStatus(schedule.uuid, SyncStatus.SYNCED)
        return true
    }

    override fun handleSseDelete(uuids: List<String>): Boolean {
        uuids.forEach {
            localRepository.deleteSchedule(it)
        }
        return true
    }

    private suspend fun syncFromLocalToServer(
        localSchedules: List<Schedule>,
        syncSuccessSchedulesCount: MutableIntState
    ) {
        // 上传本地待同步的日程
        syncSchedulesByStatus(
            localSchedules = localSchedules,
            status = SyncStatus.PENDING.toString(),
            apiCall = { entities -> scheduleApi.addSchedules(entities) },
            onSuccess = { entities ->
                entities.forEach { entity ->
                    localRepository.updateScheduleSyncStatus(entity.uuid, SyncStatus.SYNCED)
                }
                syncSuccessSchedulesCount.intValue += entities.size
            },
            errorMessage = "上传同步失败："
        )

        // 上传更新失败的日程
        syncSchedulesByStatus(
            localSchedules = localSchedules,
            status = SyncStatus.FAILED.toString(),
            apiCall = { entities -> scheduleApi.updateSchedules(entities) },
            onSuccess = { entities ->
                entities.forEach { entity ->
                    localRepository.updateScheduleSyncStatus(entity.uuid, SyncStatus.SYNCED)
                }
                syncSuccessSchedulesCount.intValue += entities.size
            },
            errorMessage = "更新同步失败："
        )

        // 上传删除失败的日程
        syncSchedulesByStatus(
            localSchedules = localSchedules,
            status = SyncStatus.DELETED_PENDING.toString(),
            apiCall = { entities -> scheduleApi.deleteSchedules(entities.map { it.uuid }) },
            onSuccess = { entities ->
                syncSuccessSchedulesCount.intValue += entities.size
            },
            errorMessage = "删除同步失败："
        )
    }

    /**
     * 根据指定状态同步日程到服务器
     * @param localSchedules 本地日程列表
     * @param status 筛选的同步状态
     * @param apiCall API调用函数
     * @param onSuccess 成功回调
     * @param errorMessage 错误信息前缀
     */
    private suspend fun syncSchedulesByStatus(
        localSchedules: List<Schedule>,
        status: String,
        apiCall: suspend (List<ScheduleEntity>) -> ApiResult<*>,
        onSuccess: (List<ScheduleEntity>) -> Unit,
        errorMessage: String
    ) {
        val filteredSchedules = localSchedules.filter { it.sync_status == status }
        if (filteredSchedules.isNotEmpty()) {
            val scheduleEntities = filteredSchedules.map { scheduleToEntity(it) }
            when (val result = apiCall(scheduleEntities)) {
                is ApiResult.Success -> {
                    onSuccess(scheduleEntities)
                }
                is ApiResult.Error -> {
                    throw Exception(errorMessage + result.message)
                }
            }
        }
    }

    private fun entityToSchedule(scheduleEntity: ScheduleEntity): Schedule {
        return Schedule(
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
            sync_status = "SYNCED"
        )
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
}