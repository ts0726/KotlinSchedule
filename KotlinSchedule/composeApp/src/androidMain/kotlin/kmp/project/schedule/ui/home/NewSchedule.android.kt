package kmp.project.schedule.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import kmp.project.schedule.model.ScheduleViewModel

@Composable
actual fun NewScheduleBackHandler(
    viewModel: ScheduleViewModel,
    onBack: () -> Unit
) {
    BackHandler(enabled = true) {
        viewModel.reset()
        onBack()
    }
}