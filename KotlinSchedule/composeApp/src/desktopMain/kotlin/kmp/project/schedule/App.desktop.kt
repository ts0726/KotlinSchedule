package kmp.project.schedule

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kmp.project.schedule.db.DatabaseDriverFactory

@Composable
actual fun getScheduleSDK(): ScheduleSDK {
    val databaseDriverFactory = DatabaseDriverFactory()
    return remember { ScheduleSDK.getInstance(databaseDriverFactory) }
}