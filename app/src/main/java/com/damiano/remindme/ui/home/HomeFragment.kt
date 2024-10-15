package com.damiano.remindme.ui.home

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.damiano.remindme.R
import com.damiano.remindme.business.CalendarUtils
import com.damiano.remindme.business.Constants
import com.damiano.remindme.ui.home.CalendarAdapter.OnItemListener
import java.time.LocalDate

class HomeFragment : Fragment(), OnItemListener {

    private lateinit var monthYearText: TextView
    private lateinit var calendarRecyclerView: RecyclerView
    private lateinit var calendarAdapter: CalendarAdapter
    private val eventReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            setMonthView()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        initWidgets(view)
        CalendarUtils.selectedDate = LocalDate.now()
        setMonthView()

        val filter = IntentFilter(Constants.ACTION_EVENT_ADDED)
        requireContext().registerReceiver(eventReceiver, filter)

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireContext().unregisterReceiver(eventReceiver)
    }

    private fun initWidgets(view: View) {
        calendarRecyclerView = view.findViewById(R.id.calendarRecyclerView)
        monthYearText = view.findViewById(R.id.monthYearTV)
        view.findViewById<Button>(R.id.previousMonthButton).setOnClickListener { previousMonthAction() }
        view.findViewById<Button>(R.id.nextMonthButton).setOnClickListener { nextMonthAction() }
        view.findViewById<Button>(R.id.weeklyButton).setOnClickListener { weeklyAction() }
        view.findViewById<Button>(R.id.newEventButtonMonthly).setOnClickListener { newEventAction() }
    }

    private fun setMonthView() {
        monthYearText.text = CalendarUtils.monthYearFromDate(CalendarUtils.selectedDate!!, requireContext())
        val daysInMonth = CalendarUtils.daysInMonthArray()
        calendarAdapter = CalendarAdapter(daysInMonth, this)
        calendarRecyclerView.layoutManager = GridLayoutManager(context, 7)
        calendarRecyclerView.adapter = calendarAdapter
    }


    private fun previousMonthAction() {
        CalendarUtils.selectedDate = CalendarUtils.selectedDate!!.minusMonths(1)
        setMonthView()
    }

    private fun nextMonthAction() {
        CalendarUtils.selectedDate = CalendarUtils.selectedDate!!.plusMonths(1)
        setMonthView()
    }

    override fun onItemClick(position: Int, date: LocalDate) {
        date?.let {
            CalendarUtils.selectedDate = it
            setMonthView()
        }
    }

    private fun weeklyAction() {
        startActivity(Intent(activity, WeekViewActivity::class.java))
    }

    private fun newEventAction() {
        findNavController().navigate(R.id.action_homeFragment_to_navigation_add)
    }
}
