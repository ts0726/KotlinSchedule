package kmp.project.schedule

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kmp.project.schedule.db.DatabaseDriverFactory
import kmp.project.schedule.db.SettingsFactory

@Composable
actual fun getScheduleSDK(): ScheduleSDK {
    val databaseDriverFactory = DatabaseDriverFactory()
    val settingsFactory = SettingsFactory()
    return remember { ScheduleSDK.getInstance(databaseDriverFactory, settingsFactory) }
}