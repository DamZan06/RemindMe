package com.damiano.remindme.ui.home

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.damiano.remindme.R
import com.damiano.remindme.business.CalendarUtils
import com.damiano.remindme.business.Constants
import com.damiano.remindme.repository.DataRepository
import com.damiano.remindme.ui.add.AddFragment
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class WeekViewActivity : AppCompatActivity(), CalendarAdapter.OnItemListener {

    private lateinit var monthYearText: TextView
    private lateinit var calendarRecyclerView: RecyclerView
    private lateinit var eventListView: ListView
    private val dataRepository = DataRepository()

    private val eventReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Constants.ACTION_EVENT_ADDED, Constants.ACTION_EVENT_DELETED -> updateWeekView()
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_week_view)
        initWidgets()

        if (CalendarUtils.selectedDate == null) {
            CalendarUtils.selectedDate = LocalDate.now()
        }

        setWeekView()

        val filter = IntentFilter().apply {
            addAction(Constants.ACTION_EVENT_ADDED)
            addAction(Constants.ACTION_EVENT_DELETED)
        }
        registerReceiver(eventReceiver, filter)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(eventReceiver)
    }

    private fun initWidgets() {
        calendarRecyclerView = findViewById(R.id.calendarRecyclerView)
        monthYearText = findViewById(R.id.monthYearTV)
        eventListView = findViewById(R.id.eventListView)
    }

    private fun setWeekView() {
        val selectedDate = CalendarUtils.selectedDate ?: LocalDate.now()
        val days = CalendarUtils.daysInWeekArray(selectedDate)
        val calendarAdapter = CalendarAdapter(days, this)
        calendarRecyclerView.layoutManager = GridLayoutManager(this, 7)
        calendarRecyclerView.adapter = calendarAdapter
        monthYearText.text = CalendarUtils.monthYearFromDate(selectedDate, this)
        updateEventListView(selectedDate)
    }

    private fun updateWeekView() {
        setWeekView()
        updateEventListView(CalendarUtils.selectedDate ?: LocalDate.now())
    }

    private fun updateEventListView(date: LocalDate) {
        val formattedDate = date.format(DateTimeFormatter.ISO_DATE)
        dataRepository.getEventsForDate(formattedDate) { events ->
            val sortedEvents = events.sortedBy { it.startTime }
            runOnUiThread {
                val eventAdapterWeek = EventAdapter(this, sortedEvents.toMutableList())
                eventListView.adapter = eventAdapterWeek
            }
        }
    }


    override fun onItemClick(position: Int, date: LocalDate) {
        CalendarUtils.selectedDate = date
        setWeekView()
        updateEventListView(date)
    }

    fun previousWeekAction(view: View) {
        CalendarUtils.selectedDate = CalendarUtils.selectedDate!!.minusWeeks(1)
        setWeekView()
    }

    fun nextWeekAction(view: View) {
        CalendarUtils.selectedDate = CalendarUtils.selectedDate!!.plusWeeks(1)
        setWeekView()
    }

    fun dailyAction(view: View) {
        startActivity(Intent(this, DailyCalendarActivity::class.java))
    }

    fun newEventAction(view: View) {
        val date = CalendarUtils.selectedDate ?: return
        val fragment = AddFragment.newInstance(date)
        supportFragmentManager.beginTransaction()
            .replace(android.R.id.content, fragment)
            .addToBackStack(null)
            .commit()
    }
}
