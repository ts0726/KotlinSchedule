package kmp.project.schedule.sdk

import kmp.project.schedule.database.Schedule
import kmp.project.schedule.db.Database
import kmp.project.schedule.db.DatabaseDriverFactory
import kmp.project.schedule.db.SettingsFactory
import kmp.project.schedule.util.timeUtil.getTimestamp
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
        username: String,
        title: String,
        content: String?,
        date: Long,
        repeatMode: String,
        location: String?,
        sequence: Int,
        finished: String,
        device: String
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
                device = device
            ),
        )
        return uuid
    }

    fun insertSchedule(schedule: Schedule) {
        database.createSchedule(schedule)
    }

//    fun getAllScheduleList(): List<Schedule> {
//        return database.getAllSchedules()
//    }

    fun getScheduleByDate(username: String, date: Long): List<Schedule> {
//        return database.getAllSchedules().filter { it.date == date }
        return database.getAllSchedulesbyDate(username, date)
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