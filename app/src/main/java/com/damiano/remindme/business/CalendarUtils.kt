package com.damiano.remindme.business

import android.content.Context
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

object CalendarUtils {
    var selectedDate: LocalDate? = null

    fun formattedShortTime(time: LocalTime): String {
        return time.format(DateTimeFormatter.ofPattern("HH:mm"))
    }

    fun monthYearFromDate(date: LocalDate, context: Context): String {
        val sharedPreferences = context.getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val savedLanguage = sharedPreferences.getString("App_Language", "en")
        val locale = Locale(savedLanguage!!)

        val formatter = DateTimeFormatter.ofPattern("MMMM yyyy", locale)
        return date.format(formatter)
    }

    fun monthDayFromDate(date: LocalDate, context: Context): String {
        val sharedPreferences = context.getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val savedLanguage = sharedPreferences.getString("App_Language", "en")
        val locale = Locale(savedLanguage!!)

        val formatter = DateTimeFormatter.ofPattern("MMMM d", locale)
        return date.format(formatter)
    }


    fun daysInMonthArray(): ArrayList<LocalDate> {
        val daysInMonthArray = ArrayList<LocalDate>()
        val currentSelectedDate = selectedDate ?: LocalDate.now()
        val firstOfMonth = currentSelectedDate.withDayOfMonth(1)
        val lastOfMonth = firstOfMonth.plusMonths(1).minusDays(1)

        val dayOfWeek = firstOfMonth.dayOfWeek
        val offset = if (dayOfWeek == DayOfWeek.SUNDAY) -6 else DayOfWeek.MONDAY.value - dayOfWeek.value
        val startDate = firstOfMonth.plusDays(offset.toLong())

        var currentDate = startDate
        while (currentDate <= lastOfMonth) {
            daysInMonthArray.add(currentDate)
            currentDate = currentDate.plusDays(1)
        }

        val lastDayOfWeek = daysInMonthArray.last().dayOfWeek
        if (lastDayOfWeek != DayOfWeek.SUNDAY) {
            val endDate = daysInMonthArray.last().with(DayOfWeek.SUNDAY).plusDays(1)
            currentDate = daysInMonthArray.last().plusDays(1)
            while (currentDate.isBefore(endDate)) {
                daysInMonthArray.add(currentDate)
                currentDate = currentDate.plusDays(1)
            }
        }

        return daysInMonthArray
    }

    fun daysInWeekArray(selectedDate: LocalDate): ArrayList<LocalDate> {
        val days = ArrayList<LocalDate>()
        var current = mondayForDate(selectedDate)
        val endDate = current.plusDays(7)

        while (current.isBefore(endDate)) {
            days.add(current)
            current = current.plusDays(1)
        }
        return days
    }

    private fun mondayForDate(current: LocalDate): LocalDate {
        var currentDate = current
        while (currentDate.dayOfWeek != DayOfWeek.MONDAY) {
            currentDate = currentDate.minusDays(1)
        }
        return currentDate
    }
}
