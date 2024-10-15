package com.damiano.remindme.ui.add

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.damiano.remindme.business.NotificationReceiver
import com.damiano.remindme.business.Constants.ACTION_EVENT_ADDED
import com.damiano.remindme.databinding.FragmentAddBinding
import com.damiano.remindme.repository.DataRepository
import com.damiano.remindme.repository.Event
import com.damiano.remindme.repository.EventSegment
import com.damiano.remindme.business.HourEventsManager
import com.damiano.remindme.R
import com.google.firebase.auth.FirebaseAuth
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar

class AddFragment : Fragment() {

    private var _binding: FragmentAddBinding? = null
    private val binding get() = _binding!!

    private lateinit var titleEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var dateButton: Button
    private lateinit var timeButtonVon: Button
    private lateinit var timeButtonBis: Button
    private lateinit var ortEditText: EditText
    private lateinit var personEditText: EditText
    private lateinit var switch1: Switch
    private lateinit var switch2: Switch
    private lateinit var switch3: Switch
    private lateinit var switch4: Switch
    private lateinit var switch5: Switch
    private lateinit var switch6: Switch
    private lateinit var saveButton: Button
    private lateinit var deletedButton: Button
    private var selectedDate: LocalDate? = null
    private var selectedTimeVon: LocalTime? = null
    private var selectedTimeBis: LocalTime? = null

    private val dataRepository = DataRepository() // Initialize DataRepository

