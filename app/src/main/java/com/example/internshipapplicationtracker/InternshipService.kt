package com.example.internshipapplicationtracker

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Toast

/**
 * Experiment 4: a started Service.
 *
 * It is started from MainActivity while the app is on screen, so a normal Service is allowed
 * on every Android version. It does a small job (5 steps, 2 seconds apart, visible in Logcat)
 * and then stops itself.
 */
class InternshipService : Service() {

    companion object {
        private const val TAG = "InternshipService"
        private const val TOTAL_STEPS = 5
        private const val STEP_DELAY_MS = 2000L
    }

    private val handler = Handler(Looper.getMainLooper())
    private var stepCount = 0
    private var isWorking = false

    private val stepRunnable = object : Runnable {
        override fun run() {
            stepCount++
            Log.d(TAG, "Internship service working... step $stepCount of $TOTAL_STEPS")

            if (stepCount >= TOTAL_STEPS) {
                stopSelf()
            } else {
                handler.postDelayed(this, STEP_DELAY_MS)
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null // not a bound service
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate: service created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand: service started")

        if (!isWorking) {
            isWorking = true
            stepCount = 0
            handler.postDelayed(stepRunnable, STEP_DELAY_MS)
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        handler.removeCallbacks(stepRunnable)
        Log.d(TAG, "onDestroy: service stopped")
        Toast.makeText(applicationContext, "Internship service stopped", Toast.LENGTH_SHORT).show()
        super.onDestroy()
    }
}