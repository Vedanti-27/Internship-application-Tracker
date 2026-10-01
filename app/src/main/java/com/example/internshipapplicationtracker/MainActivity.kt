package com.example.internshipapplicationtracker

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.util.Calendar
import java.util.Locale

/**
 * Home dashboard for students.
 * Displays application counts (Applied, Running, Completed), features repeating alarm scheduling (every 24h), and alarm cancel option.
 */
class MainActivity : AppCompatActivity() {

    private val defaultCompany = "ABC Technologies"
    private val defaultRole = "Software Development Intern"
    private val defaultLocation = "Pune"
    private val defaultStatus = "Open"

    private lateinit var prefs: SharedPreferences

    private lateinit var tvUserAppliedCount: TextView
    private lateinit var tvUserRunningCount: TextView
    private lateinit var tvUserCompletedCount: TextView

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                showTimePickerDialog()
            } else {
                Toast.makeText(
                    this,
                    "Notification permission denied. Allow it in Settings to receive reminders.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = getSharedPreferences(LoginActivity.PREFS_NAME, Context.MODE_PRIVATE)

        if (!prefs.getBoolean(LoginActivity.KEY_IS_LOGGED_IN, false)) {
            openLoginScreen()
            return
        }

        setContentView(R.layout.activity_main)
        applySystemBarPadding(findViewById(R.id.rootMain))

        tvUserAppliedCount = findViewById(R.id.tvUserAppliedCount)
        tvUserRunningCount = findViewById(R.id.tvUserRunningCount)
        tvUserCompletedCount = findViewById(R.id.tvUserCompletedCount)

        NotificationHelper.createNotificationChannel(this)

        val email = prefs.getString(LoginActivity.KEY_EMAIL, "") ?: ""
        findViewById<TextView>(R.id.tvUserEmail).text = "Logged in as: $email"

        setupButtons()
    }

    override fun onResume() {
        super.onResume()
        updateStudentCounts()
    }

    private fun updateStudentCounts() {
        val userId = prefs.getLong(LoginActivity.KEY_USER_ID, -1L)
        val dbHelper = DatabaseHelper(this)
        val counts = dbHelper.getApplicationCounts(userId)
        dbHelper.close()

        tvUserAppliedCount.text = counts.totalApplied.toString()
        tvUserRunningCount.text = counts.runningCount.toString()
        tvUserCompletedCount.text = counts.completedCount.toString()
    }

    private fun setupButtons() {
        findViewById<Button>(R.id.btnViewDetails).setOnClickListener {
            openDetails()
        }

        findViewById<Button>(R.id.btnBrowse).setOnClickListener {
            startActivity(Intent(this, InternshipListActivity::class.java))
        }

        findViewById<Button>(R.id.btnSetReminder).setOnClickListener {
            onSetReminderClicked()
        }

        findViewById<Button>(R.id.btnCancelReminder).setOnClickListener {
            cancelReminder()
        }

        findViewById<Button>(R.id.btnSavedApplications).setOnClickListener {
            startActivity(Intent(this, ApplicationsActivity::class.java))
        }

        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            logout()
        }
    }

    private fun openDetails() {
        val intent = Intent(this, InternshipDetailsActivity::class.java).apply {
            putExtra(InternshipDetailsActivity.EXTRA_COMPANY, defaultCompany)
            putExtra(InternshipDetailsActivity.EXTRA_ROLE, defaultRole)
            putExtra(InternshipDetailsActivity.EXTRA_LOCATION, defaultLocation)
            putExtra(InternshipDetailsActivity.EXTRA_STATUS, defaultStatus)
        }
        startActivity(intent)
    }

    private fun onSetReminderClicked() {
        val needsPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED

        if (needsPermission) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            showTimePickerDialog()
        }
    }

    private fun showTimePickerDialog() {
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(
            this,
            { _, selectedHour, selectedMinute ->
                scheduleRepeatingReminder(selectedHour, selectedMinute)
            },
            currentHour,
            currentMinute,
            true
        ).show()
    }

    private fun scheduleRepeatingReminder(hour: Int, minute: Int) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DATE, 1)
            }
        }

        val intent = Intent(this, InternshipReminderReceiver::class.java).apply {
            action = InternshipReminderReceiver.ACTION_INTERNSHIP_REMINDER
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            1001,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        alarmManager?.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )

        val timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
        Toast.makeText(
            this,
            "Daily alarm set for $timeFormatted (repeats every 24 hours)",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun cancelReminder() {
        val intent = Intent(this, InternshipReminderReceiver::class.java).apply {
            action = InternshipReminderReceiver.ACTION_INTERNSHIP_REMINDER
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            1001,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        alarmManager?.cancel(pendingIntent)

        Toast.makeText(this, "Daily alarm / reminder canceled", Toast.LENGTH_SHORT).show()
    }

    private fun logout() {
        prefs.edit().clear().apply()
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
        openLoginScreen()
    }

    private fun openLoginScreen() {
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}