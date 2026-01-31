package kmp.project.schedule.ui.composableItem

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kmp.project.schedule.util.timeUtil.LunarUtil
import kmp.project.schedule.util.timeUtil.convertLocalDateToDate
import kmp.project.schedule.util.timeUtil.convertMonthOfYearToChinese
import kotlinx.coroutines.launch
import kotlinx.datetime.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalendarPager(currentDate: LocalDate, onDayClick: (LocalDate) -> Unit) {
    val initPager = (currentDate.year - 1901 - 1) * 12 + currentDate.monthNumber
    val pagerState = rememberPagerState(initialPage = initPager, pageCount = { (2099 - 1901) * 12 })
    val year = remember { mutableIntStateOf(currentDate.year) }
    val month = remember { mutableIntStateOf(currentDate.monthNumber) }
//    var days: List<CalendarDay>
    //初始化被选择的日期为今天
    val selectedDay = remember { mutableIntStateOf(currentDate.toEpochDays()) }

    LaunchedEffect(pagerState.currentPage) {
        val yearMonth = currentDate.plus(DatePeriod(months = pagerState.currentPage - initPager))
        year.intValue = yearMonth.year
        month.intValue = yearMonth.monthNumber
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = convertLocalDateToDate(LocalDate.fromEpochDays(selectedDay.intValue)),
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 10.dp, end = 10.dp, top = 5.dp, bottom = 5.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 10.dp, end = 10.dp, top = 5.dp, bottom = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${year.intValue}年 ${convertMonthOfYearToChinese(month.intValue)}",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
            )
            PageSwitcher(pagerState, selectedDay, onDayClick)
        }
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 5.dp, bottom = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf("日", "一", "二", "三", "四", "五", "六").forEach { day ->
                Text(
                    text = day,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(50.dp)
                )
            }
        }
        HorizontalPager(
            state = pagerState,
        ) { page ->
            val days = remember(page) {
                generateCalendarDays(currentDate.plus(DatePeriod(months = page - initPager)), onDayClick)
            }
            CalendarView(currentDate.plus(DatePeriod(months = page - initPager)), selectedDay, days)
        }
    }
}

@Composable
fun PageSwitcher(
    pagerState: PagerState,
    selectedDay: MutableState<Int>,
    onDayClick: (LocalDate) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    Row {
        Row {
            IconButton(
                onClick = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage - 1)
                    }
                },
            ){
                Icon(
                    imageVector = Icons.AutoMirrored.Default.KeyboardArrowLeft,
                    contentDescription = "Close"
                )
            }
            IconButton(
                onClick = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                },
            ){
                Icon(
                    imageVector = Icons.AutoMirrored.Default.KeyboardArrowRight,
                    contentDescription = "Close"
                )
            }
            IconButton(
                onClick = {
                    coroutineScope.launch {
                        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
                        pagerState.animateScrollToPage(page = (today.year - 1901 - 1) * 12 + today.monthNumber)
                        selectedDay.value = LocalDate(today.year, today.month, today.dayOfMonth).toEpochDays()
                        onDayClick(LocalDate.fromEpochDays(selectedDay.value))
                    }
                }
            ) {
                Text(
                    text = "今",
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
fun CalendarView(date: LocalDate, selectedDay: MutableState<Int>, days: List<CalendarDay>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(370.dp),
        verticalArrangement = Arrangement.SpaceAround
    ) {
        var index = 0
        for (i in 1..days.size / 7) {
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            ) {
                while (index < i * 7) {
                    val day = days[index]
                    key(day.day) {
                        CalendarDayCard(day, date, selectedDay)
                    }
                    index++
                }
            }
        }
    }
}

/**
 * 日历卡片
 * @param day 日期数据类
 * @param date 当前日期
 * @param selectedDay 选中的日期
 */
@Composable
fun CalendarDayCard(day: CalendarDay, date: LocalDate, selectedDay: MutableState<Int>) {
    val isSelected = remember(day, selectedDay.value) {
        day.isCurrentMonth && selectedDay.value == LocalDate(date.year, date.monthNumber, day.day).toEpochDays()
    }

    Card(
        shape = CircleShape,
        onClick = {
            if (day.isCurrentMonth) {
                day.onClick()
                selectedDay.value = LocalDate(date.year, date.monthNumber, day.day).toEpochDays()
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
            if (day.isCurrentMonth) {
                Text(
                    text = LunarUtil(
                        LocalDate(date.year, date.monthNumber, day.day)
                            .atStartOfDayIn(TimeZone.UTC)
                            .toLocalDateTime(TimeZone.UTC)
                    ).getChineseLunarDay(),
                    color = calendarTextColor(day, isSelected),
                    textAlign = TextAlign.Center,
                    lineHeight = 9.sp,
                    fontSize = 9.sp,
                )
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

/**
 * 生成日历日期列表
 * @param date 当前日期
 * @param onDayClick 点击日期回调函数
 * @return 日期列表
 */
fun generateCalendarDays(date: LocalDate, onDayClick: (LocalDate) -> Unit): List<CalendarDay> {
    val days = mutableListOf<CalendarDay>()
    val firstDayOfMonth = LocalDate(date.year, date.month, 1)
    val dayOfWeek = (firstDayOfMonth.dayOfWeek.ordinal + 1) % 7
    val daysInMonth = getMonthOfDay(date.year, date.monthNumber)
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

    //添加上个月的日期
    val previousMonth = date.minus(DatePeriod(months = 1))
    val daysInPreviousMonth = getMonthOfDay(previousMonth.year, previousMonth.monthNumber)
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
//        println("now $i")
        //获取当月的第i天，用于返回当天的时间戳
        val day = LocalDate(date.year, date.monthNumber, i)
        val isToday = day == today
        days.add(
            CalendarDay(
                day = i,
                isCurrentMonth = true,
                isToday = isToday,
                onClick = {onDayClick(day)}
            )
        )
    }

    //添加下个月的日期
    if (days.size % 7 != 0) {
//        println("days size: ${days.size}")
        val remainingDays = (days.size / 7 + 1) * 7 - days.size
//        println(remainingDays)
        for (i in 1 .. remainingDays) {
//            println("after $i")
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
        return MaterialTheme.colorScheme.onBackground
    return MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
}

@Composable
fun calendarTodayColor(day: CalendarDay, isSelected: Boolean): Color {
    if (isSelected && day.isCurrentMonth)
        return MaterialTheme.colorScheme.primary
    return MaterialTheme.colorScheme.background
}

@Composable
fun calendarSelectedBorder(day: CalendarDay, isSelected: Boolean):BorderStroke {
    if (day.isToday && !isSelected) {
        return BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
    }
    return BorderStroke(0.dp, Color.Transparent)
}

fun getMonthOfDay(year: Int, month: Int): Int {
    val day: Int = if (year % 4 == 0 && year % 100 != 0 || year % 400 == 0) 29 else 28

    when (month) {
        1, 3, 5, 7, 8, 10, 12 -> return 31
        4, 6, 9, 11 -> return 30
        2 -> return day
    }
    return 0
}