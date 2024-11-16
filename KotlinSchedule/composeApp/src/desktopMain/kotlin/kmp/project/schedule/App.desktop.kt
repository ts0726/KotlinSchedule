package kmp.project.schedule

import androidx.compose.runtime.Composable
import kmp.project.schedule.db.DatabaseDriverFactory

@Composable
actual fun getScheduleSDK(): ScheduleSDK {
    return ScheduleSDK(
        databaseDriverFactory = DatabaseDriverFactory(),
    )
}