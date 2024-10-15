package com.damiano.remindme.repository

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class EventSegment(
    val name: String = "",
    val date: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val eventId: String = ""
)