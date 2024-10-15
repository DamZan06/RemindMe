package com.damiano.remindme.ui.home

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.damiano.remindme.R
import com.damiano.remindme.business.Constants
import com.damiano.remindme.repository.DataRepository
import com.damiano.remindme.business.DialogUtils
import com.damiano.remindme.repository.Event

class EventAdapter(context: Context, private val events: MutableList<Event>) : ArrayAdapter<Event>(context, 0, events) {

    private val dataRepository = DataRepository()

    private fun removeEvent(event: Event) {
        events.remove(event)
        notifyDataSetChanged()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val itemView = convertView ?: LayoutInflater.from(context).inflate(R.layout.event_list_item, parent, false)

        val event = getItem(position)

        val titleTV: TextView = itemView.findViewById(R.id.eventTitleTV)
        val timeTV: TextView = itemView.findViewById(R.id.eventTimeTV)
        val locationTV: TextView = itemView.findViewById(R.id.eventLocationTV)
        val descriptionTV: TextView = itemView.findViewById(R.id.eventDescriptionTV)
        val attendeesTV: TextView = itemView.findViewById(R.id.eventAttendeesTV)
        val deleteButton: Button = itemView.findViewById(R.id.deleteEventButton)
        val changeButton: Button = itemView.findViewById(R.id.changeEventButton)

        titleTV.text = event?.name ?: ""
        timeTV.text = event?.startTime?.let { start ->
            event?.endTime?.let { end ->
                context.getString(R.string.zeit_von, start.substring(0, 5), end.substring(0, 5))
            }
        } ?: "Zeit: N/A"
        locationTV.text = if (event?.location.isNullOrEmpty()) "" else context.getString(
            R.string.ort_,
            event?.location
        )
        descriptionTV.text = if (event?.description.isNullOrEmpty()) "" else context.getString(
            R.string.beschreibung_,
            event?.description
        )
        attendeesTV.text = if (event?.attendees.isNullOrEmpty()) "" else context.getString(
            R.string.personen_,
            event?.attendees
        )

        val additionalDetailsVisible = event?.isExpanded ?: false
        locationTV.visibility = if (additionalDetailsVisible && locationTV.text.isNotEmpty()) View.VISIBLE else View.GONE
        descriptionTV.visibility = if (additionalDetailsVisible && descriptionTV.text.isNotEmpty()) View.VISIBLE else View.GONE
        attendeesTV.visibility = if (additionalDetailsVisible && attendeesTV.text.isNotEmpty()) View.VISIBLE else View.GONE
        deleteButton.visibility = if (additionalDetailsVisible) View.VISIBLE else View.GONE
        changeButton.visibility = if (additionalDetailsVisible) View.VISIBLE else View.GONE

        itemView.setOnClickListener {
            event?.isExpanded = !(event?.isExpanded ?: false)
            notifyDataSetChanged()
        }

        deleteButton.setOnClickListener {
            event?.let { eventToDelete ->
                DialogUtils.showDeleteConfirmationDialog(context) {
                    dataRepository.deleteEvent(eventToDelete.eventId) { success ->
                        if (success) {
                            removeEvent(eventToDelete)
                            val intent = Intent(Constants.ACTION_EVENT_DELETED)
                            context.sendBroadcast(intent)
                        }
                    }
                }
            }
        }

        changeButton.setOnClickListener {
            event?.let {
                // Show a toast message indicating that editing is not possible
                Toast.makeText(
                    context,
                    context.getString(R.string.updating_an_event),
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        return itemView
    }
}
