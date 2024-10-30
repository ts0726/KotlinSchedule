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
import cn.hutool.core.date.ChineseDate
import cn.hutool.core.date.DateUtil
import cn.hutool.core.date.chinese.LunarInfo
import kmp.project.schedule.util.convertLocalDateToDate
import kmp.project.schedule.util.convertMonthOfYearToChinese
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import java.time.LocalDate
import java.time.YearMonth

@Composable
@Preview
fun CalendarPreview() {
//    CalendarView(YearMonth.now()) {}
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalendarPaager(onDayClick: (LocalDate) -> Unit) {
    val initPager = (YearMonth.now().year - 1901 - 1) * 12 + YearMonth.now().monthValue
    val pagerState = rememberPagerState(initialPage = initPager, pageCount = { (2099 - 1901) * 12 })
    val year = remember { mutableIntStateOf(YearMonth.now().year) }
    val month = remember { mutableIntStateOf(YearMonth.now().monthValue) }
    var days = generateCalendarDays(YearMonth.now(), onDayClick)
    //初始化被选择的日期为今天
    val selectedDay = remember { mutableStateOf(initSelectedDay(YearMonth.now(), days)) }

    LaunchedEffect(pagerState.currentPage) {
        val yearMonth = YearMonth.now().plusMonths((pagerState.currentPage - initPager).toLong())
        year.value = yearMonth.year
        month.value = yearMonth.monthValue
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = convertLocalDateToDate(LocalDate.ofEpochDay(selectedDay.value)),
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
                text = "${year.value}年 ${convertMonthOfYearToChinese(month.value)}",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
            )
            pageSwitcher(pagerState, selectedDay, days, onDayClick)
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
            days = generateCalendarDays(YearMonth.now().plusMonths((page - initPager).toLong()), onDayClick)
            CalendarView(YearMonth.now().plusMonths((page - initPager).toLong()), selectedDay, days)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun pageSwitcher(pagerState: PagerState, selectedDay: MutableState<Long>, days: List<CalendarDay>, onDayClick: (LocalDate) -> Unit) {
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
                        pagerState.animateScrollToPage(page = (YearMonth.now().year - 1901 - 1) * 12 + YearMonth.now().monthValue)
                        selectedDay.value = initSelectedDay(YearMonth.now(), days)
                        onDayClick(LocalDate.ofEpochDay(selectedDay.value))
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
fun CalendarView(yearMonth: YearMonth, selectedDay: MutableState<Long>, days: List<CalendarDay>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(370.dp),
        verticalArrangement = Arrangement.SpaceAround
    ) {
        var index = 0
        for (i in 1 .. days.size / 7) {
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            ) {
                while (index < i * 7){
                    val day = days[index]
                    var isSelected = false
                    //判断是否为本月，否则会超出访问范围导致崩溃
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
                            if (day.isCurrentMonth) {
                                Text(
                                    text = toLunarCalendar(yearMonth.atDay(day.day)),
                                    color = calendarTextColor(day, isSelected),
                                    textAlign = TextAlign.Center,
                                    lineHeight = 9.sp,
                                    fontSize = 9.sp,
                                )
                            }
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

fun generateCalendarDays(yearMonth: YearMonth, onDayClick: (LocalDate) -> Unit): List<CalendarDay> {
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
                onClick = {onDayClick(date)}
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

fun toLunarCalendar(day: LocalDate): String {
    val chineseDate = ChineseDate(DateUtil.parseDate(day.toString()))
    if (chineseFestival(chineseDate) == "") {
        if (chineseDate.term != "")
            return chineseDate.term
        if (chineseDate.chineseDay == "初一")
            return if (chineseDate.chineseMonthName == "") chineseDate.chineseMonth else chineseDate.chineseMonthName
        return chineseDate.chineseDay
    }
    return chineseFestival(chineseDate)
}

fun chineseFestival(chineseDate: ChineseDate): String {
    val month = if (chineseDate.chineseMonthName == "") chineseDate.chineseMonth else chineseDate.chineseMonthName
    when (month) {
        "正月" -> {
            when(chineseDate.chineseDay) {
                "初一" -> return "春节"
                "十五" -> return "元宵节"
            }
        }
        "二月" -> {
            when(chineseDate.chineseDay) {
                "初二" -> return "龙抬头"
            }
        }
        "五月" -> {
            when(chineseDate.chineseDay) {
                "初五" -> return "端午节"
            }
        }
        "六月" -> {
            when(chineseDate.chineseDay) {
                "廿四" -> return "火把节"
            }
        }
        "七月" -> {
            when(chineseDate.chineseDay) {
                "初七" -> return "七夕"
                "十五" -> return "中元节"
            }
        }
        "八月" -> {
            when(chineseDate.chineseDay) {
                "十五" -> return "中秋节"
            }
        }
        "九月" -> {
            when(chineseDate.chineseDay) {
                "初九" -> return "重阳节"
            }
        }
        "腊月" -> {
            when(chineseDate.chineseDay) {
                "初八" -> return "腊八节"
                "廿三" -> return "小年"
                "廿九" -> {
                    //判断大小月，如果12月是小月，则29是除夕，否则30为除夕
                    val lunarYear = chineseDate.chineseYear
                    val lunarMonth = chineseDate.month
                    val lunarDay = chineseDate.day
                    var flag = false
                    if (12 == lunarMonth && 29 == lunarDay) {
                        if (29 == LunarInfo.monthDays(lunarYear, lunarMonth)) {
                            flag = true
                        }
                    }
                    if (flag)
                        return "除夕"
                }

                "三十" -> return "除夕"
            }
        }
    }
    return ""
}