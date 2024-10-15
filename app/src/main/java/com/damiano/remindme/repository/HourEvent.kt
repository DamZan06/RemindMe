package com.damiano.remindme.repository

import java.time.LocalTime

data class HourEvent(val time: LocalTime, val events: List<EventSegment>)
