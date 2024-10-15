package com.damiano.remindme.ui.event

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.damiano.remindme.databinding.FragmentEventBinding
import com.damiano.remindme.repository.DataRepository
import com.damiano.remindme.business.Constants
import com.damiano.remindme.ui.event.EventAdapterList

class EventFragment : Fragment() {

    private var _binding: FragmentEventBinding? = null
    private val binding get() = _binding!!
    private lateinit var eventAdapterList: EventAdapterList
    private val dataRepository = DataRepository()

    private val eventReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Constants.ACTION_EVENT_ADDED, Constants.ACTION_EVENT_DELETED -> updateEventList()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEventBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupEventList()
        registerEventReceiver()
    }

    private fun setupEventList() {
        eventAdapterList = EventAdapterList(requireContext(), mutableListOf())
        binding.eventListView.adapter = eventAdapterList
        updateEventList()
    }

    private fun updateEventList() {
        dataRepository.getEvents { events ->
            val sortedEvents = events.sortedBy { it.startTime }
            requireActivity().runOnUiThread {
                eventAdapterList.clear()
                eventAdapterList.addAll(sortedEvents)
                eventAdapterList.notifyDataSetChanged()
            }
        }
    }

    private fun registerEventReceiver() {
        val filter = IntentFilter().apply {
            addAction(Constants.ACTION_EVENT_ADDED)
            addAction(Constants.ACTION_EVENT_DELETED)
        }
        requireActivity().registerReceiver(eventReceiver, filter)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().unregisterReceiver(eventReceiver)
        _binding = null
    }
}
