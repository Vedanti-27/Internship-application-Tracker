package com.example.internshipapplicationtracker

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

/**
 * Operator / Admin Dashboard:
 * Allows operators to post new companies/internships, manage existing company listings,
 * and view/manage applications submitted by multiple users.
 */
class OperatorActivity : AppCompatActivity() {

    private lateinit var etCompany: EditText
    private lateinit var etRole: EditText
    private lateinit var etLocation: EditText
    private lateinit var etCategory: EditText
    private lateinit var etPhone: EditText
    private lateinit var etVideoUrl: EditText
    private lateinit var etDescription: EditText
    private lateinit var btnAddCompany: Button

    private lateinit var containerCompanies: LinearLayout
    private lateinit var containerAllApplications: LinearLayout
    private lateinit var btnOpLogout: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences(LoginActivity.PREFS_NAME, Context.MODE_PRIVATE)
        if (!prefs.getBoolean(LoginActivity.KEY_IS_LOGGED_IN, false) ||
            prefs.getString(LoginActivity.KEY_USER_ROLE, "student") != "operator"
        ) {
            openLoginScreen()
            return
        }

        setContentView(R.layout.activity_operator)
        applySystemBarPadding(findViewById(R.id.rootOperator))

        etCompany = findViewById(R.id.etOpCompany)
        etRole = findViewById(R.id.etOpRole)
        etLocation = findViewById(R.id.etOpLocation)
        etCategory = findViewById(R.id.etOpCategory)
        etPhone = findViewById(R.id.etOpPhone)
        etVideoUrl = findViewById(R.id.etOpVideoUrl)
        etDescription = findViewById(R.id.etOpDescription)
        btnAddCompany = findViewById(R.id.btnAddCompany)

        containerCompanies = findViewById(R.id.containerCompanies)
        containerAllApplications = findViewById(R.id.containerAllApplications)
        btnOpLogout = findViewById(R.id.btnOpLogout)

        btnAddCompany.setOnClickListener {
            addCompany()
        }

        btnOpLogout.setOnClickListener {
            logout()
        }

