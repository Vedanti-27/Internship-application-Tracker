package com.example.internshipapplicationtracker

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

/**
 * Login / Registration supporting Student and Operator / Admin roles.
 */
class LoginActivity : AppCompatActivity() {

    companion object {
        const val PREFS_NAME = "InternshipTrackerPrefs"
        const val KEY_IS_LOGGED_IN = "is_logged_in"
        const val KEY_EMAIL = "user_email"
        const val KEY_USER_NAME = "user_name"
        const val KEY_USER_ID = "user_id"
        const val KEY_USER_ROLE = "user_role"
    }

    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPhone: EditText
    private lateinit var etPassword: EditText
    private lateinit var rgRole: RadioGroup
    private lateinit var rbStudent: RadioButton
    private lateinit var rbOperator: RadioButton
    private lateinit var btnSignIn: Button
    private lateinit var btnSignUp: Button

    private var isSignUpMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (prefs.getBoolean(KEY_IS_LOGGED_IN, false)) {
            val role = prefs.getString(KEY_USER_ROLE, "student") ?: "student"
            openDashboard(role)
            return
        }

        setContentView(R.layout.activity_login)
        applySystemBarPadding(findViewById(R.id.rootLogin))

        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        etPhone = findViewById(R.id.etPhone)
        etPassword = findViewById(R.id.etPassword)
        rgRole = findViewById(R.id.rgRole)
        rbStudent = findViewById(R.id.rbStudent)
        rbOperator = findViewById(R.id.rbOperator)
        btnSignIn = findViewById(R.id.btnSignIn)
        btnSignUp = findViewById(R.id.btnSignUp)

        btnSignIn.setOnClickListener {
            if (isSignUpMode) {
                isSignUpMode = false
                updateUiForMode()
            } else {
                attemptLogin()
            }
        }

        btnSignUp.setOnClickListener {
            if (!isSignUpMode) {
                isSignUpMode = true
                updateUiForMode()
            } else {
                attemptSignUp()
            }
        }
    }

    private fun updateUiForMode() {
        if (isSignUpMode) {
            etName.visibility = View.VISIBLE
            etPhone.visibility = View.VISIBLE
            btnSignIn.text = "Back to Sign In"
            btnSignUp.text = "Create Account"
        } else {
            etName.visibility = View.GONE
            etPhone.visibility = View.GONE
            btnSignIn.text = "Sign In"
            btnSignUp.text = "Sign Up / Register"
        }
    }

    private fun getSelectedRole(): String {
        return if (rbOperator.isChecked) "operator" else "student"
    }

    private fun attemptLogin() {
        val email = etEmail.text.toString().trim().lowercase()
        val password = etPassword.text.toString()
        val selectedRole = getSelectedRole()

        if (email.isEmpty() || password.isEmpty()) {
            showToast("Login failed: email and password cannot be empty")
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToast("Login failed: please enter a valid email address")
            return
        }

        if (password.length < 4) {
            showToast("Login failed: password must have at least 4 characters")
            return
        }

        val userName = email.substringBefore("@")
        val dbHelper = DatabaseHelper(this)
        val user = dbHelper.getUserByEmail(email)

        val userId: Long
        val role: String

        if (user == null) {
            userId = dbHelper.insertUser(userName, email, "", selectedRole)
            role = selectedRole
        } else {
            userId = user.id
            role = user.role
        }
        dbHelper.close()

        if (userId == -1L) {
            showToast("Login failed: could not access the database")
            return
        }

        saveUserSession(email, userName, userId, role)
        showToast("Login successful as $role")
        openDashboard(role)
    }

    private fun attemptSignUp() {
        val name = etName.text.toString().trim()
        val email = etEmail.text.toString().trim().lowercase()
        val phone = etPhone.text.toString().trim()
        val password = etPassword.text.toString()
        val role = getSelectedRole()

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showToast("Sign up failed: name, email and password are required")
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToast("Sign up failed: please enter a valid email address")
            return
        }

        if (password.length < 4) {
            showToast("Sign up failed: password must have at least 4 characters")
            return
        }

        val dbHelper = DatabaseHelper(this)
        val userId = dbHelper.insertUser(name, email, phone, role)
        dbHelper.close()

        if (userId == -1L) {
            showToast("Sign up failed: user already exists or database error")
            return
        }

        saveUserSession(email, name, userId, role)
        showToast("Registration successful! Welcome, $name")
        openDashboard(role)
    }

    private fun saveUserSession(email: String, name: String, userId: Long, role: String) {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putString(KEY_EMAIL, email)
            .putString(KEY_USER_NAME, name)
            .putLong(KEY_USER_ID, userId)
            .putString(KEY_USER_ROLE, role)
            .apply()
    }

    private fun openDashboard(role: String) {
        val intent = if (role == "operator") {
            Intent(this, OperatorActivity::class.java)
        } else {
            Intent(this, MainActivity::class.java)
        }
        startActivity(intent)
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}