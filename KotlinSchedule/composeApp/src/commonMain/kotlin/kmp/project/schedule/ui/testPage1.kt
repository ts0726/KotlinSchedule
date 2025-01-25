package kmp.project.schedule.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kmp.project.schedule.ScheduleSDK
import kmp.project.schedule.net.ScheduleApi
import kmp.project.schedule.ui.composableItem.CalendarPickerDialog
import kmp.project.schedule.util.LunarUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.*


@Composable
fun TestPage1(
    onButtonClick: () -> Unit,
    sdk: ScheduleSDK
) {
    val showDatePickerDialog = remember { mutableStateOf(false) }
    val time = remember { mutableStateOf(Clock.System.todayIn(TimeZone.currentSystemDefault())) }
    val today = time.value.atStartOfDayIn(TimeZone.UTC).toLocalDateTime(TimeZone.UTC)
    val lunar = LunarUtil(today).getChineseLunarDay()
    val receivedData = remember { mutableStateOf("") }
    val token = remember { mutableStateOf("") }
    Column {
//        Text(text = convertLocalDateToDate(time.value))
        Text(text = "农历${lunar}")
        IconButton(
            onClick = { showDatePickerDialog.value = !showDatePickerDialog.value },
        ) {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = "Select Repeat"
            )
        }
        if (showDatePickerDialog.value) {
            CalendarPickerDialog(
                onDismiss = { showDatePickerDialog.value = false },
                onDateSelected = {day ->
                    time.value = day
                },
                date = time
            )
        }
        Button(onClick = onButtonClick) {
            Text("test")
        }

        Button(onClick = {
            CoroutineScope(Dispatchers.IO).launch {
                receivedData.value = ScheduleApi.getAllSchedules("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOiJodHRwOi8vbG9jYWxob3N0OjgwODAvc2NoZWR1bGVzIiwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo4MDgwIiwibmFtZSI6InRlc3QiLCJleHAiOjE3Mzc3Nzg4NDF9.7_ifo7wwwfG9H8XKaD9btmAqU-HaT90UOdFIaGsAges")
                    .toString()
            }
        }) {
            Text("发送测试")
        }

        Button(
            onClick = {
                sdk.saveToken("access-token", "test token")
            }
        ) {
            Text("save token")
        }

        Button(
            onClick = {
                token.value = sdk.getToken("access-token").toString()
            }
        ) {
            Text("get token")
        }

        Text(text = receivedData.value)

        Text(text = token.value)
    }
}