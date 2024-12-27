package kmp.project.schedule.util

import kotlinx.datetime.*

/**
 * 获取日期，返回xx月xx日 星期x
 */
fun getCurrentDate(date: LocalDate): String {
    val month = date.monthNumber
    val day = date.dayOfMonth
    val dayOfWeek = date.dayOfWeek.name
    return "$month" + "月" + "$day" + "日 " + "星期${convertDayOfWeekToChinese(dayOfWeek)}"
}

///**
// * 将时间戳转换为日期字符串
// * @param millis 时间戳
// */
//fun convertMillisToDate(millis: Long): String {
//    val instant = Instant.fromEpochMilliseconds(millis)
//    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
//    return "${localDateTime.year}年${localDateTime.monthNumber}月${localDateTime.dayOfMonth}日" +
//            " 周${convertDayOfWeekToChinese(localDateTime.dayOfWeek.name)}"
//}
//
fun convertLocalDateToDate(date: LocalDate): String {
    return "${date.year}年${date.monthNumber}月${date.dayOfMonth}日 周${convertDayOfWeekToChinese(date.dayOfWeek.name)}"
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
 * 获取“日”时间戳
 */
//fun getDayTimestamp(): Long {
//    val currentInstant = Clock.System.now()
//    val currentDate = currentInstant.toLocalDateTime(TimeZone.currentSystemDefault())
//    return currentDate.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
//}

/**
 * 计算距离今日的天数
 */
fun getDaysFromToday(date: LocalDate): Int {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    return date.toEpochDays() - today.toEpochDays()
}