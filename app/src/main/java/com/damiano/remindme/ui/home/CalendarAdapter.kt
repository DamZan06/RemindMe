package com.damiano.remindme.ui.home

import android.graphics.Color
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.damiano.remindme.R
import com.damiano.remindme.business.CalendarUtils
import com.damiano.remindme.repository.DataRepository
import com.damiano.remindme.repository.Event
import java.time.LocalDate

class CalendarAdapter(
    private val days: ArrayList<LocalDate>,
    private val onItemListener: OnItemListener
) : RecyclerView.Adapter<CalendarViewHolder>() {

    private val dataRepository = DataRepository()
    private var events: List<Event> = emptyList()

    init {
        dataRepository.getEvents { eventList ->
            events = eventList
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.calendar_cell, parent, false)
        val layoutParams = view.layoutParams

        if (days.size > 15) {
            layoutParams.height = (parent.height * 0.1666666).toInt()
        } else {
            layoutParams.height = parent.height
        }

        return CalendarViewHolder(view, onItemListener, days)
    }

    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        val date = days[position]
        holder.bind(date, events)

        val context = holder.itemView.context

        val typedValueOnSecondary = TypedValue()
        context.theme.resolveAttribute(com.google.android.material.R.attr.colorOnSecondary, typedValueOnSecondary, true)
        val colorOnSecondary = typedValueOnSecondary.data

        val typedValueOnTertiary = TypedValue()
        context.theme.resolveAttribute(com.google.android.material.R.attr.colorOnTertiary, typedValueOnTertiary, true)
        val colorOnTertiary = typedValueOnTertiary.data

        if (date == CalendarUtils.selectedDate) {
            holder.parentView.setBackgroundColor(colorOnTertiary)
        } else {
            holder.parentView.setBackgroundColor(Color.TRANSPARENT)
        }

        if (date.month == CalendarUtils.selectedDate?.month) {
            holder.dayOfMonth.setTextColor(colorOnSecondary)
        } else {
            holder.dayOfMonth.setTextColor(colorOnTertiary)
        }
    }

    override fun getItemCount(): Int {
        return days.size
    }

    interface OnItemListener {
        fun onItemClick(position: Int, date: LocalDate)
    }
}
