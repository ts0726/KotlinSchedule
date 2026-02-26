package kmp.project.schedule.di

import kmp.project.schedule.db.DatabaseDriverFactory
import kmp.project.schedule.domain.repository.LocalRepositoryImpl
import org.koin.dsl.module

val repositoryModule = module {
    single<LocalRepositoryImpl> {
        LocalRepositoryImpl(
            databaseDriverFactory = DatabaseDriverFactory()
        )
    }
}