package com.damiano.remindme.ui.home

import android.text.TextUtils
import android.util.TypedValue
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.damiano.remindme.R
import com.damiano.remindme.repository.Event
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class CalendarViewHolder(itemView: View, private val onItemListener: CalendarAdapter.OnItemListener, private val days: ArrayList<LocalDate>) :
    RecyclerView.ViewHolder(itemView), View.OnClickListener {

    val parentView: View = itemView.findViewById(R.id.parentView)
    val dayOfMonth: TextView = itemView.findViewById(R.id.cellDayText)
    private val eventIndicatorsLayout: LinearLayout = itemView.findViewById(R.id.eventIndicatorsLayout)

    init {
        itemView.setOnClickListener(this)
    }

    fun bind(date: LocalDate?, events: List<Event>) {
        val context = itemView.context

        val typedValueSecondary = TypedValue()
        context.theme.resolveAttribute(R.attr.colorSecondaryContainer, typedValueSecondary, true)
        val colorSecondary = typedValueSecondary.data

        val typedValueOnSecondaryFixed = TypedValue()
        context.theme.resolveAttribute(R.attr.colorOnSecondaryFixed, typedValueOnSecondaryFixed, true)
        val colorOnSecondaryFixed = typedValueOnSecondaryFixed.data

        val typedValuePrimaryVariant = TypedValue()
        context.theme.resolveAttribute(R.attr.colorPrimaryVariant, typedValuePrimaryVariant, true)
        val colorPrimaryVariant = typedValuePrimaryVariant.data

        date?.let {
            dayOfMonth.text = it.dayOfMonth.toString()

            eventIndicatorsLayout.removeAllViews()

            val dateStr = it.format(DateTimeFormatter.ISO_LOCAL_DATE)
            val eventsForDate = events.filter { event -> event.date == dateStr }
            val maxIndicators = 3

            eventsForDate.take(maxIndicators).forEach { event ->
                val indicator = TextView(context).apply {
                    text = event.name
                    setTextColor(colorSecondary)
                    setBackgroundColor(colorPrimaryVariant)
                    textSize = 10f
                    setPadding(4, 2, 4, 2)
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(2, 1, 2, 1)
                    }
                    maxLines = 1
                    ellipsize = TextUtils.TruncateAt.END
                }
                eventIndicatorsLayout.addView(indicator)
            }

            if (eventsForDate.size > maxIndicators) {
                val moreIndicator = TextView(context).apply {
                    val remainingEvents = eventsForDate.size - maxIndicators
                    val Text = if (remainingEvents == 1) {
                        context.getString(R.string._1_weiteres)
                    } else {
                        context.getString(R.string.weitere, remainingEvents)
                    }
                    text = Text
                    setTextColor(colorOnSecondaryFixed)
                    textSize = 9f
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                }
                eventIndicatorsLayout.addView(moreIndicator)
            }

        }
    }

    override fun onClick(view: View) {
        val position = adapterPosition
        if (position != RecyclerView.NO_POSITION) {
            onItemListener.onItemClick(position, days[position])
        }
    }
}
