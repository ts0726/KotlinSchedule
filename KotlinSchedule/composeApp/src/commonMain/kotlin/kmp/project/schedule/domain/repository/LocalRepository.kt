package kmp.project.schedule.domain.repository

import kmp.project.schedule.database.Schedule
import kmp.project.schedule.domain.sync.SyncStatus

interface LocalRepository {
    fun insertSchedule(
        username: String,
        title: String,
        content: String?,
        date: Long,
        repeatMode: String,
        location: String?,
        sequence: Int,
        finished: String,
        device: String,
        syncStatus: String
    ): String

    fun insertSchedule(schedule: Schedule)

    fun getAllSchedulesByUsername(username: String): List<Schedule>

    fun getScheduleByDate(username: String, date: Long): List<Schedule>

    fun getScheduleByUuid(uuid: String): Schedule?

    fun deleteSchedule(uuid: String)

    fun updateSchedule(schedule: Schedule)

    fun updateSchedules(schedules: List<Schedule>)

    fun updateScheduleSyncStatus(uuid: String, syncStatus: SyncStatus)
}