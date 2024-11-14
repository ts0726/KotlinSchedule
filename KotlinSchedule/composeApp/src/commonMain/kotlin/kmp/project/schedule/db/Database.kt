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
                content = schedule.content,
                date = schedule.date,
                repeatMode = schedule.repeatMode,
                location = schedule.location
            )
        }
    }

    private fun mapResultToList(
        id: Long,
        title: String,
        content: String?,
        date: Long,
        repeatMode: Long,
        location: String?
    ): Schedule {
        return Schedule(
            id = id,
            title = title,
            content = content,
            date = date,
            repeatMode = repeatMode,
            location = location
        )
    }

}