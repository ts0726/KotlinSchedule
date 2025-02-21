package kmp.project.schedule.util

import kmp.project.schedule.util.timeUtil.convertDayOfWeekToChinese
import kmp.project.schedule.viewModel.RepeatMode
import kotlinx.datetime.LocalDate

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
        RepeatMode.MONTHLY -> "每月" + date.dayOfMonth + "日"
        RepeatMode.YEARLY -> "每年" + date.monthNumber + "月" + date.dayOfMonth + "日"
    }
}

/**
 * 获取所有重复选项
 */
fun getOptions(): List<RepeatMode> {
    return RepeatMode.entries.map { it }
}