package kmp.project.schedule.db

import kmp.project.schedule.database.Schedule
import kmp.project.schedule.database.ScheduleDatabase

internal class Database (databaseDriverFactory: DatabaseDriverFactory){
    private val database = ScheduleDatabase(databaseDriverFactory.createDriver())
    private val dbQuery = database.scheduleQueries

    internal fun getAllSchedules(): List<Schedule> {
        return dbQuery.selectAllSchedules(::mapResultToList).executeAsList()
    }

    internal fun createSchedule(schedule: Schedule) {
        dbQuery.transaction {
            dbQuery.insertSchedule(
                title = schedule.title,
                uuid = schedule.uuid,
                content = schedule.content,
                date = schedule.date,
                repeatMode = schedule.repeatMode,
                location = schedule.location
            )
        }
    }

    internal fun deleteSchedule(uuid: String) {
        dbQuery.transaction {
            dbQuery.deleteSchedule(uuid)
        }
    }

    private fun mapResultToList(
        id: Long,
        uuid: String,
        title: String,
        content: String?,
        date: Long,
        repeatMode: Long,
        location: String?
    ): Schedule {
        return Schedule(
            id = id,
            uuid = uuid,
            title = title,
            content = content,
            date = date,
            repeatMode = repeatMode,
            location = location
        )
    }

}