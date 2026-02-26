package kmp.project.schedule.domain.sync

import kmp.project.schedule.entity.ScheduleEntity
import kotlinx.datetime.LocalDate

interface SyncManager {
    suspend fun syncAddSchedule(scheduleEntity: ScheduleEntity): Result<Unit>
    suspend fun syncUpdateSchedule(scheduleEntity: ScheduleEntity): Result<Unit>
    suspend fun syncDeleteSchedule(uuid: String): Result<Unit>
    suspend fun syncDeleteSchedules(uuids: List<String>): Result<Int> // 返回成功删除的数量
    suspend fun syncUpdateSchedules(scheduleEntities: List<ScheduleEntity>): Result<Unit>
    suspend fun syncDataIncrementally(
        userName: String,
        currentDate: LocalDate
    ): Result<Int>
    fun handleSseSchedule(scheduleEntity: ScheduleEntity, currentDate: LocalDate): Boolean
    fun handleSseDelete(uuids: List<String>): Boolean
}