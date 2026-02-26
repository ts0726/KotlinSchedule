package kmp.project.schedule.di

import kmp.project.schedule.viewModel.AuthViewModel
import kmp.project.schedule.viewModel.HomePageStateViewModel
import kmp.project.schedule.viewModel.ScheduleViewModel
import org.koin.dsl.module

val viewModelModule = module {
    factory {
        ScheduleViewModel(repository = get(), syncManager = get())
    }

    factory {
        AuthViewModel(settingsManager = get())
    }

    factory {
        HomePageStateViewModel()
    }
}