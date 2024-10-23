package kmp.project.schedule.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kmp.project.schedule.ui.composableItem.CalendarView
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun TestPage1() {
    val time = remember { mutableStateOf("time") }
    Column {
        Text(text = time.value)
        CalendarView(YearMonth.now()) { day ->
            time.value = day.toString()
        }
    }
}