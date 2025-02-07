package kmp.project.schedule.util

import kmp.project.schedule.util.timeUtil.convertDayOfWeekToChinese
import kotlinx.datetime.LocalDate

/**
 * 获取重复选项
 * @param date 接收时间值，用于获取重复日期
 * @param index 重复选项索引
 */
fun getRepeat(date: LocalDate, index: Int): String {
    return getOptions(date)[index]
}

/**
 * 获取重复选项
 * @param date 接收时间值，用于获取重复日期
 */
fun getOptions(date: LocalDate): List<String> {
//    val instant = Instant.fromEpochMilliseconds(date)
//    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    val options = listOf(
        "一次性",
        "每天",
        "每周" + convertDayOfWeekToChinese(date.dayOfWeek.name),
        "每月" + date.dayOfMonth + "日",
        "每年" + date.monthNumber + "月" + date.dayOfMonth + "日"
    )
    return options
}