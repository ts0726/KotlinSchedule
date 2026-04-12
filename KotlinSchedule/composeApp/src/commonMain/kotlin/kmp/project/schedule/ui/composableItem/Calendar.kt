package kmp.project.schedule.ui.composableItem

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kmp.project.schedule.database.Schedule
import kmp.project.schedule.util.timeUtil.LunarUtil
import kmp.project.schedule.util.timeUtil.convertLocalDateToDate
import kmp.project.schedule.util.timeUtil.convertMonthOfYearToChinese
import kmp.project.schedule.viewModel.ScheduleViewModel
import kotlinx.coroutines.launch
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.number
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import kotlin.time.Clock

@Suppress("UnrememberedMutableState")
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalendarPager(
    currentDate: LocalDate,
    onDayClick: (LocalDate) -> Unit,
    onMonthChanged: (LocalDate) -> Unit,
    viewMode: Int = 0,
    scheduleViewModel: ScheduleViewModel
) {
    val initPager = (currentDate.year - 1901 - 1) * 12 + currentDate.month.number
    val pagerState = rememberPagerState(initialPage = initPager, pageCount = { (2099 - 1901) * 12 })

    val currentYearMonth = remember(pagerState.currentPage) {
        currentDate.plus(DatePeriod(months = pagerState.currentPage - initPager))
    }

    //初始化被选择的日期为今天
    val selectedDay = remember(currentDate) {
        mutableLongStateOf(currentDate.toEpochDays())
    }

    var parentWidth by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current // 获取当前屏幕密度

    LaunchedEffect(pagerState.currentPage) {
        val newDate = currentDate.plus(DatePeriod(months = pagerState.currentPage - initPager))
        onMonthChanged(newDate)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 5.dp, end = 5.dp, bottom = 5.dp)
            .onGloballyPositioned{ coordinates ->
                parentWidth = with(density) { coordinates.size.width.toDp() }
            },
    ) {
        Text(
            text = convertLocalDateToDate(LocalDate.fromEpochDays(selectedDay.longValue)),
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
                text = "${currentYearMonth.year}年 ${convertMonthOfYearToChinese(currentYearMonth.month.number)}",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
            )
            PageSwitcher(pagerState, selectedDay, onDayClick)
        }
        Row(
            horizontalArrangement = Arrangement.SpaceAround,
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
            key = { page -> "month_$page" }
        ) { page ->
            val targetDate = remember(page) {
                currentDate.plus(DatePeriod(months = page - initPager))
            }
            val days = remember(page) {
                generateCalendarDays(targetDate, onDayClick)
            }
            CalendarView(
                date = targetDate,
                selectedDay = selectedDay,
                days = days,
                viewMode = viewMode,
                parentWidth = parentWidth,
                scheduleViewModel = scheduleViewModel
            )
        }
    }
}

