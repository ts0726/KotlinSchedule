package kmp.project.demo.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kmp.project.demo.ui.composableItem.CalendarPickerDialog
import kmp.project.demo.util.LunarUtil
import kotlinx.datetime.*


@Composable
fun TestPage1() {
    val showDatePickerDialog = remember { mutableStateOf(false) }
    val time = remember { mutableStateOf(Clock.System.todayIn(TimeZone.currentSystemDefault())) }
    val today = time.value.atStartOfDayIn(TimeZone.UTC).toLocalDateTime(TimeZone.UTC)
    val lunar = LunarUtil(today).getChineseLunarDay()
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
                }
            )
        }
    }
}