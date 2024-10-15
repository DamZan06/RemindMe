package com.damiano.remindme.business

import com.damiano.remindme.repository.DataRepository
import com.damiano.remindme.repository.EventSegment
import com.damiano.remindme.repository.HourEvent
import java.time.LocalTime

object HourEventsManager {
    private val hourEventsList = mutableListOf<HourEvent>()

    fun updateHourEventsList(dataRepository: DataRepository) {
        dataRepository.getEventSegments { segments ->
            val hourEventsMap = mutableMapOf<LocalTime, MutableList<EventSegment>>()

            for (segment in segments) {
                val hourStart = LocalTime.parse(segment.startTime).withMinute(0).withSecond(0)
                hourEventsMap.getOrPut(hourStart) { mutableListOf() }.add(segment)
            }

            hourEventsList.clear()
            hourEventsList.addAll(hourEventsMap.entries.sortedBy { it.key }
                .map { HourEvent(it.key, it.value) })
        }
    }
}
