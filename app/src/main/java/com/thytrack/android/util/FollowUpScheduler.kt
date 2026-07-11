package com.thytrack.android.util

import java.util.Calendar
import java.util.Date

/** X 轴刻度自适应算法（端口化自 Flutter）。 */
enum class XAxisStride { MONTH, QUARTER, YEAR }

object XAxisStrideCalculator {
    fun calculate(dates: List<Date>): XAxisStride {
        if (dates.size < 2) return XAxisStride.MONTH
        val sorted = dates.sorted()
        val span = ((sorted.last().time - sorted.first().time) / 86_400_000L).toInt()
        return when {
            span < 180 -> XAxisStride.MONTH
            span < 730 -> XAxisStride.QUARTER
            else -> XAxisStride.YEAR
        }
    }
}

/** 复诊日期计算（端口化自 Flutter follow_up_scheduler.dart）。 */
object FollowUpScheduler {
    fun nextVisitDate(lastVisit: Date, intervalMonths: Int): Date =
        Calendar.getInstance().apply {
            time = lastVisit
            add(Calendar.MONTH, intervalMonths)
        }.time

    fun daysUntilNextVisit(lastVisit: Date, intervalMonths: Int): Long {
        val next = nextVisitDate(lastVisit, intervalMonths)
        return (next.time - Date().time) / 86_400_000L
    }

    fun shouldRemind(lastVisit: Date, intervalMonths: Int, advanceDays: Int): Boolean =
        daysUntilNextVisit(lastVisit, intervalMonths) <= advanceDays

    fun isOverdue(lastVisit: Date, intervalMonths: Int): Boolean =
        daysUntilNextVisit(lastVisit, intervalMonths) < 0
}
