package com.vio_0x.methodic.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun formatDate(date: Date): String {
    val today = Calendar.getInstance()
    val otherDay = Calendar.getInstance().apply { time = date }

    // Create formatters inside the function to use the current locale
    val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    val fullFormatter = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())

    return when {
        isSameDay(today, otherDay) -> "Today, ${timeFormatter.format(date)}"
        isYesterday(today, otherDay) -> "Yesterday, ${timeFormatter.format(date)}"
        else -> fullFormatter.format(date)
    }
}

fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

fun isYesterday(today: Calendar, otherDay: Calendar): Boolean {
    val yesterday = Calendar.getInstance().apply {
        time = today.time
        add(Calendar.DAY_OF_YEAR, -1)
    }
    return isSameDay(yesterday, otherDay)
}