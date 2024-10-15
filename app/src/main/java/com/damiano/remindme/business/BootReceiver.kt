package com.damiano.remindme.business

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.damiano.remindme.repository.DataRepository
import com.damiano.remindme.ui.add.AddFragment

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            if (context != null) {
                val dataRepository = DataRepository()
                dataRepository.getAllEvents { events ->
                    events.forEach { event ->
                        val fragment = AddFragment()
                        fragment.setNotificationAlarms(event)
                    }
                }
            }
        }
    }
}
