package com.example.internshipapplicationtracker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * System Broadcast Receiver:
 * Listens for device boot completion (BOOT_COMPLETED) to re-initialize notification channels and alarms upon reboot.
 */
class BootCompletedReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootCompletedReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            Log.d(TAG, "Device boot completed. Re-initializing notification channel.")
            NotificationHelper.createNotificationChannel(context)
        }
    }
}