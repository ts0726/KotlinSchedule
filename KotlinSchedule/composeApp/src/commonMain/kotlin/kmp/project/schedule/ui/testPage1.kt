package kmp.project.schedule.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import cn.hutool.core.date.ChineseDate
import cn.hutool.core.date.DateUnit
import cn.hutool.core.date.DateUtil
import kmp.project.schedule.ui.composableItem.CalendarPaager
import kmp.project.schedule.ui.composableItem.CalendarView
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun TestPage1() {
    val time = remember { mutableStateOf("2024-10-01") }
    Column {
        Text(text = time.value)
        val chineseDate = ChineseDate(DateUtil.parseDate(time.value))
        Text(text = chineseDate.chineseDay)
        CalendarPaager {day ->
            time.value = day.toString()
        }
    }
}