package com.example.internshipapplicationtracker

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

/**
 * Experiment 7: reads saved applications from SQLite and shows them.
 * Also supports deleting applications and filtering by status (All, Running, Completed).
 */
class ApplicationsActivity : AppCompatActivity() {

    private lateinit var container: LinearLayout
    private lateinit var tvEmpty: TextView
    private lateinit var btnFilterAll: Button
    private lateinit var btnFilterRunning: Button
    private lateinit var btnFilterCompleted: Button

    private var currentFilter = "ALL" // "ALL", "Running", "Completed"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_applications)
        applySystemBarPadding(findViewById(R.id.rootApplications))

        container = findViewById(R.id.containerApplications)
        tvEmpty = findViewById(R.id.tvEmpty)

        btnFilterAll = findViewById(R.id.btnFilterAll)
        btnFilterRunning = findViewById(R.id.btnFilterRunning)
        btnFilterCompleted = findViewById(R.id.btnFilterCompleted)

        btnFilterAll.setOnClickListener {
            currentFilter = "ALL"
            loadApplications()
        }

        btnFilterRunning.setOnClickListener {
            currentFilter = "Running"
            loadApplications()
        }

        btnFilterCompleted.setOnClickListener {
            currentFilter = "Completed"
            loadApplications()
        }

        findViewById<Button>(R.id.btnBackHome).setOnClickListener {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        loadApplications()
    }

    private fun loadApplications() {
        val prefs = getSharedPreferences(LoginActivity.PREFS_NAME, Context.MODE_PRIVATE)
        val userId = prefs.getLong(LoginActivity.KEY_USER_ID, -1L)

        val dbHelper = DatabaseHelper(this)
        val allApplications = dbHelper.getApplications(userId)
        dbHelper.close()

        val filteredList = when (currentFilter) {
            "Running" -> allApplications.filter { it.status.equals("Running", ignoreCase = true) || it.status.equals("In Progress", ignoreCase = true) }
            "Completed" -> allApplications.filter { it.status.equals("Completed", ignoreCase = true) }
            else -> allApplications
        }

        container.removeAllViews()

        if (filteredList.isEmpty()) {
            tvEmpty.visibility = android.view.View.VISIBLE
            tvEmpty.text = if (allApplications.isEmpty()) {
                "No applications saved yet."
            } else {
                "No applications found for $currentFilter filter."
            }
            return
        }

        tvEmpty.visibility = android.view.View.GONE

        val inflater = LayoutInflater.from(this)
        for (application in filteredList) {
            val itemView = inflater.inflate(R.layout.item_application, container, false)

            itemView.findViewById<TextView>(R.id.tvItemCompany).text = application.companyName
            itemView.findViewById<TextView>(R.id.tvItemRole).text = application.role
            itemView.findViewById<TextView>(R.id.tvItemLocation).text = "Location: ${application.location}"
            itemView.findViewById<TextView>(R.id.tvItemDate).text = "Date: ${application.date}"
            itemView.findViewById<TextView>(R.id.tvItemStatus).text = "Status: ${application.status}"

            // Delete icon listener
            itemView.findViewById<ImageView>(R.id.btnDelete).setOnClickListener {
                confirmDelete(application.id, application.companyName)
            }

            // Status toggle listener
            itemView.findViewById<Button>(R.id.btnToggleStatus).setOnClickListener {
                showStatusDialog(application.id, application.status)
            }

            container.addView(itemView)
        }
    }

    private fun confirmDelete(id: Long, companyName: String) {
        AlertDialog.Builder(this)
            .setTitle("Delete Application")
            .setMessage("Are you sure you want to delete application for $companyName?")
            .setPositiveButton("Delete") { _, _ ->
                val dbHelper = DatabaseHelper(this)
                val deleted = dbHelper.deleteApplication(id)
                dbHelper.close()
                if (deleted) {
                    Toast.makeText(this, "Application deleted", Toast.LENGTH_SHORT).show()
                    loadApplications()
                } else {
                    Toast.makeText(this, "Failed to delete application", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showStatusDialog(id: Long, currentStatus: String) {
        val statuses = arrayOf("Applied", "Running", "Completed")
        AlertDialog.Builder(this)
            .setTitle("Select Status")
            .setItems(statuses) { _, which ->
                val selectedStatus = statuses[which]
                val dbHelper = DatabaseHelper(this)
                val updated = dbHelper.updateApplicationStatus(id, selectedStatus)
                dbHelper.close()
                if (updated) {
                    Toast.makeText(this, "Status updated to $selectedStatus", Toast.LENGTH_SHORT).show()
                    loadApplications()
                }
            }
            .show()
    }
}