        loadCompanies()
        loadAllApplications()
    }

    private fun addCompany() {
        val companyName = etCompany.text.toString().trim()
        val role = etRole.text.toString().trim()
        val location = etLocation.text.toString().trim()
        val category = etCategory.text.toString().trim()
        val phone = etPhone.text.toString().trim()
        val videoUrl = etVideoUrl.text.toString().trim()
        val description = etDescription.text.toString().trim()

        if (companyName.isEmpty() || role.isEmpty() || location.isEmpty()) {
            Toast.makeText(this, "Company Name, Role, and Location are required", Toast.LENGTH_SHORT).show()
            return
        }

        val dbHelper = DatabaseHelper(this)
        val rowId = dbHelper.insertCompany(
            companyName = companyName,
            role = role,
            location = location,
            category = if (category.isEmpty()) "General Internship" else category,
            description = if (description.isEmpty()) "Great internship opportunity at $companyName." else description,
            phone = if (phone.isEmpty()) "5554" else phone,
            videoUrl = videoUrl
        )
        dbHelper.close()

        if (rowId != -1L) {
            Toast.makeText(this, "Internship posted successfully!", Toast.LENGTH_SHORT).show()
            clearForm()
            loadCompanies()
        } else {
            Toast.makeText(this, "Failed to post internship", Toast.LENGTH_SHORT).show()
        }
    }

    private fun clearForm() {
        etCompany.text.clear()
        etRole.text.clear()
        etLocation.text.clear()
        etCategory.text.clear()
        etPhone.text.clear()
        etVideoUrl.text.clear()
        etDescription.text.clear()
    }

    private fun loadCompanies() {
        containerCompanies.removeAllViews()
        val dbHelper = DatabaseHelper(this)
        val companies = dbHelper.getCompanies()
        dbHelper.close()

        if (companies.isEmpty()) {
            val emptyTv = TextView(this).apply {
                text = "No posted companies found."
                setPadding(16, 16, 16, 16)
            }
            containerCompanies.addView(emptyTv)
            return
        }

        val inflater = LayoutInflater.from(this)
        for (company in companies) {
            val card = inflater.inflate(R.layout.item_application, containerCompanies, false)

            card.findViewById<TextView>(R.id.tvItemCompany).text = company.companyName
            card.findViewById<TextView>(R.id.tvItemRole).text = "${company.role} (${company.category})"
            card.findViewById<TextView>(R.id.tvItemLocation).text = "Location: ${company.location}"
            card.findViewById<TextView>(R.id.tvItemDate).text = "Phone: ${company.phone}"
            card.findViewById<TextView>(R.id.tvItemStatus).text = "Active Posting"

            card.findViewById<Button>(R.id.btnToggleStatus).visibility = android.view.View.GONE

            card.findViewById<ImageView>(R.id.btnDelete).setOnClickListener {
                confirmDeleteCompany(company.id, company.companyName)
            }

            containerCompanies.addView(card)
        }
    }

    private fun confirmDeleteCompany(id: Long, companyName: String) {
        AlertDialog.Builder(this)
            .setTitle("Delete Posting")
            .setMessage("Delete $companyName from internship listings?")
            .setPositiveButton("Delete") { _, _ ->
                val dbHelper = DatabaseHelper(this)
                val deleted = dbHelper.deleteCompany(id)
                dbHelper.close()
                if (deleted) {
                    Toast.makeText(this, "Posting deleted", Toast.LENGTH_SHORT).show()
                    loadCompanies()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun loadAllApplications() {
        containerAllApplications.removeAllViews()
        val dbHelper = DatabaseHelper(this)
        val applications = dbHelper.getApplications(-1L) // Fetch all student applications
        dbHelper.close()

        if (applications.isEmpty()) {
            val emptyTv = TextView(this).apply {
                text = "No student applications recorded yet."
                setPadding(16, 16, 16, 16)
            }
            containerAllApplications.addView(emptyTv)
            return
        }

        val inflater = LayoutInflater.from(this)
        for (app in applications) {
            val card = inflater.inflate(R.layout.item_application, containerAllApplications, false)

            card.findViewById<TextView>(R.id.tvItemCompany).text = "${app.companyName} (User ID: ${app.userId})"
            card.findViewById<TextView>(R.id.tvItemRole).text = app.role
            card.findViewById<TextView>(R.id.tvItemLocation).text = "Location: ${app.location}"
            card.findViewById<TextView>(R.id.tvItemDate).text = "Applied Date: ${app.date}"
            card.findViewById<TextView>(R.id.tvItemStatus).text = "Status: ${app.status}"

            card.findViewById<ImageView>(R.id.btnDelete).setOnClickListener {
                val helper = DatabaseHelper(this)
                helper.deleteApplication(app.id)
                helper.close()
                Toast.makeText(this, "Application removed", Toast.LENGTH_SHORT).show()
                loadAllApplications()
            }

            card.findViewById<Button>(R.id.btnToggleStatus).setOnClickListener {
                showUpdateStatusDialog(app.id)
            }

            containerAllApplications.addView(card)
        }
    }

    private fun showUpdateStatusDialog(appId: Long) {
        val statuses = arrayOf("Applied", "Running", "Completed")
        AlertDialog.Builder(this)
            .setTitle("Update Student Application Status")
            .setItems(statuses) { _, which ->
                val selectedStatus = statuses[which]
                val dbHelper = DatabaseHelper(this)
                val updated = dbHelper.updateApplicationStatus(appId, selectedStatus)
                dbHelper.close()
                if (updated) {
                    Toast.makeText(this, "Status updated to $selectedStatus", Toast.LENGTH_SHORT).show()
                    loadAllApplications()
                }
            }
            .show()
    }

    private fun logout() {
        val prefs = getSharedPreferences(LoginActivity.PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
        openLoginScreen()
    }

    private fun openLoginScreen() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}