    companion object {
        private const val ARG_SELECTED_DATE = "selected_date"

        fun newInstance(date: LocalDate): AddFragment {
            val fragment = AddFragment()
            val args = Bundle()
            args.putString(ARG_SELECTED_DATE, date.toString()) // Convert LocalDate to String
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            selectedDate = it.getString(ARG_SELECTED_DATE)?.let { dateStr ->
                LocalDate.parse(dateStr)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddBinding.inflate(inflater, container, false)
        val view = binding.root

        titleEditText = binding.titleEditText
        descriptionEditText = binding.descriptionEditText
        dateButton = binding.dateButton
        timeButtonVon = binding.timeButtonVon
        timeButtonBis = binding.timeButtonBis
        ortEditText = binding.ortEditText
        personEditText = binding.personenEditText
        switch1 = binding.switch1
        switch2 = binding.switch2
        switch3 = binding.switch3
        switch4 = binding.switch4
        switch5 = binding.switch5
        switch6 = binding.switch6
        saveButton = binding.saveEventButton
        deletedButton = binding.deledButton

        dateButton.setOnClickListener { popDatePicker() }
        timeButtonVon.setOnClickListener { popTimePickerVon() }
        timeButtonBis.setOnClickListener { popTimePickerBis() }
        saveButton.setOnClickListener { saveEventAction() }
        deletedButton.setOnClickListener { deleteFieldsAction() }

        selectedDate?.let {
            dateButton.text = it.toString()
        }

        return view
    }

    private fun popDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
            selectedDate = LocalDate.of(selectedYear, selectedMonth + 1, selectedDay)
            dateButton.text = selectedDate.toString()
        }, selectedDate?.year ?: year, selectedDate?.monthValue?.minus(1) ?: month, selectedDate?.dayOfMonth ?: day)
        datePicker.show()
    }

    private fun popTimePickerVon() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePicker = TimePickerDialog(requireContext(), { _, selectedHour, selectedMinute ->
            selectedTimeVon = LocalTime.of(selectedHour, selectedMinute)
            selectedTimeBis = selectedTimeVon?.plusHours(1)
            timeButtonVon.text = selectedTimeVon?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "Select"
            timeButtonBis.text = selectedTimeBis?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "To"
        }, hour, minute, true)
        timePicker.show()
    }

    private fun popTimePickerBis() {
        val calendar = Calendar.getInstance()
        if (selectedTimeVon != null) {
            val hour = selectedTimeVon!!.plusHours(1).hour
            val minute = selectedTimeVon!!.plusHours(1).minute
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, minute)
        } else {
            calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY))
            calendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE))
        }

        val timePicker = TimePickerDialog(requireContext(), { _, selectedHour, selectedMinute ->
            selectedTimeBis = LocalTime.of(selectedHour, selectedMinute)
            timeButtonBis.text = selectedTimeBis?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "Select"
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true)
        timePicker.show()
    }

    private fun showAuthenticationErrorToast() {
        val inflater = layoutInflater
        val layout = inflater.inflate(R.layout.custom_toast, view?.findViewById(R.id.toastText))

        // Imposta il testo del Toast
        val toastText: TextView = layout.findViewById(R.id.toastText)
        toastText.text = getString(R.string.not_authenticated_error)

        // Crea il Toast con il layout personalizzato
        val toast = Toast(requireContext())
        toast.duration = Toast.LENGTH_LONG
        toast.view = layout
        toast.show()
    }

    private fun saveEventAction() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            // Mostra il Toast di errore con il testo rosso
            showAuthenticationErrorToast()
            return
        }



        // Se l'utente è autenticato, continua con il salvataggio dell'evento
        val title = titleEditText.text.toString()
        val description = descriptionEditText.text.toString()
        val ort = ortEditText.text.toString()
        val person = personEditText.text.toString()

        val missingFields = mutableListOf<String>()

        if (title.isEmpty()) missingFields.add(getString(R.string.titel_lable))
        if (selectedDate == null) missingFields.add(getString(R.string.datum_lable))
        if (selectedTimeVon == null) missingFields.add(getString(R.string.startzeit_lable))
        if (selectedTimeBis == null) missingFields.add(getString(R.string.endzeit_lable))

        if (missingFields.isEmpty()) {
            val eventId = dataRepository.generateUniqueId()

            val event = Event(
                eventId = eventId,
                name = title,
                description = description,
                date = selectedDate!!.toString(),
                startTime = selectedTimeVon!!.format(DateTimeFormatter.ISO_LOCAL_TIME),
                endTime = selectedTimeBis!!.format(DateTimeFormatter.ISO_LOCAL_TIME),
                location = ort,
                attendees = person,
                timer5min = switch1.isChecked,
                timer10min = switch2.isChecked,
                timer15min = switch3.isChecked,
                timer30min = switch4.isChecked,
                timer45min = switch5.isChecked,
                timer60min = switch6.isChecked,
            )

            dataRepository.saveEvent(event)

            val segments = splitEventIntoSegments(event)
            segments.forEach { segment ->
                dataRepository.saveEventSegment(segment)
            }

            HourEventsManager.updateHourEventsList(dataRepository)

            setNotificationAlarms(event)

            val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
            val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
            val formattedDate = selectedDate!!.format(dateFormatter)
            val formattedStartTime = selectedTimeVon!!.format(timeFormatter)
            val formattedEndTime = selectedTimeBis!!.format(timeFormatter)

            Toast.makeText(requireContext(),
                getString(
                    R.string.ereignis_am_von_bis_gespeichert,
                    title,
                    formattedDate,
                    formattedStartTime,
                    formattedEndTime
                ), Toast.LENGTH_LONG).show()

            val intent = Intent(ACTION_EVENT_ADDED)
            requireActivity().sendBroadcast(intent)

            requireActivity().onBackPressed()
        } else {
            val message = when (missingFields.size) {
                1 -> getString(R.string.bitte_f_llen_sie_das_folgende_feld_aus, missingFields[0])
                2 -> getString(
                    R.string.bitte_f_llen_sie_die_folgenden_felder_aus_und,
                    missingFields[0],
                    missingFields[1]
                )
                else -> {
                    val lastField = missingFields.removeLast()
                    getString(
                        R.string.bitte_f_llen_sie_die_folgenden_felder_aus_und,
                        missingFields.joinToString(", "),
                        lastField
                    )
                }
            }
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        }
    }
    @SuppressLint("StringFormatMatches")
    fun setNotificationAlarms(event: Event) {
        val context = requireContext()
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val startTime = LocalTime.parse(event.startTime, DateTimeFormatter.ISO_LOCAL_TIME)
        val date = LocalDate.parse(event.date, DateTimeFormatter.ISO_LOCAL_DATE)
        val eventDateTime = LocalDateTime.of(date, startTime)

        val timeOffsets = listOf(
            5 to event.timer5min,
            10 to event.timer10min,
            15 to event.timer15min,
            30 to event.timer30min,
            45 to event.timer45min,
            60 to event.timer60min
        )

        timeOffsets.forEach { (minutesBefore, isChecked) ->
            if (isChecked) {
                val notificationTime = eventDateTime.minusMinutes(minutesBefore.toLong())
                if (notificationTime.isAfter(LocalDateTime.now())) {
                    val intent = Intent(context, NotificationReceiver::class.java).apply {
                        putExtra("title", "Event Reminder")
                        putExtra("message",
                            getString(R.string.startet_in_minuten, event.name, minutesBefore))
                    }

                    val pendingIntent = PendingIntent.getBroadcast(
                        context,
                        System.currentTimeMillis().toInt(),
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        notificationTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                        pendingIntent
                    )
                }
            }
        }
    }


    private fun deleteFieldsAction() {
        titleEditText.text.clear()
        descriptionEditText.text.clear()
        dateButton.text = getString(R.string.datum_festlegen)
        timeButtonVon.text = getString(R.string.von)
        timeButtonBis.text = getString(R.string.bis)
        ortEditText.text.clear()
        personEditText.text.clear()
        switch1.isChecked = false
        switch2.isChecked = false
        switch3.isChecked = false
        switch4.isChecked = false
        switch5.isChecked = false
        switch6.isChecked = false
    }

    private fun splitEventIntoSegments(event: Event): List<EventSegment> {
        val segments = mutableListOf<EventSegment>()
        val startDateTime = LocalDateTime.of(LocalDate.parse(event.date), LocalTime.parse(event.startTime))
        val endDateTime = LocalDateTime.of(LocalDate.parse(event.date), LocalTime.parse(event.endTime))

        var segmentStartTime = startDateTime

        while (segmentStartTime.isBefore(endDateTime)) {
            val currentHourEndTime = segmentStartTime.withMinute(0).withSecond(0).withNano(0).plusHours(1)
            val segmentEndTime = if (currentHourEndTime.isBefore(endDateTime)) currentHourEndTime else endDateTime

            segments.add(
                EventSegment(
                    name = event.name,
                    date = event.date,
                    startTime = segmentStartTime.toLocalTime().format(DateTimeFormatter.ISO_LOCAL_TIME),
                    endTime = segmentEndTime.toLocalTime().format(DateTimeFormatter.ISO_LOCAL_TIME),
                    eventId = event.eventId
                )
            )

            segmentStartTime = currentHourEndTime
        }

        return segments
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
