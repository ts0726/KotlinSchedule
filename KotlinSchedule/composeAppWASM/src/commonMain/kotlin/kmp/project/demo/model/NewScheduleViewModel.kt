package kmp.project.demo.model

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import kmp.project.schedule.util.getDayTimestamp
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime


class NewScheduleViewModel: ViewModel() {
    val title = mutableStateOf("")
    val content = mutableStateOf("")
    val date = mutableStateOf( getDayTimestamp() )
    val repeatMode = mutableStateOf(0)
    val location = mutableStateOf("未设定")
}