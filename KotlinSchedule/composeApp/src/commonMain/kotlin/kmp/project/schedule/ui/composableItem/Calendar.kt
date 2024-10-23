package kmp.project.schedule.ui.composableItem

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
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
    val selectedDay = remember { mutableStateOf(initSelectedDay(yearMonth, days)) }

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
                    .padding(top = 5.dp, bottom = 5.dp)
            ) {
                while (index < i * 7){
                    val day = days[index]
                    var isSelected = false
                    if (day.isCurrentMonth) {
                        isSelected = selectedDay.value == yearMonth.atDay(day.day).toEpochDay()
                    }
                    Card(
                        shape = CircleShape,
                        onClick = {
                            if(day.isCurrentMonth) {
                                day.onClick()
                                selectedDay.value = yearMonth.atDay(day.day).toEpochDay()
                            }
                        },
                        colors = CardColors(
                            containerColor = calendarTodayColor(day, isSelected),
                            contentColor = Color.LightGray,
                            disabledContainerColor = calendarTextColor(day, isSelected),
                            disabledContentColor = calendarTextColor(day, isSelected)
                        ),
                        border = calendarSelectedBorder(day, isSelected),
                        modifier = Modifier.size(50.dp),
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = day.day.toString(),
                                color = calendarTextColor(day, isSelected),
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.Center,
                                lineHeight = 13.sp,
                                fontSize = 13.sp,
                            )
                            Text(
                                text = "初一",
                                color = calendarTextColor(day, isSelected),
                                textAlign = TextAlign.Center,
                                lineHeight = 9.sp,
                                fontSize = 9.sp,
                            )
                        }
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
fun calendarTextColor(day: CalendarDay, isSelected: Boolean): Color {
    if (isSelected && day.isCurrentMonth)
        return MaterialTheme.colorScheme.onPrimary
    if (day.isToday) {
        return MaterialTheme.colorScheme.primary
    }
    if (day.isCurrentMonth)
        return MaterialTheme.colorScheme.onSurface
    return MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
}

@Composable
fun calendarTodayColor(day: CalendarDay, isSelected: Boolean): Color {
    if (isSelected && day.isCurrentMonth)
        return MaterialTheme.colorScheme.primary
    return MaterialTheme.colorScheme.surface
}

@Composable
fun calendarSelectedBorder(day: CalendarDay, isSelected: Boolean):BorderStroke {
    if (day.isToday && !isSelected) {
        return BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
    }
    return BorderStroke(0.dp, Color.Transparent)
}

fun initSelectedDay(yearMonth: YearMonth, days: List<CalendarDay>):Long {
    days.forEach { day ->
        if (day.isToday) {
            return yearMonth.atDay(day.day).toEpochDay()
        }
    }
    return 0L
}