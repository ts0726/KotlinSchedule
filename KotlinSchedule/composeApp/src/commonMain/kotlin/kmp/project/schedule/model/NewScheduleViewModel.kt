package kmp.project.schedule.model

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import kmp.project.schedule.ScheduleSDK
import kmp.project.schedule.util.getDayTimestamp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class NewScheduleViewModel: ViewModel() {
    val title = mutableStateOf("")
    val content = mutableStateOf("")
    val date = mutableStateOf( getDayTimestamp() )
    val repeatMode = mutableStateOf(0)
    val location = mutableStateOf("未设定")

    fun onSave(sdk: ScheduleSDK, navController: NavController) {
        CoroutineScope(Dispatchers.IO).launch {
            sdk.insertSchedule(
                title = title.value,
                content = content.value,
                date = date.value,
                repeatMode = repeatMode.value,
                location = location.value
            )
            withContext(Dispatchers.Main) {
                navController.popBackStack()
            }
        }
    }
}