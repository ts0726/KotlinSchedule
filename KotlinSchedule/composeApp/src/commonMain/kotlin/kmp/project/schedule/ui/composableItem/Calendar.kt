package kmp.project.schedule.ui.composableItem

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*

@Composable
@Preview
fun CalendarPreview() {
    CalendarView(YearMonth.now())
}

@Composable
fun CalendarView(yearMonth: YearMonth) {
    val days = generateCalendarDays(yearMonth)

    Column {
        Text(
            text = yearMonth.month.getDisplayName(TextStyle.FULL, Locale.CHINA) + " " + yearMonth.year,
            modifier = Modifier.padding(16.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxSize()
        ) {
            listOf("日", "一", "二", "三", "四", "五", "六").forEach { day ->
                item {
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }
            items(days.size) { index ->
                val day = days[index]
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .aspectRatio(1f)
                        .padding(4.dp)
                ) {
                    Text(
                        text = day.day.toString(),
                        color = if (day.isCurrentMonth) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                }
            }
        }
    }
}

data class CalendarDay(val day: Int, val isCurrentMonth: Boolean)

fun generateCalendarDays(yearMonth: YearMonth): List<CalendarDay> {
    val days = mutableListOf<CalendarDay>()
    val firstDayOfMonth = yearMonth.atDay(1)
    val dayOfWeek = firstDayOfMonth.dayOfWeek.value % 7
    val daysInMonth = yearMonth.lengthOfMonth()

    //添加上个月的日期
    val previousMonth = yearMonth.minusMonths(1)
    val daysInPreviousMonth = previousMonth.lengthOfMonth()
    for (i in 1 .. dayOfWeek) {
        days.add(CalendarDay(daysInPreviousMonth - dayOfWeek + i, false))
    }

    //添加当前月份的日期
    for (i in 1 .. daysInMonth) {
        days.add(CalendarDay(i, true))
    }

    //添加下个月的日期
    if (days.size % 7 != 0) {
        val remainingDays = (days.size / 7 + 1) * 7 - days.size
        for (i in 1 .. remainingDays) {
            days.add(CalendarDay(i, false))
        }
    }

    return days
}