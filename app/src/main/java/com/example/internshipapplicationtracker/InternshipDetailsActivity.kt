package com.example.internshipapplicationtracker

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.telephony.SmsManager
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.MediaController
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

/**
 * Details screen for internships.
 * Displays company details, contacts, promotional video, and allows students to save/apply.
 */
class InternshipDetailsActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "InternshipDetailsActivity"

        const val EXTRA_COMPANY = "extra_company"
        const val EXTRA_ROLE = "extra_role"
        const val EXTRA_LOCATION = "extra_location"
        const val EXTRA_STATUS = "extra_status"
        const val EXTRA_CATEGORY = "extra_category"
        const val EXTRA_DESCRIPTION = "extra_description"
        const val EXTRA_PHONE = "extra_phone"
        const val EXTRA_VIDEO_URL = "extra_video_url"
    }

    private lateinit var imgBanner: ImageView
    private lateinit var imgLogo: ImageView
    private lateinit var tvLogoLetter: TextView
    private lateinit var tvVideoPlaceholder: TextView
    private lateinit var videoView: VideoView

    private var company = ""
    private var role = ""
    private var location = ""
    private var status = ""
    private var category = ""
    private var description = ""
    private var phoneNumber = ""
    private var videoUrl = ""

    private val smsPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                sendSms()
            } else {
                Toast.makeText(
                    this,
                    "SMS permission denied. Opening messaging app instead.",
                    Toast.LENGTH_LONG
                ).show()
                openSmsApp()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")

        setContentView(R.layout.activity_internship_details)
        applySystemBarPadding(findViewById(R.id.rootDetails))

        // Read Intent extras
        company = intent.getStringExtra(EXTRA_COMPANY) ?: "ABC Technologies"
        role = intent.getStringExtra(EXTRA_ROLE) ?: "Software Development Intern"
        location = intent.getStringExtra(EXTRA_LOCATION) ?: "Pune"
        status = intent.getStringExtra(EXTRA_STATUS) ?: "Open"
        category = intent.getStringExtra(EXTRA_CATEGORY) ?: "Software Development"
        description = intent.getStringExtra(EXTRA_DESCRIPTION) ?: "Work on real software projects."
        phoneNumber = intent.getStringExtra(EXTRA_PHONE) ?: "5554"
        videoUrl = intent.getStringExtra(EXTRA_VIDEO_URL) ?: ""

        imgBanner = findViewById(R.id.imgBanner)
        imgLogo = findViewById(R.id.imgLogo)
        tvLogoLetter = findViewById(R.id.tvLogoLetter)
        tvVideoPlaceholder = findViewById(R.id.tvVideoPlaceholder)
        videoView = findViewById(R.id.videoView)

        showInternshipInfo()
        loadImages()
        setupButtons()
    }

    override fun onPause() {
        super.onPause()
        if (::videoView.isInitialized && videoView.isPlaying) {
            videoView.pause()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::videoView.isInitialized) {
            videoView.stopPlayback()
        }
    }

    private fun showInternshipInfo() {
        findViewById<TextView>(R.id.tvCompanyName).text = company
        findViewById<TextView>(R.id.tvRole).text = role
        findViewById<TextView>(R.id.tvStatus).text = "Application Status: $status"
        findViewById<TextView>(R.id.tvLocation).text = "Location: $location"
        findViewById<TextView>(R.id.tvCategory).text = "Category: $category"
        findViewById<TextView>(R.id.tvContact).text = "Contact number: $phoneNumber"
        findViewById<TextView>(R.id.tvDescription).text = description
    }

    private fun loadImages() {
        val bannerId = resources.getIdentifier("internship_banner", "drawable", packageName)
        if (bannerId != 0) {
            imgBanner.setImageResource(bannerId)
        }

        val logoId = resources.getIdentifier("company_logo", "drawable", packageName)
        if (logoId != 0) {
            imgLogo.setImageResource(logoId)
            tvLogoLetter.visibility = View.GONE
        } else {
            tvLogoLetter.text = company.take(1).uppercase()
        }
    }

    private fun setupButtons() {
        findViewById<Button>(R.id.btnCall).setOnClickListener { callCompany() }
        findViewById<Button>(R.id.btnSms).setOnClickListener { onSendSmsClicked() }
        findViewById<Button>(R.id.btnVideo).setOnClickListener { playVideo() }
        findViewById<Button>(R.id.btnApply).setOnClickListener { saveApplication() }
    }

    private fun callCompany() {
        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
        try {
            startActivity(dialIntent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "No dialer app found on this device", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onSendSmsClicked() {
        val granted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED

        if (granted) {
            sendSms()
        } else {
            smsPermissionLauncher.launch(Manifest.permission.SEND_SMS)
        }
    }

    @Suppress("DEPRECATION")
    private fun sendSms() {
        val message = "Hello, I am interested in the $role position at $company."
        try {
            val smsManager: SmsManager? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                getSystemService(SmsManager::class.java)
            } else {
                SmsManager.getDefault()
            }

            if (smsManager == null) {
                Toast.makeText(this, "SMS is not supported on this device", Toast.LENGTH_SHORT).show()
                return
            }

            smsManager.sendTextMessage(phoneNumber, null, message, null, null)
            Toast.makeText(this, "SMS sent to $phoneNumber", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to send SMS: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun openSmsApp() {
        val message = "Hello, I am interested in the $role position at $company."
        val smsIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$phoneNumber"))
        smsIntent.putExtra("sms_body", message)
        try {
            startActivity(smsIntent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "No messaging app found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun playVideo() {
        tvVideoPlaceholder.visibility = View.GONE
        videoView.visibility = View.VISIBLE

        val videoUri: Uri = if (videoUrl.isNotBlank()) {
            Uri.parse(videoUrl)
        } else {
            val rawId = resources.getIdentifier("internship_video", "raw", packageName)
            if (rawId != 0) {
                Uri.parse("android.resource://$packageName/$rawId")
            } else {
                tvVideoPlaceholder.visibility = View.VISIBLE
                videoView.visibility = View.GONE
                tvVideoPlaceholder.text = "Promotional video not available for this company."
                Toast.makeText(this, "No video available", Toast.LENGTH_SHORT).show()
                return
            }
        }

        val controller = MediaController(this)
        controller.setAnchorView(videoView)
        videoView.setMediaController(controller)
        videoView.setVideoURI(videoUri)

        videoView.setOnPreparedListener {
            videoView.start()
        }
        videoView.setOnErrorListener { _, _, _ ->
            Toast.makeText(this, "Sorry, this video cannot be played", Toast.LENGTH_LONG).show()
            true
        }
    }

    private fun saveApplication() {
        val prefs = getSharedPreferences(LoginActivity.PREFS_NAME, Context.MODE_PRIVATE)
        val userId = prefs.getLong(LoginActivity.KEY_USER_ID, -1L)

        val dbHelper = DatabaseHelper(this)
        val result = dbHelper.saveInternshipApplication(
            userId = userId,
            companyName = company,
            role = role,
            location = location,
            category = category,
            status = "Applied"
        )
        dbHelper.close()

        val message = when (result) {
            SaveResult.SAVED -> "Application saved successfully!"
            SaveResult.ALREADY_SAVED -> "You have already saved this application."
            SaveResult.FAILED -> "Could not save application."
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}