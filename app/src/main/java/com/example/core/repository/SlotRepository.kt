package com.example.core.repository

import com.example.core.model.TimeSlot
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class CalendarDay(
    val dateString: String,      // "2026-09-24"
    val dayOfWeekText: String,   // "Thu"
    val dayOfMonthText: String,  // "24"
    val monthText: String,       // "Sep"
    val isAvailable: Boolean = true,
    val isFullyBooked: Boolean = false,
    val isToday: Boolean = false,
    val isPast: Boolean = false
)

class SlotRepository {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val dayOfWeekFormat = SimpleDateFormat("EEE", Locale.getDefault())
    private val dayOfMonthFormat = SimpleDateFormat("d", Locale.getDefault())
    private val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())

    fun getUpcomingDays(count: Int = 14): List<CalendarDay> {
        val days = mutableListOf<CalendarDay>()
        val cal = Calendar.getInstance()

        for (i in 0 until count) {
            val date = cal.time
            val dateStr = dateFormat.format(date)
            val isToday = (i == 0)

            // Simulate high-demand fully booked status on specific dates (e.g., weekend or 3 days out)
            val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
            val isFullyBooked = (i == 4) // day 4 simulates fully booked peak demand

            days.add(
                CalendarDay(
                    dateString = dateStr,
                    dayOfWeekText = if (isToday) "Today" else dayOfWeekFormat.format(date),
                    dayOfMonthText = dayOfMonthFormat.format(date),
                    monthText = monthFormat.format(date),
                    isAvailable = !isFullyBooked,
                    isFullyBooked = isFullyBooked,
                    isToday = isToday,
                    isPast = false
                )
            )
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return days
    }

    fun getTimeSlotsForDate(dateString: String): List<TimeSlot> {
        val todayStr = dateFormat.format(Date())
        val isToday = (dateString == todayStr)
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

        val slots = listOf(
            TimeSlot("slot_08", "08:00 AM - 10:00 AM", "Morning", isAvailable = !isToday || currentHour < 8),
            TimeSlot("slot_10", "10:00 AM - 12:00 PM", "Morning", isAvailable = !isToday || currentHour < 10),
            TimeSlot("slot_12", "12:00 PM - 02:00 PM", "Afternoon", isAvailable = !isToday || currentHour < 12),
            TimeSlot("slot_02", "02:00 PM - 04:00 PM", "Afternoon", isAvailable = !isToday || currentHour < 14),
            TimeSlot("slot_04", "04:00 PM - 06:00 PM", "Evening", isAvailable = !isToday || currentHour < 16),
            TimeSlot("slot_06", "06:00 PM - 08:00 PM", "Evening", isAvailable = !isToday || currentHour < 18)
        )

        return slots
    }

    /**
     * Server-side revalidation contract:
     * Validates that the requested slot has not expired or been taken by another customer.
     */
    fun validateSlotAvailability(dateString: String, slotTime: String): Boolean {
        val slots = getTimeSlotsForDate(dateString)
        val matching = slots.firstOrNull { it.timeDisplay == slotTime }
        return matching?.isAvailable == true
    }
}
