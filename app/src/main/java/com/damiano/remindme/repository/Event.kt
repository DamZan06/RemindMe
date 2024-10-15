package com.damiano.remindme.repository

import com.google.firebase.database.IgnoreExtraProperties
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@IgnoreExtraProperties
data class Event(
    val name: String = "",
    val date: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val eventId: String = "",
    val location: String = "",
    val description: String = "",
    val attendees: String = "",
    val timer5min: Boolean = false,
    val timer10min: Boolean = false,
    val timer15min: Boolean = false,
    val timer30min: Boolean = false,
    val timer45min: Boolean = false,
    val timer60min: Boolean = false,
    var isExpanded: Boolean = false
) : Comparable<Event> {
    override fun compareTo(other: Event): Int {
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        val thisTime = LocalTime.parse(this.startTime, formatter)
        val otherTime = LocalTime.parse(other.startTime, formatter)
        return thisTime.compareTo(otherTime)
    }
}
