package kmp.project.schedule.di

import kmp.project.schedule.db.SettingsFactory
import kmp.project.schedule.util.SettingsManager
import org.koin.dsl.module

val settingsModule = module {
    single<SettingsManager> {
        SettingsManager(
            settings = SettingsFactory().createSettings()
        )
    }
}