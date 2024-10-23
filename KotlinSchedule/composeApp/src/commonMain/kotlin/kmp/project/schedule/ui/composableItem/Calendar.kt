package kmp.project.schedule.ui.composableItem

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.ui.tooling.preview.Preview
import java.time.LocalDate
import java.time.YearMonth

@Composable
@Preview
fun CalendarPreview() {
    CalendarView(YearMonth.now()) {}
}

@Composable
fun CalendarView(yearMonth: YearMonth, onDayClick: (Long) -> Unit) {
    val days = generateCalendarDays(yearMonth, onDayClick)

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            listOf("日", "一", "二", "三", "四", "五", "六").forEach { day ->
                Card(
                    shape = CircleShape,
                    colors = CardColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.LightGray,
                        disabledContainerColor = Color.Transparent,
                        disabledContentColor = Color.Transparent
                    ),
                    modifier = Modifier.size(55.dp),
                ) {
                    Text(
                        text = day,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        var index = 0
        for (i in 1 .. days.size / 7) {
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(top = 3.dp, bottom = 3.dp)
            ) {
                while (index < i * 7){
                    val day = days[index]
                    Card(
                        shape = CircleShape,
                        onClick = { day.onClick() },
                        colors = CardColors(
                            containerColor = calendarTodayColor(day),
                            contentColor = Color.LightGray,
                            disabledContainerColor = calendarTextColor(day),
                            disabledContentColor = calendarTextColor(day)
                        ),
                        modifier = Modifier.size(55.dp),
                    ) {
                        Text(
                            text = day.day.toString(),
                            color = calendarTextColor(day),
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            fontSize = 15.sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "初一",
                            color = calendarTextColor(day),
                            textAlign = TextAlign.Center,
                            fontSize = 10.sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    index++
                }
            }
        }
    }
}

/**
 * 日期数据类
 * @param day 在当前月份的天数
 * @param isCurrentMonth 是否为当前月
 * @param isToday 是否为今天
 * @param onClick 回调函数，通过点击获取时间戳
 */
data class CalendarDay(val day: Int, val isCurrentMonth: Boolean, val isToday: Boolean, val onClick: () -> Unit)

fun generateCalendarDays(yearMonth: YearMonth, onDayClick: (Long) -> Unit): List<CalendarDay> {
    val days = mutableListOf<CalendarDay>()
    val firstDayOfMonth = yearMonth.atDay(1)
    val dayOfWeek = firstDayOfMonth.dayOfWeek.value % 7
    val daysInMonth = yearMonth.lengthOfMonth()
    val today = LocalDate.now()

    //添加上个月的日期
    val previousMonth = yearMonth.minusMonths(1)
    val daysInPreviousMonth = previousMonth.lengthOfMonth()
    for (i in 1 .. dayOfWeek) {
        days.add(
            CalendarDay(
                day = daysInPreviousMonth - dayOfWeek + i,
                isCurrentMonth = false,
                isToday = false,
                onClick = {}
            )
        )
    }

    //添加当前月份的日期
    for (i in 1 .. daysInMonth) {
        //获取当月的第i天，用于返回当天的时间戳
        val date = yearMonth.atDay(i)
        val isToday = yearMonth.year == today.year && yearMonth.month == today.month && i == today.dayOfMonth
        days.add(
            CalendarDay(
                day = i,
                isCurrentMonth = true,
                isToday = isToday,
                onClick = {onDayClick(date.toEpochDay())}
            )
        )
    }

    //添加下个月的日期
    if (days.size % 7 != 0) {
        val remainingDays = (days.size / 7 + 1) * 7 - days.size
        for (i in 1 .. remainingDays) {
            days.add(
                CalendarDay(
                    day = i,
                    isCurrentMonth = false,
                    isToday = false,
                    onClick = {}
                )
            )
        }
    }

    return days
}

@Composable
fun calendarTextColor(day: CalendarDay): Color {
    if (day.isToday)
        return MaterialTheme.colorScheme.onPrimary
    if (day.isCurrentMonth)
        return MaterialTheme.colorScheme.onSurface
    return MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
}

@Composable
fun calendarTodayColor(day: CalendarDay): Color {
    if (day.isToday)
        return MaterialTheme.colorScheme.primary
    return MaterialTheme.colorScheme.surface
}