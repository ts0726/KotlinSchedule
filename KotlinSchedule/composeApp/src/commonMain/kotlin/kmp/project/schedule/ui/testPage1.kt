package kmp.project.schedule.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kmp.project.schedule.ui.composableItem.CalendarPaager
import kmp.project.schedule.util.convertLocalDateToDate
import java.time.LocalDate

@Composable
fun TestPage1() {
    val time = remember { mutableStateOf<LocalDate>(LocalDate.now()) }
    Column {
        Text(text = convertLocalDateToDate(time.value))
//        val chineseDate = ChineseDate(DateUtil.parseDate(time.value))
//        Text(text = chineseDate.chineseDay)
        CalendarPaager {day ->
            time.value = day
        }
    }
}