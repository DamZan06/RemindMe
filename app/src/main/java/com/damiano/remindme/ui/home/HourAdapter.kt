package com.damiano.remindme.ui.home

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.damiano.remindme.R
import com.damiano.remindme.business.CalendarUtils
import com.damiano.remindme.repository.EventSegment
import com.damiano.remindme.repository.HourEvent
import java.time.LocalTime

class HourAdapter(context: Context, hourEvents: List<HourEvent>) :
    ArrayAdapter<HourEvent>(context, 0, hourEvents) {

    private val displayMetrics = context.resources.displayMetrics
    private val screenWidth = displayMetrics.widthPixels
    private val timeColumnWidth = (70 * displayMetrics.density).toInt()
    private val eventMargin = (4 * displayMetrics.density).toInt()
    private val columnWidth = (screenWidth - timeColumnWidth - 6 * eventMargin) / 3

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val itemView = convertView ?: LayoutInflater.from(context).inflate(R.layout.hour_cell, parent, false)
        val hourEvent = getItem(position)

        hourEvent?.let {
            setHour(itemView, it.time)
            setEvents(itemView, it.events)
        }

        return itemView
    }


    private fun setHour(convertView: View, time: LocalTime) {
        val timeTV = convertView.findViewById<TextView>(R.id.timeTV)
        timeTV.text = CalendarUtils.formattedShortTime(time)
    }


    private fun createEventView(event: EventSegment): TextView {
        return TextView(context).apply {
            id = View.generateViewId()
            text = event.name
            setBackgroundColor(context.getColor(R.color.purple_500))
            setTextColor(context.getColor(R.color.white))
            textSize = 12f
            setPadding(8, 8, 8, 8)
        }
    }
    private val eventIdToColumnMap = mutableMapOf<String, Int>()

    private fun setEvents(convertView: View, events: List<EventSegment>) {
        val constraintLayout = convertView.findViewById<ConstraintLayout>(R.id.constraintLayout)

        for (i in constraintLayout.childCount - 1 downTo 1) {
            constraintLayout.removeViewAt(i)
        }

        val eventGroups = events.groupBy { it.eventId }

        eventGroups.forEach { (eventId, segments) ->
            val sortedSegments = segments.sortedBy { LocalTime.parse(it.startTime) }

            val eventView = createEventView(sortedSegments.first())
            val columnIndex = getColumnForEvent(eventId)
            constraintLayout.addView(eventView)

            positionEvent(eventView, sortedSegments.first(), sortedSegments.last(), columnIndex)
        }
    }

    private fun getColumnForEvent(eventId: String): Int {
        return eventIdToColumnMap.computeIfAbsent(eventId) { findFreeColumn() }
    }

    private fun findFreeColumn(): Int {
        for (i in 0 until 3) {
            if (eventIdToColumnMap.values.none { it == i }) {
                return i
            }
        }
        return 0
    }

    private fun positionEvent(eventView: TextView, firstSegment: EventSegment, lastSegment: EventSegment, columnIndex: Int) {
        val constraintSet = ConstraintSet()
        constraintSet.clone(eventView.parent as ConstraintLayout)

        val columnStartX = timeColumnWidth + (columnIndex * (columnWidth + eventMargin))


        val startTime = LocalTime.parse(firstSegment.startTime)
        val endTime = LocalTime.parse(lastSegment.endTime)
        val topMargin = calculateMarginBasedOnTime(startTime)
        val height = calculateHeightBasedOnDuration(startTime, endTime)

        constraintSet.connect(eventView.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, topMargin)
        constraintSet.connect(eventView.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, columnStartX)

        constraintSet.constrainWidth(eventView.id, columnWidth)
        constraintSet.constrainHeight(eventView.id, height)

        constraintSet.applyTo(eventView.parent as ConstraintLayout)

    }

    private fun calculateMarginBasedOnTime(time: LocalTime): Int {
        val minutesPerHour = 60
        val pixelsPerHour = 180
        val minutesPastHour = time.minute
        return (minutesPastHour.toFloat() / minutesPerHour * pixelsPerHour).toInt()
    }

    private fun calculateHeightBasedOnDuration(startTime: LocalTime, endTime: LocalTime): Int {
        val pixelsPerHour = 180
        val startMinutes = startTime.hour * 60 + startTime.minute
        val endMinutes = endTime.hour * 60 + endTime.minute
        val durationMinutes = endMinutes - startMinutes
        return (durationMinutes.toFloat() / 60 * pixelsPerHour).toInt()
    }
}