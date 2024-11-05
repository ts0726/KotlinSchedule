package kmp.project.schedule.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kmp.project.schedule.util.LunarUtil
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime


@Composable
fun TestPage1() {
    val showDatePickerDialog = remember { mutableStateOf(false) }
//    val time = remember { mutableStateOf<LocalDate>(LocalDate.now()) }
    val date = "2025-10-23"
    val today = LocalDate.parse(date).atStartOfDayIn(TimeZone.UTC).toLocalDateTime(TimeZone.UTC)
    val lunar = LunarUtil(today)
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
//        val chineseDate = ChineseDate(DateUtil.parseDate(time.value))
//        Text(text = chineseDate.chineseDay)
//        CalendarPaager {day ->
//            time.value = day
//        }
        if (showDatePickerDialog.value) {
//            CalendarPickerDialog(
//                onDismiss = { showDatePickerDialog.value = false },
//                onDateSelected = {day ->
//                    time.value = day
//                }
//            )
        }
    }
}