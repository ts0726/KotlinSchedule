package kmp.project.schedule.util

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant


class LunarUtil(cal: LocalDateTime) {
    private var year = 0
    private var month = 0
    private var day = 0
    private var leap = false

    companion object {
        val chineseNumber = arrayOf("01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12")
        private val chineseNumberDay = arrayOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12")
        private val lunarInfo = arrayOf(0x04bd8, 0x04ae0, 0x0a570,
                0x054d5, 0x0d260, 0x0d950, 0x16554, 0x056a0, 0x09ad0, 0x055d2,
                0x04ae0, 0x0a5b6, 0x0a4d0, 0x0d250, 0x1d255, 0x0b540, 0x0d6a0,
                0x0ada2, 0x095b0, 0x14977, 0x04970, 0x0a4b0, 0x0b4b5, 0x06a50,
                0x06d40, 0x1ab54, 0x02b60, 0x09570, 0x052f2, 0x04970, 0x06566,
                0x0d4a0, 0x0ea50, 0x06e95, 0x05ad0, 0x02b60, 0x186e3, 0x092e0,
                0x1c8d7, 0x0c950, 0x0d4a0, 0x1d8a6, 0x0b550, 0x056a0, 0x1a5b4,
                0x025d0, 0x092d0, 0x0d2b2, 0x0a950, 0x0b557, 0x06ca0, 0x0b550,
                0x15355, 0x04da0, 0x0a5d0, 0x14573, 0x052d0, 0x0a9a8, 0x0e950,
                0x06aa0, 0x0aea6, 0x0ab50, 0x04b60, 0x0aae4, 0x0a570, 0x05260,
                0x0f263, 0x0d950, 0x05b57, 0x056a0, 0x096d0, 0x04dd5, 0x04ad0,
                0x0a4d0, 0x0d4d4, 0x0d250, 0x0d558, 0x0b540, 0x0b5a0, 0x195a6,
                0x095b0, 0x049b0, 0x0a974, 0x0a4b0, 0x0b27a, 0x06a50, 0x06d40,
                0x0af46, 0x0ab60, 0x09570, 0x04af5, 0x04970, 0x064b0, 0x074a3,
                0x0ea50, 0x06b58, 0x055c0, 0x0ab60, 0x096d5, 0x092e0, 0x0c960,
                0x0d954, 0x0d4a0, 0x0da50, 0x07552, 0x056a0, 0x0abb7, 0x025d0,
                0x092d0, 0x0cab5, 0x0a950, 0x0b4a0, 0x0baa4, 0x0ad50, 0x055d9,
                0x04ba0, 0x0a5b0, 0x15176, 0x052b0, 0x0a930, 0x07954, 0x06aa0,
                0x0ad50, 0x05b52, 0x04b60, 0x0a6e6, 0x0a4e0, 0x0d260, 0x0ea65,
                0x0d530, 0x05aa0, 0x076a3, 0x096d0, 0x04bd7, 0x04ad0, 0x0a4d0,
                0x1d0b6, 0x0d250, 0x0d520, 0x0dd45, 0x0b5a0, 0x056d0, 0x055b2,
                0x049b0, 0x0a577, 0x0a4b0, 0x0aa50, 0x1b255, 0x06d20, 0x0ada0)

        //返回农历y年的总天数
        private fun yearDays(y: Int): Int {
            var sum = 348
            var i = 0x8000
            while (i > 0x8) {
                if ((lunarInfo[y - 1900] and i) != 0) {
                    sum += 1
                }
                i = i shr 1
            }
            return sum + leapDays(y)
        }

        //返回农历y年闰月的天数
        private fun leapDays(y: Int): Int {
            return if (leapMonth(y) != 0) {
                if ((lunarInfo[y - 1900] and 0x10000) != 0)
                    30
                else
                    29
            } else
                0
        }

        //判断y年闰哪个月，没闰将返回0
        private fun leapMonth(y: Int): Int {
            return lunarInfo[y - 1900] and 0xf
        }

        //返回y年m月的总天数
        private fun monthDays(y: Int, m: Int): Int {
            return if ((lunarInfo[y - 1900] and (0x10000 shr m)) == 0)
                29
            else
                30
        }

        fun getChinaDayString(day: Int): String {
            val chineseTen = arrayOf("0", "1", "2", "3")
            val n = if (day % 10 == 0) 9 else day % 10 - 1
            return if (day > 30) "" else if (day == 10) "10" else chineseTen[day / 10] + chineseNumberDay[n]
        }
    }

    init {
        val baseDate = LocalDateTime(1900, 1, 31, 0, 0)
        val offset = (cal.toInstant(TimeZone.UTC).epochSeconds - baseDate.toInstant(TimeZone.UTC).epochSeconds) / 86400
        var monCyl = 14
        var iYear = 1900
        var daysOfYear: Int
        var offsetVar = offset.toInt()

        while (iYear < 2050 && offsetVar > 0) {
            daysOfYear = yearDays(iYear)
            offsetVar -= daysOfYear
            monCyl += 12
            iYear++
        }

        if (offsetVar < 0) {
            offsetVar += yearDays(iYear - 1)
            iYear--
            monCyl -= 12
        }

        year = iYear
        val leapMonth = leapMonth(iYear)
        leap = false

        var iMonth = 1
        var daysOfMonth = 0

        while (iMonth < 13 && offsetVar > 0) {
            daysOfMonth = if (leapMonth > 0 && iMonth == leapMonth + 1 && !leap) {
                --iMonth
                leap = true
                leapDays(year)
            } else {
                monthDays(year, iMonth)
            }
            offsetVar -= daysOfMonth
            if (leap && iMonth == leapMonth + 1) leap = false
            if (!leap) monCyl++
            iMonth++
        }

        if (offsetVar == 0 && leapMonth > 0 && iMonth == leapMonth + 1) {
            if (leap) {
                leap = false
            } else {
                leap = true
                --iMonth
            }
        }

        if (offsetVar < 0) {
            offsetVar += daysOfMonth
            --iMonth
            --monCyl
        }

        month = iMonth
        day = offsetVar + 1
    }

    override fun toString(): String {
        return "$year-${if (leap) "闰" else ""}${chineseNumber[month - 1]}-${getChinaDayString(day)}"
    }
}