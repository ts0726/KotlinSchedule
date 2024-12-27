package kmp.project.schedule.db

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import kmp.project.schedule.database.ScheduleDatabase

actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(ScheduleDatabase.Schema, context, "schedule.db")
    }
}