package kmp.project.schedule.ui

import androidx.compose.runtime.Composable
import kmp.project.schedule.ui.composableItem.CalendarView
import java.time.YearMonth

@Composable
fun TestPage1() {
    CalendarView(YearMonth.now())
}