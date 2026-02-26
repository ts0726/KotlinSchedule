package kmp.project.schedule.util

import kmp.project.schedule.entity.RepeatMode
import kmp.project.schedule.util.timeUtil.convertDayOfWeekToChinese
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number

/**
 * 获取重复选项
 * @param date 接收时间值，用于获取重复日期
 * @param repeatMode 重复选项索引
 */
fun getRepeat(date: LocalDate, repeatMode: RepeatMode): String {
    return when(repeatMode) {
        RepeatMode.NONE -> "一次性"
        RepeatMode.DAILY -> "每天"
        RepeatMode.WEEKLY -> "每周" + convertDayOfWeekToChinese(date.dayOfWeek.name)
        RepeatMode.MONTHLY -> "每月" + date.day + "日"
        RepeatMode.YEARLY -> "每年" + date.month.number + "月" + date.day + "日"
    }
}

/**
 * 获取所有重复选项
 */
fun getOptions(): List<RepeatMode> {
    return RepeatMode.entries.map { it }
}