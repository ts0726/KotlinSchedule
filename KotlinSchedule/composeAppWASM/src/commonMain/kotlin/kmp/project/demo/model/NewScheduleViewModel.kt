package kmp.project.demo.model

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import kmp.project.schedule.util.getDayTimestamp


class NewScheduleViewModel: ViewModel() {
    val title = mutableStateOf("")
    val content = mutableStateOf("")
    val date = mutableStateOf( getDayTimestamp() )
    val repeatMode = mutableStateOf(0)
    val location = mutableStateOf("未设定")

    fun reset() {
        title.value = ""
        content.value = ""
        date.value = getDayTimestamp()
        repeatMode.value = 0
        location.value = "未设定"
    }
}