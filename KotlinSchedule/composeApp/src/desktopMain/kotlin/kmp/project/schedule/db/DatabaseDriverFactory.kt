package kmp.project.schedule.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
//import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import kmp.project.schedule.database.ScheduleDatabase
import okio.FileSystem
import okio.Path.Companion.toPath

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        val databasePath = "./scheduleDatabase.db"
        val driver: SqlDriver = JdbcSqliteDriver("jdbc:sqlite:$databasePath")

        // 使用okio库检查数据库文件是否存在
        val dbPath = databasePath.toPath()
        if (!FileSystem.SYSTEM.exists(dbPath)) {
            // 如果文件不存在，则创建数据库架构
            ScheduleDatabase.Schema.create(driver)
        }

        return driver
    }
}