@Composable
fun PageSwitcher(
    pagerState: PagerState,
    selectedDay: MutableState<Long>,
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
                        pagerState.animateScrollToPage(page = (today.year - 1901 - 1) * 12 + today.month.number)
                        selectedDay.value = LocalDate(today.year, today.month, today.day).toEpochDays()
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

/**
 * 日历视图
 * @param date 当前日期
 * @param selectedDay 选中的日期
 * @param days 日期列表
 * @param viewMode 视图模式，0为周视图，1为月
 * @param parentWidth 父组件宽度，用于月视图中计算每个日期卡片的宽度
 */
@Composable
fun CalendarView(
    date: LocalDate,
    selectedDay: MutableState<Long>,
    days: List<CalendarDay>,
    viewMode: Int,
    parentWidth: Dp,
    scheduleViewModel: ScheduleViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (viewMode == 0) 370.dp else 800.dp),
        verticalArrangement = Arrangement.SpaceAround
    ) {
        var index = 0
        for (i in 1..days.size / 7) {
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = if (viewMode == 0) Modifier.fillMaxWidth().height(60.dp)
                        else Modifier.fillMaxWidth().weight(1f)
            ) {
                while (index < i * 7) {
                    val day = days[index]
                    val targetDate = remember(date, day.day) {
                        if (day.isCurrentMonth) {
                            LocalDate(date.year, date.month.number, day.day)
                        } else {
                            null
                        }
                    }
                    val todaySchedules = remember(targetDate) { mutableStateOf(emptyList<Schedule>()) }

                    if (targetDate != null) {
                        todaySchedules.value = scheduleViewModel.loadTodaySchedulesFromCache(targetDate)
                    }
                    key("${date.year}-${date.month.number}") {
                        if (viewMode == 0)
                            CalendarDayCard(
                                day = day,
                                date = date,
                                selectedDay = selectedDay,
                                todaySchedulesCount = todaySchedules.value.size
                            )
                        else
                            MonthlyCalendarDayCard(
                                day = day,
                                date = date,
                                selectedDay = selectedDay,
                                calendarWidth = parentWidth,
                                todaySchedules = todaySchedules.value
                            )
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
fun CalendarDayCard(
    day: CalendarDay,
    date: LocalDate,
    selectedDay: MutableState<Long>,
    todaySchedulesCount: Int
) {
    val isSelected = remember(day, selectedDay.value) {
        day.isCurrentMonth && selectedDay.value == LocalDate(date.year, date.month.number, day.day).toEpochDays()
    }

    Card(
        shape = CircleShape,
        onClick = {
            if (day.isCurrentMonth) {
                day.onClick()
                selectedDay.value = LocalDate(date.year, date.month.number, day.day).toEpochDays()
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
                        LocalDate(date.year, date.month.number, day.day)
                            .atStartOfDayIn(TimeZone.UTC)
                            .toLocalDateTime(TimeZone.UTC)
                    ).getChineseLunarDay(),
                    color = calendarTextColor(day, isSelected),
                    textAlign = TextAlign.Center,
                    lineHeight = 9.sp,
                    fontSize = 9.sp,
                )
            }
            Text(
                text = if (todaySchedulesCount == 0) "" else todaySchedulesCount.toString(),
                color = calendarTextColor(day, isSelected),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 10.sp,
            )
        }
    }
}

/**
 * 月视图日历卡片
 * @param day 日期数据类
 * @param date 当前日期
 * @param selectedDay 选中的日期
 * @param calendarWidth 父组件宽度，用于计算卡片宽度
 * @param todaySchedules 当日的日程列表，用于在月视图中显示日程信息
 */
@Composable
fun MonthlyCalendarDayCard(
    day: CalendarDay,
    date: LocalDate,
    selectedDay: MutableState<Long>,
    calendarWidth: Dp,
    todaySchedules: List<Schedule>
) {
    val isSelected = remember(day, selectedDay.value) {
        day.isCurrentMonth && selectedDay.value == LocalDate(date.year, date.month.number, day.day).toEpochDays()
    }

    Card(
        shape = RoundedCornerShape(0.dp),
        onClick = {
            if (day.isCurrentMonth) {
                day.onClick()
                selectedDay.value = LocalDate(date.year, date.month.number, day.day).toEpochDays()
            }
        },
        colors = CardColors(
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = Color.LightGray,
            disabledContainerColor = MaterialTheme.colorScheme.background,
            disabledContentColor = MaterialTheme.colorScheme.background
        ),
        modifier = Modifier.fillMaxHeight().width(calendarWidth / 7),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 0.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else Color(255, 255, 255, 50)
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 5.dp, bottom = 5.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Card(
                    modifier = Modifier.size(30.dp),
                    shape = CircleShape,
                    colors = CardColors(
                        containerColor = calendarTodayColor(day, isSelected),
                        contentColor = Color.LightGray,
                        disabledContainerColor = calendarTextColor(day, isSelected),
                        disabledContentColor = calendarTextColor(day, isSelected)
                    ),
                    border = calendarSelectedBorder(day, isSelected),
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
                            fontSize = 10.sp,
                        )
                    }
                }
                if (day.isCurrentMonth) {
                    Text(
                        text = LunarUtil(
                            LocalDate(date.year, date.month.number, day.day)
                                .atStartOfDayIn(TimeZone.UTC)
                                .toLocalDateTime(TimeZone.UTC)
                        ).getChineseLunarDay(),
                        color = if (day.isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                        lineHeight = 9.sp,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            if (day.isCurrentMonth) {
                for (schedule in todaySchedules) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 5.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(4.dp),
                        colors = CardColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            contentColor = MaterialTheme.colorScheme.primary,
                            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            disabledContentColor = MaterialTheme.colorScheme.primary
                        ),
                        border = BorderStroke(0.dp, Color.Transparent)
                    ) {
                        Text(
                            text = schedule.title,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
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
    val daysInMonth = getMonthOfDay(date.year, date.month.number)
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

    //添加上个月的日期
    val previousMonth = date.minus(DatePeriod(months = 1))
    val daysInPreviousMonth = getMonthOfDay(previousMonth.year, previousMonth.month.number)
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
        val day = LocalDate(date.year, date.month.number, i)
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