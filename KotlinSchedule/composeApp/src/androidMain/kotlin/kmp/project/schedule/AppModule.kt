package kmp.project.schedule


import kmp.project.schedule.db.DatabaseDriverFactory
import kmp.project.schedule.db.SettingsFactory
import kmp.project.schedule.sdk.ScheduleSDK
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {
    single<ScheduleSDK> {
        ScheduleSDK (
            databaseDriverFactory = DatabaseDriverFactory(
                androidContext()
            ),
            settingsFactory = SettingsFactory(
                androidContext()
            )
        )
    }
}