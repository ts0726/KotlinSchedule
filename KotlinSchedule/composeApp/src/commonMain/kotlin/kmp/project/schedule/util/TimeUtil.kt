package kmp.project.schedule.util

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

/**
 * 获取日期，返回xx月xx日 星期x
 */
fun getCurrentDate(): String {
    val currentInstant = Clock.System.now()
    val currentDate = currentInstant.toLocalDateTime(TimeZone.currentSystemDefault())
    val month = currentDate.monthNumber
    val day = currentDate.dayOfMonth
    val dayOfWeek = currentDate.dayOfWeek.name
    return "$month" + "月" + "$day" + "日 " + "星期${convertDayOfWeekToChinese(dayOfWeek)}"
}

/**
 * 将时间戳转换为日期字符串
 * @param millis 时间戳
 */
fun convertMillisToDate(millis: Long): String {
    val instant = Instant.fromEpochMilliseconds(millis)
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return "${localDateTime.year}年${localDateTime.monthNumber}月${localDateTime.dayOfMonth}日" +
            " 周${convertDayOfWeekToChinese(localDateTime.dayOfWeek.name)}"
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

/**
 * 获取“日”时间戳
 */
fun getDayTimestamp(): Long {
    val currentInstant = Clock.System.now()
    val currentDate = currentInstant.toLocalDateTime(TimeZone.currentSystemDefault())
    return currentDate.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
}