package kmp.project.schedule


import kmp.project.schedule.db.DatabaseDriverFactory
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {
    single<ScheduleSDK> {
        ScheduleSDK (
            databaseDriverFactory = DatabaseDriverFactory(
                androidContext()
            )
        )
    }
}