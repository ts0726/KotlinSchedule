package kmp.project.schedule

import kmp.project.schedule.database.Schedule
import kmp.project.schedule.db.Database
import kmp.project.schedule.db.DatabaseDriverFactory

class ScheduleSDK(databaseDriverFactory: DatabaseDriverFactory) {
    private val database = Database(databaseDriverFactory)

    @Throws(Exception::class)
    fun insertSchedule(
        title: String,
        content: String?,
        date: Long,
        repeatMode: Int,
        location: String?
    ) {
        database.createSchedule(
            Schedule(
                id = 0,
                title = title,
                content = content,
                date = date,
                repeatMode = repeatMode.toLong(),
                location = location)
        )
    }

    fun getScheduleList(): List<Schedule> {
        return database.getAllSchedules()
    }
}