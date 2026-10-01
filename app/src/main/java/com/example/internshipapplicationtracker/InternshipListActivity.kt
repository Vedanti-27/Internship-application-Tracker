package com.example.internshipapplicationtracker

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/**
 * Browses all internships posted by operators in the SQLite database.
 */
class InternshipListActivity : AppCompatActivity() {

    private lateinit var containerList: LinearLayout
    private lateinit var tvListEmpty: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_internship_list)
        applySystemBarPadding(findViewById(R.id.rootList))

        containerList = findViewById(R.id.containerInternshipList)
        tvListEmpty = findViewById(R.id.tvListEmpty)
    }

    override fun onResume() {
        super.onResume()
        loadCompaniesFromDb()
    }

    private fun loadCompaniesFromDb() {
        containerList.removeAllViews()

        val dbHelper = DatabaseHelper(this)
        val companies = dbHelper.getCompanies()
        dbHelper.close()

        if (companies.isEmpty()) {
            tvListEmpty.visibility = View.VISIBLE
            return
        }

        tvListEmpty.visibility = View.GONE
        val inflater = LayoutInflater.from(this)

        for (company in companies) {
            val cardView = inflater.inflate(R.layout.item_application, containerList, false)

            cardView.findViewById<TextView>(R.id.tvItemCompany).text = company.companyName
            cardView.findViewById<TextView>(R.id.tvItemRole).text = company.role
            cardView.findViewById<TextView>(R.id.tvItemLocation).text = "Location: ${company.location}"
            cardView.findViewById<TextView>(R.id.tvItemDate).text = "Category: ${company.category}"
            cardView.findViewById<TextView>(R.id.tvItemStatus).text = "Status: Open"

            cardView.findViewById<View>(R.id.btnDelete).visibility = View.GONE
            cardView.findViewById<View>(R.id.btnToggleStatus).visibility = View.GONE

            cardView.setOnClickListener {
                openDetails(company)
            }

            containerList.addView(cardView)
        }
    }

    private fun openDetails(company: CompanyRecord) {
        val intent = Intent(this, InternshipDetailsActivity::class.java).apply {
            putExtra(InternshipDetailsActivity.EXTRA_COMPANY, company.companyName)
            putExtra(InternshipDetailsActivity.EXTRA_ROLE, company.role)
            putExtra(InternshipDetailsActivity.EXTRA_LOCATION, company.location)
            putExtra(InternshipDetailsActivity.EXTRA_CATEGORY, company.category)
            putExtra(InternshipDetailsActivity.EXTRA_DESCRIPTION, company.description)
            putExtra(InternshipDetailsActivity.EXTRA_PHONE, company.phone)
            putExtra(InternshipDetailsActivity.EXTRA_VIDEO_URL, company.videoUrl)
            putExtra(InternshipDetailsActivity.EXTRA_STATUS, "Open")
        }
        startActivity(intent)
    }
}