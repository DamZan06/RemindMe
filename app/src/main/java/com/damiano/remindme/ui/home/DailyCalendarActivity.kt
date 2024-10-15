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
import com.damiano.remindme.R
import com.damiano.remindme.business.CalendarUtils
import com.damiano.remindme.repository.DataRepository
import com.damiano.remindme.ui.add.AddFragment
import com.damiano.remindme.business.Constants.ACTION_EVENT_ADDED
import com.damiano.remindme.repository.EventSegment
import com.damiano.remindme.repository.HourEvent
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.TextStyle
import java.util.Locale

class DailyCalendarActivity : AppCompatActivity() {

    private lateinit var monthDayText: TextView
    private lateinit var dayOfWeekTV: TextView
    private lateinit var hourListView: ListView
    private val dataRepository = DataRepository()
    private var events: List<EventSegment> = emptyList()
    private val eventReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            updateEvents()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_daily_calendar)
        initWidgets()

        if (CalendarUtils.selectedDate == null) {
            CalendarUtils.selectedDate = LocalDate.now()
        }

        val filter = IntentFilter(ACTION_EVENT_ADDED)
        registerReceiver(eventReceiver, filter)

        loadEvents()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(eventReceiver)
    }

    override fun onResume() {
        super.onResume()
        loadEvents()
    }

    private fun initWidgets() {
        monthDayText = findViewById(R.id.monthDayText)
        dayOfWeekTV = findViewById(R.id.dayOfWeekTV)
        hourListView = findViewById(R.id.hourListView)
    }

    private fun setDayView() {
        monthDayText.text = CalendarUtils.monthDayFromDate(CalendarUtils.selectedDate!!, this) // Passa 'this' come Context

        // Recupera la lingua salvata dalle impostazioni
        val sharedPreferences = getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val savedLanguage = sharedPreferences.getString("App_Language", "en") ?: "en"
        val locale = Locale(savedLanguage)

        // Ottieni il nome del giorno della settimana nella lingua selezionata
        val dayOfWeek = CalendarUtils.selectedDate!!.dayOfWeek.getDisplayName(TextStyle.FULL, locale)
        dayOfWeekTV.text = dayOfWeek
        setHourAdapter()
    }



    private fun setHourAdapter() {
        val hourAdapter = HourAdapter(applicationContext, hourEventList())
        hourListView.adapter = hourAdapter
    }

    private fun hourEventList(): ArrayList<HourEvent> {
        val list = ArrayList<HourEvent>()

        for (hour in 0 until 24) {
            val time = LocalTime.of(hour, 0)
            val eventsForHour = events.filter {
                it.date == CalendarUtils.selectedDate.toString() &&
                        LocalTime.parse(it.startTime) == time
            }
            val hourEvent = HourEvent(time, eventsForHour)
            list.add(hourEvent)
        }
        return list
    }

    private fun loadEvents() {
        CalendarUtils.selectedDate?.let { selectedDate ->
            dataRepository.getEventSegments { eventSegments ->
                events = eventSegments
                setDayView()
            }
        }
    }

    fun previousDayAction(view: View) {
        CalendarUtils.selectedDate = CalendarUtils.selectedDate!!.minusDays(1)
        setDayView()
    }

    fun nextDayAction(view: View) {
        CalendarUtils.selectedDate = CalendarUtils.selectedDate!!.plusDays(1)
        setDayView()
    }

    fun newEventAction(view: View) {
        val date = CalendarUtils.selectedDate ?: return
        val fragment = AddFragment.newInstance(date)
        supportFragmentManager.beginTransaction()
            .replace(android.R.id.content, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun updateEvents() {
        loadEvents()
    }
}
