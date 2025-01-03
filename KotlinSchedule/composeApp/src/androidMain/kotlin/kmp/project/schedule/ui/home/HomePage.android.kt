package kmp.project.schedule.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState

@Composable
actual fun BackHandler(
    showDeleteTopDocker: MutableState<Boolean>
) {
    println("backHandler: " + showDeleteTopDocker.value)
    BackHandler(enabled = showDeleteTopDocker.value) {
        if (showDeleteTopDocker.value) {
            showDeleteTopDocker.value = !showDeleteTopDocker.value
        }
        println("showDeleteTopDocker: " + showDeleteTopDocker.value)
    }
}