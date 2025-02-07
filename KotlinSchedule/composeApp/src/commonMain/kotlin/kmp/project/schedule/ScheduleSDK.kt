package kmp.project.schedule

import kmp.project.schedule.database.Schedule
import kmp.project.schedule.db.Database
import kmp.project.schedule.db.DatabaseDriverFactory
import kmp.project.schedule.db.SettingsFactory
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class ScheduleSDK(
    databaseDriverFactory: DatabaseDriverFactory,
    settingsFactory: SettingsFactory
) {
    private val database = Database(databaseDriverFactory)
    private val settings = settingsFactory.createSettings()

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
        return database.getAllSchedulesbyDate(date)
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

    fun updateSchedules(schedules: List<Schedule>) {
        database.updateSchedules(schedules)
    }

    fun getCountOfSchedulesByDate(date: Int): Int {
        return database.countSchedulesByDate(date).toInt()
    }

    fun addSetting(key: String, value: Any) {
        when (value) {
            is String -> settings.putString(key, value)
            is Int -> settings.putInt(key, value)
            is Boolean -> settings.putBoolean(key, value)
            is Float -> settings.putFloat(key, value)
            is Long -> settings.putLong(key, value)
            else -> throw IllegalArgumentException("Unsupported types: ${value::class}")
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> getSetting(key: String, classType: Class<T>): T? {
        println("getSetting")
        return when (classType) {
            String::class.java -> settings.getStringOrNull(key) as T
            Int::class.java -> settings.getIntOrNull(key) as T
            Boolean::class.java -> settings.getBooleanOrNull(key) as T
            Float::class.java -> settings.getFloatOrNull(key) as T
            Long::class.java -> settings.getLongOrNull(key) as T
            else -> null
        }
    }

    fun removeSetting(key: String) {
        settings.remove(key)
    }

    companion object {
        @Volatile
        private var INSTANCE: ScheduleSDK? = null

        fun getInstance(
            databaseDriverFactory: DatabaseDriverFactory,
            settingsFactory: SettingsFactory
        ): ScheduleSDK {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ScheduleSDK(databaseDriverFactory, settingsFactory).also { INSTANCE = it }
            }
        }
    }
}