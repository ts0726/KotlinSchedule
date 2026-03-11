package kmp.project.schedule.util.timeUtil

import kotlinx.datetime.DateTimeUnit
import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.number
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import kotlin.time.Instant.Companion.fromEpochMilliseconds

/**
 * 获取日期，返回xx月xx日 星期x
 */
fun getCurrentDate(date: LocalDate): String {
    val month = date.month.number
    val day = date.day
    val dayOfWeek = date.dayOfWeek.name
    return "$month" + "月" + "$day" + "日 " + "星期${convertDayOfWeekToChinese(dayOfWeek)}"
}

fun convertLocalDateToDate(date: LocalDate): String {
    return "${date.year}年${date.month.number}月${date.day}日 周${convertDayOfWeekToChinese(date.dayOfWeek.name)}"
}

fun convertLocalDateToDateSimple(date: LocalDate): String {
    return "${date.year}年${date.month.number}月${date.day}日"
}

/**
 * 转换英文星期为中文
 */
fun convertDayOfWeekToChinese(dayOfWeek: String): String {
    return when (dayOfWeek) {
        "MONDAY" -> "一"
        "TUESDAY" -> "二"
        "WEDNESDAY" -> "三"
        "THURSDAY" -> "四"
        "FRIDAY" -> "五"
        "SATURDAY" -> "六"
        "SUNDAY" -> "日"
        else -> "--"
    }
}

fun convertMonthOfYearToChinese(monthOfYear: Int): String {
    return when(monthOfYear) {
        1 -> "一月"
        2 -> "二月"
        3 -> "三月"
        4 -> "四月"
        5 -> "五月"
        6 -> "六月"
        7 -> "七月"
        8 -> "八月"
        9 -> "九月"
        10 -> "十月"
        11 -> "十一月"
        12 -> "十二月"
        else -> "--"
    }
}

/**
 * 计算距离今日的天数
 */
fun getDaysFromToday(date: LocalDate): Long {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    return date.toEpochDays() - today.toEpochDays()
}

fun getTimestamp(): Long {
    return Clock.System.now().toEpochMilliseconds()
}

fun getMonthDateRange(date: LocalDate): Pair<LocalDate, LocalDate> {
    val startDay = date.minus(date.day - 1, DateTimeUnit.DAY)
    val endDay = startDay.plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY)
    return Pair(startDay, endDay)
}

fun convertTimestampToLocalDate(timestamp: Long): LocalDate {
    return fromEpochMilliseconds(timestamp).toLocalDateTime(TimeZone.currentSystemDefault()).date
}