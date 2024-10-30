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
import kmp.project.schedule.ui.composableItem.CalendarPaager
import kmp.project.schedule.ui.composableItem.CalendarPickerDialog
import kmp.project.schedule.util.convertLocalDateToDate
import java.time.LocalDate

@Composable
fun TestPage1() {
    val showDatePickerDialog = remember { mutableStateOf(false) }
    val time = remember { mutableStateOf<LocalDate>(LocalDate.now()) }
    Column {
        Text(text = convertLocalDateToDate(time.value))
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
            CalendarPickerDialog(
                onDismiss = { showDatePickerDialog.value = false },
                onDateSelected = {day ->
                    time.value = day
                }
            )
        }
    }
}