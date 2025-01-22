package kmp.project.schedule.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable

@Composable
actual fun EditModeBackHandler(
    showDeleteTopDocker: Boolean,
    closeEditMode: () -> Unit
) {
    BackHandler(enabled = showDeleteTopDocker) {
        if (showDeleteTopDocker) {
            closeEditMode()
        }
    }
}