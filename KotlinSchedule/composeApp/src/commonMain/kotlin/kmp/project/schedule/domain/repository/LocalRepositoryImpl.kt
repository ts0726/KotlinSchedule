package kmp.project.schedule.domain.repository

import kmp.project.schedule.database.Schedule
import kmp.project.schedule.db.Database
import kmp.project.schedule.db.DatabaseDriverFactory
import kmp.project.schedule.domain.sync.SyncStatus
import kmp.project.schedule.util.timeUtil.getTimestamp
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class LocalRepositoryImpl(
    databaseDriverFactory: DatabaseDriverFactory
): LocalRepository {
    private val database = Database(databaseDriverFactory)

    @OptIn(ExperimentalUuidApi::class)
    @Throws(Exception::class)
    override fun insertSchedule(
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
    ): String {
        val uuid = Uuid.random().toString()
        database.createSchedule(
            Schedule(
                id = 0,
                uuid = uuid,
                username = username,
                title = title,
                content = content,
                date = date,
                repeatMode = repeatMode,
                location = location,
                finished = finished,
                sequence = sequence.toLong(),
                timestamp = getTimestamp(),
                device = device,
                sync_status = syncStatus
            ),
        )
        return uuid
    }

    override fun insertSchedule(schedule: Schedule) {
        database.createSchedule(schedule)
    }

    override fun getAllSchedulesByUsername(username: String): List<Schedule> {
        return database.getAllSchedulesByUsername(username)
    }

    override fun getScheduleByDate(username: String, date: Long): List<Schedule> {
//        return database.getAllSchedules().filter { it.date == date }
        return database.getAllSchedulesbyDate(username, date)
    }

    override fun getScheduleByUuid(uuid: String): Schedule? {
        return database.getAllSchedules().firstOrNull { it.uuid == uuid }
    }

    override fun getSchedulesByDateRange(
        username: String,
        startDate: Long,
        endDate: Long
    ): List<Schedule> {
        return database.getSchedulesByDateRange(username, startDate, endDate)
    }

    override fun deleteSchedule(uuid: String) {
        database.deleteSchedule(uuid)
    }

    override fun updateSchedule(schedule: Schedule) {
        database.updateSchedule(schedule)
    }

    override fun updateSchedules(schedules: List<Schedule>) {
        database.updateSchedules(schedules)
    }

    override fun updateScheduleSyncStatus(
        uuid: String,
        syncStatus: SyncStatus
    ) {
        database.updateScheduleSyncStatus(uuid, syncStatus)
    }
}