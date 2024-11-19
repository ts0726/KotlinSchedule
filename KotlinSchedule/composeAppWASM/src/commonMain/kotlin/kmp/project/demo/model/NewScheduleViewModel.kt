package kmp.project.demo.model

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn


class NewScheduleViewModel: ViewModel() {
    val title = mutableStateOf("")
    val content = mutableStateOf("")
    val date = mutableStateOf( Clock.System.todayIn(TimeZone.currentSystemDefault()) )
    val repeatMode = mutableStateOf(0)
    val location = mutableStateOf("未设定")

    fun reset() {
        title.value = ""
        content.value = ""
        date.value = Clock.System.todayIn(TimeZone.currentSystemDefault())
        repeatMode.value = 0
        location.value = "未设定"
    }
}