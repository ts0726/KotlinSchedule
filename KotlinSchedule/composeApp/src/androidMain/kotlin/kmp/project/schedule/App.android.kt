package kmp.project.schedule

import androidx.compose.runtime.Composable
import kmp.project.schedule.sdk.ScheduleSDK
import org.koin.compose.koinInject

@Composable
actual fun getScheduleSDK(): ScheduleSDK {
    return koinInject()
}