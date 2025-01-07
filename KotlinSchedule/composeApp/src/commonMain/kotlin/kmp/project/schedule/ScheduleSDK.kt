package kmp.project.schedule

import kmp.project.schedule.database.Schedule
import kmp.project.schedule.db.Database
import kmp.project.schedule.db.DatabaseDriverFactory
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class ScheduleSDK(databaseDriverFactory: DatabaseDriverFactory) {
    private val database = Database(databaseDriverFactory)

    @OptIn(ExperimentalUuidApi::class)
    @Throws(Exception::class)
    fun insertSchedule(
        title: String,
        content: String?,
        date: Long,
        repeatMode: Int,
        location: String?,
        sequence: Int
    ): String {
        val uuid = Uuid.random().toString()
        database.createSchedule(
            Schedule(
                id = 0,
                uuid = uuid,
                title = title,
                content = content,
                date = date,
                repeatMode = repeatMode.toLong(),
                location = location,
                sequence = sequence.toLong())
        )
        return uuid
    }

//    fun getAllScheduleList(): List<Schedule> {
//        return database.getAllSchedules()
//    }

    fun getScheduleByDate(date: Long): List<Schedule> {
//        return database.getAllSchedules().filter { it.date == date }
        return database.getAllSchedules().filter { it.date == date }.sortedByDescending { it.id }
    }

    fun getScheduleByUuid(uuid: String): Schedule? {
        return database.getAllSchedules().firstOrNull { it.uuid == uuid }
    }

    fun deleteSchedule(uuid: String) {
        database.deleteSchedule(uuid)
    }

    fun updateSchedule(schedule: Schedule) {
        database.updateSchedule(schedule)
    }

    fun getCountOfSchedulesByDate(date: Int): Int {
        return database.countSchedulesByDate(date).toInt()
    }

    companion object {
        @Volatile
        private var INSTANCE: ScheduleSDK? = null

        fun getInstance(databaseDriverFactory: DatabaseDriverFactory): ScheduleSDK {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ScheduleSDK(databaseDriverFactory).also { INSTANCE = it }
            }
        }
    }
}