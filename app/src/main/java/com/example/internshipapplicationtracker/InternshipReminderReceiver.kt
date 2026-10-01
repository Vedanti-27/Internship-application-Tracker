package com.example.internshipapplicationtracker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

/**
 * Experiment 5: Broadcast Receiver.
 * MainActivity sends a broadcast; this class receives it and shows the notification.
 */
class InternshipReminderReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_INTERNSHIP_REMINDER =
            "com.example.internshipapplicationtracker.ACTION_INTERNSHIP_REMINDER"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_INTERNSHIP_REMINDER) {
            val shown = NotificationHelper.showReminderNotification(context)
            if (!shown) {
                Toast.makeText(
                    context,
                    "Notification permission is not granted",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}