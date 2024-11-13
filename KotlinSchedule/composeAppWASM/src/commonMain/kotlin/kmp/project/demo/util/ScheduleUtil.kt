package kmp.project.schedule.util

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * 获取重复选项
 * @param date 接收时间值，用于获取重复日期
 * @param index 重复选项索引
 */
fun getRepeat(date: Long, index: Int): String {
    return getOptions(date)[index]
}

/**
 * 获取重复选项
 * @param date 接收时间值，用于获取重复日期
 */
fun getOptions(date: Long): List<String> {
    val instant = Instant.fromEpochMilliseconds(date)
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    val options = listOf(
        "一次性",
        "每天",
        "每周" + convertDayOfWeekToChinese(localDateTime.dayOfWeek.name),
        "每月" + localDateTime.dayOfMonth + "日",
        "每年" + localDateTime.monthNumber + "月" + localDateTime.dayOfMonth + "日"
    )
    return options
}