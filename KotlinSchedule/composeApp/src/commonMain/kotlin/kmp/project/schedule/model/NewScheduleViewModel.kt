package kmp.project.schedule.model

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import kmp.project.schedule.ScheduleSDK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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

    suspend fun onSave(sdk: ScheduleSDK, navController: NavController) {
        sdk.insertSchedule(
            title = title.value,
            content = content.value,
            date = date.value.toEpochDays().toLong(),
            repeatMode = repeatMode.value,
            location = location.value
        )
        withContext(Dispatchers.Main) {
            navController.popBackStack()
        }
    }
}