package kmp.project.schedule.di

import kmp.project.schedule.db.SettingsFactory
import kmp.project.schedule.domain.repository.LocalRepositoryImpl
import kmp.project.schedule.domain.sync.SyncManager
import kmp.project.schedule.domain.sync.SyncManagerImpl
import kmp.project.schedule.net.scheduleApi
import kmp.project.schedule.util.SettingsManager
import org.koin.dsl.module

val appModule = module {
    single { get<SettingsFactory>().createSettings() }

    single { SettingsManager(get()) }

    single<SyncManager> {
        SyncManagerImpl(
            localRepository = get<LocalRepositoryImpl>(),
            scheduleApi = scheduleApi
        )
    }
}