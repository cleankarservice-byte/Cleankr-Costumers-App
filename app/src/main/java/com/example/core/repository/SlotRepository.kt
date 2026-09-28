package com.example.core.repository

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class SlotRepository {

    fun getUpcomingDates(daysCount: Int = 7): List<Pair<String, String>> {
        val list = mutableListOf<Pair<String, String>>()
        val calendar = Calendar.getInstance()
        val formatDisplay = SimpleDateFormat("EEE, d MMM", Locale.ENGLISH)
        val formatValue = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)

        for (i in 0 until daysCount) {
            val date = calendar.time
            val label = if (i == 0) "Today (${formatDisplay.format(date)})" else if (i == 1) "Tomorrow (${formatDisplay.format(date)})" else formatDisplay.format(date)
            list.add(Pair(label, formatValue.format(date)))
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        return list
    }

    fun getAvailableTimeSlots(): List<String> = listOf(
        "08:00 AM - 10:00 AM",
        "10:00 AM - 12:00 PM",
        "12:00 PM - 02:00 PM",
        "02:00 PM - 04:00 PM",
        "04:00 PM - 06:00 PM",
        "06:00 PM - 08:00 PM"
    )
}
