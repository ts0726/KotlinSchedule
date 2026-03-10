package kmp.project.schedule.db

import kmp.project.schedule.database.Schedule
import kmp.project.schedule.database.ScheduleDatabase
import kmp.project.schedule.domain.sync.SyncStatus

internal class Database (databaseDriverFactory: DatabaseDriverFactory){
    private val database = ScheduleDatabase(databaseDriverFactory.createDriver())
    private val dbQuery = database.scheduleQueries

    internal fun getAllSchedules(): List<Schedule> {
        return dbQuery.selectAllSchedules(::mapResultToList).executeAsList()
    }

    internal fun getAllSchedulesByUsername(username: String): List<Schedule> {
        return dbQuery.selectSchedulesByUsername(username).executeAsList()
    }

    internal fun getAllSchedulesbyDate(username: String, date: Long): List<Schedule> {
        return dbQuery.selectSchedulesByDate(username, date).executeAsList()
    }

    internal fun getSchedulesByDateRange(username: String, startDate: Long, endDate: Long): List<Schedule> {
        return dbQuery.selectSchedulesByDateRange(username, startDate, endDate).executeAsList()
    }

    internal fun createSchedule(schedule: Schedule) {
        dbQuery.transaction {
            dbQuery.insertSchedule(
                title = schedule.title,
                uuid = schedule.uuid,
                username = schedule.username,
                content = schedule.content,
                date = schedule.date,
                repeatMode = schedule.repeatMode,
                location = schedule.location,
                sequence = schedule.sequence,
                finished = schedule.finished,
                timestamp = schedule.timestamp,
                device = schedule.device
            )
        }
    }

    internal fun deleteSchedule(uuid: String) {
        dbQuery.transaction {
            dbQuery.deleteSchedule(uuid)
        }
    }

    internal fun updateSchedule(schedule: Schedule) {
        dbQuery.transaction {
            dbQuery.updateSchedule(
                title = schedule.title,
                content = schedule.content,
                date = schedule.date,
                repeatMode = schedule.repeatMode,
                location = schedule.location,
                uuid = schedule.uuid,
                sequence = schedule.sequence,
                device = schedule.device,
                finished = schedule.finished,
                timestamp = schedule.timestamp
            )
        }
    }

    internal fun updateSchedules(schedules: List<Schedule>) {
        dbQuery.transaction {
            schedules.forEach { schedule ->
                dbQuery.updateSchedule(
                    title = schedule.title,
                    content = schedule.content,
                    date = schedule.date,
                    repeatMode = schedule.repeatMode,
                    location = schedule.location,
                    uuid = schedule.uuid,
                    sequence = schedule.sequence,
                    device = schedule.device,
                    finished = schedule.finished,
                    timestamp = schedule.timestamp
                )
            }
        }
    }

    internal fun updateScheduleSyncStatus(uuid: String, syncStatus: SyncStatus) {
        dbQuery.transaction {
            dbQuery.updateScheduleSyncStatus(
                sync_status = syncStatus.toString(),
                uuid = uuid
            )
        }
    }

    private fun mapResultToList(
        id: Long,
        uuid: String,
        username: String,
        title: String,
        content: String?,
        date: Long,
        repeatMode: String,
        location: String?,
        sequence: Long,
        finished: String,
        timestamp: Long,
        device: String,
        syncStatus: String,
    ): Schedule {
        return Schedule(
            id = id,
            uuid = uuid,
            username = username,
            title = title,
            content = content,
            date = date,
            repeatMode = repeatMode,
            location = location,
            sequence = sequence,
            finished = finished,
            timestamp = timestamp,
            device = device,
            sync_status = syncStatus
        )
    }

}