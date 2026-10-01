package com.example.internshipapplicationtracker

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** One row of the Users table. */
data class UserRecord(
    val id: Long,
    val name: String,
    val email: String,
    val phone: String,
    val role: String
)

/** One row of the Companies table (Internship postings). */
data class CompanyRecord(
    val id: Long,
    val companyName: String,
    val role: String,
    val location: String,
    val category: String,
    val description: String,
    val phone: String,
    val videoUrl: String
)

/** One row of the Applications table. */
data class ApplicationRecord(
    val id: Long,
    val userId: Long,
    val courseId: Long,
    val companyName: String,
    val role: String,
    val date: String,
    val status: String,
    val location: String
)

/** Counts summary for dashboard statistics. */
data class ApplicationCounts(
    val totalApplied: Int,
    val runningCount: Int,
    val completedCount: Int
)

/** Result of trying to save an application. */
enum class SaveResult {
    SAVED,
    ALREADY_SAVED,
    FAILED
}

/**
 * SQLite database helper for multi-role support (Student & Operator).
 * Tables: Users, Courses, Companies, Applications.
 */
class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "internship_tracker.db"
        private const val DATABASE_VERSION = 2

        // Table names
        private const val TABLE_USERS = "Users"
        private const val TABLE_COURSES = "Courses"
        private const val TABLE_COMPANIES = "Companies"
        private const val TABLE_APPLICATIONS = "Applications"

        // Shared column names
        private const val COL_ID = "id"

        // Users
        private const val COL_NAME = "name"
        private const val COL_EMAIL = "email"
        private const val COL_PHONE = "phone"
        private const val COL_ROLE = "role" // "student" or "operator"

        // Courses
        private const val COL_COURSE_NAME = "courseName"
        private const val COL_DESCRIPTION = "description"

        // Companies
        private const val COL_COMPANY_NAME = "companyName"
        private const val COL_JOB_ROLE = "jobRole"
        private const val COL_LOCATION = "location"
        private const val COL_CATEGORY = "category"
        private const val COL_COMPANY_DESC = "description"
        private const val COL_COMPANY_PHONE = "phone"
        private const val COL_VIDEO_URL = "videoUrl"

        // Applications
        private const val COL_USER_ID = "userId"
        private const val COL_COURSE_ID = "courseId"
        private const val COL_COMPANY = "companyName"
        private const val COL_APP_ROLE = "role"
        private const val COL_DATE = "date"
        private const val COL_STATUS = "status"
        private const val COL_APP_LOCATION = "location"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE $TABLE_USERS (" +
                    "$COL_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$COL_NAME TEXT NOT NULL, " +
                    "$COL_EMAIL TEXT NOT NULL UNIQUE, " +
                    "$COL_PHONE TEXT, " +
                    "$COL_ROLE TEXT DEFAULT 'student')"
        )

        db.execSQL(
            "CREATE TABLE $TABLE_COURSES (" +
                    "$COL_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$COL_COURSE_NAME TEXT NOT NULL, " +
                    "$COL_DESCRIPTION TEXT)"
        )

        db.execSQL(
            "CREATE TABLE $TABLE_COMPANIES (" +
                    "$COL_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$COL_COMPANY_NAME TEXT NOT NULL, " +
                    "$COL_JOB_ROLE TEXT NOT NULL, " +
                    "$COL_LOCATION TEXT, " +
                    "$COL_CATEGORY TEXT, " +
                    "$COL_COMPANY_DESC TEXT, " +
                    "$COL_COMPANY_PHONE TEXT, " +
                    "$COL_VIDEO_URL TEXT)"
        )

        db.execSQL(
            "CREATE TABLE $TABLE_APPLICATIONS (" +
                    "$COL_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$COL_USER_ID INTEGER NOT NULL, " +
                    "$COL_COURSE_ID INTEGER, " +
                    "$COL_COMPANY TEXT NOT NULL, " +
                    "$COL_APP_ROLE TEXT, " +
                    "$COL_DATE TEXT, " +
                    "$COL_STATUS TEXT, " +
                    "$COL_APP_LOCATION TEXT, " +
                    "FOREIGN KEY($COL_USER_ID) REFERENCES $TABLE_USERS($COL_ID), " +
                    "FOREIGN KEY($COL_COURSE_ID) REFERENCES $TABLE_COURSES($COL_ID))"
        )

        // Seed default categories
        seedCourse(db, "Software Development", "Programming, software design and application development internships.")
        seedCourse(db, "Mobile Development", "Android and iOS app development internships.")
        seedCourse(db, "Web Development", "Frontend and backend website development internships.")

        // Seed default companies
        seedCompany(
            db,
            "ABC Technologies",
            "Software Development Intern",
            "Pune",
            "Software Development",
            "Work with the ABC Technologies engineering team on real software projects. Learn clean code and SDLC.",
            "5554",
            ""
        )
        seedCompany(
            db,
            "XYZ Solutions",
            "Android Development Intern",
            "Mumbai",
            "Mobile Development",
            "Join XYZ Solutions to build and improve Android apps using Kotlin and Jetpack components.",
            "5556",
            ""
        )
        seedCompany(
            db,
            "TechNova Pvt Ltd",
            "Web Development Intern",
            "Bengaluru",
            "Web Development",
            "Help TechNova build responsive websites using modern web development frameworks.",
            "5558",
            ""
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_APPLICATIONS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_COMPANIES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_COURSES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db)
    }

    private fun seedCourse(db: SQLiteDatabase, courseName: String, description: String) {
        val values = ContentValues()
        values.put(COL_COURSE_NAME, courseName)
        values.put(COL_DESCRIPTION, description)
        db.insert(TABLE_COURSES, null, values)
    }

    private fun seedCompany(
        db: SQLiteDatabase,
        name: String,
        role: String,
        location: String,
        category: String,
        desc: String,
        phone: String,
        videoUrl: String
    ) {
        val values = ContentValues()
        values.put(COL_COMPANY_NAME, name)
        values.put(COL_JOB_ROLE, role)
        values.put(COL_LOCATION, location)
        values.put(COL_CATEGORY, category)
        values.put(COL_COMPANY_DESC, desc)
        values.put(COL_COMPANY_PHONE, phone)
        values.put(COL_VIDEO_URL, videoUrl)
        db.insert(TABLE_COMPANIES, null, values)
    }

    // ---------------- Users ----------------

    fun insertUser(name: String, email: String, phone: String, role: String = "student"): Long {
        val existingUser = getUserByEmail(email)
        if (existingUser != null) {
            return existingUser.id
        }

        val values = ContentValues()
        values.put(COL_NAME, name)
        values.put(COL_EMAIL, email)
        values.put(COL_PHONE, phone)
        values.put(COL_ROLE, role)
        return writableDatabase.insert(TABLE_USERS, null, values)
    }

    fun getUserByEmail(email: String): UserRecord? {
        var user: UserRecord? = null
        readableDatabase.query(
            TABLE_USERS,
            null,
            "$COL_EMAIL = ?",
            arrayOf(email),
            null, null, null
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                user = UserRecord(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID)),
                    name = cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME)) ?: "",
                    email = cursor.getString(cursor.getColumnIndexOrThrow(COL_EMAIL)) ?: "",
                    phone = cursor.getString(cursor.getColumnIndexOrThrow(COL_PHONE)) ?: "",
                    role = cursor.getString(cursor.getColumnIndexOrThrow(COL_ROLE)) ?: "student"
                )
            }
        }
        return user
    }

    fun getUserIdByEmail(email: String): Long {
        return getUserByEmail(email)?.id ?: -1L
    }

    fun getAllUsers(): List<UserRecord> {
        val list = ArrayList<UserRecord>()
        readableDatabase.query(
            TABLE_USERS,
            null,
            null,
            null,
            null, null,
            "$COL_ID DESC"
        ).use { cursor ->
            while (cursor.moveToNext()) {
                list.add(
                    UserRecord(
                        id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID)),
                        name = cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME)) ?: "",
                        email = cursor.getString(cursor.getColumnIndexOrThrow(COL_EMAIL)) ?: "",
                        phone = cursor.getString(cursor.getColumnIndexOrThrow(COL_PHONE)) ?: "",
                        role = cursor.getString(cursor.getColumnIndexOrThrow(COL_ROLE)) ?: "student"
                    )
                )
            }
        }
        return list
    }

    // ---------------- Companies (Added by Operator) ----------------

    fun insertCompany(
        companyName: String,
        role: String,
        location: String,
        category: String,
        description: String,
        phone: String,
        videoUrl: String = ""
    ): Long {
        val values = ContentValues()
        values.put(COL_COMPANY_NAME, companyName)
        values.put(COL_JOB_ROLE, role)
        values.put(COL_LOCATION, location)
        values.put(COL_CATEGORY, category)
        values.put(COL_COMPANY_DESC, description)
        values.put(COL_COMPANY_PHONE, phone)
        values.put(COL_VIDEO_URL, videoUrl)
        return writableDatabase.insert(TABLE_COMPANIES, null, values)
    }

    fun getCompanies(): List<CompanyRecord> {
        val list = ArrayList<CompanyRecord>()
        readableDatabase.query(
            TABLE_COMPANIES,
            null,
            null,
            null,
            null, null,
            "$COL_ID DESC"
        ).use { cursor ->
            while (cursor.moveToNext()) {
                list.add(
                    CompanyRecord(
                        id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID)),
                        companyName = cursor.getString(cursor.getColumnIndexOrThrow(COL_COMPANY_NAME)) ?: "",
                        role = cursor.getString(cursor.getColumnIndexOrThrow(COL_JOB_ROLE)) ?: "",
                        location = cursor.getString(cursor.getColumnIndexOrThrow(COL_LOCATION)) ?: "",
                        category = cursor.getString(cursor.getColumnIndexOrThrow(COL_CATEGORY)) ?: "",
                        description = cursor.getString(cursor.getColumnIndexOrThrow(COL_COMPANY_DESC)) ?: "",
                        phone = cursor.getString(cursor.getColumnIndexOrThrow(COL_COMPANY_PHONE)) ?: "",
                        videoUrl = cursor.getString(cursor.getColumnIndexOrThrow(COL_VIDEO_URL)) ?: ""
                    )
                )
            }
        }
        return list
    }

    fun deleteCompany(id: Long): Boolean {
        return writableDatabase.delete(TABLE_COMPANIES, "$COL_ID = ?", arrayOf(id.toString())) > 0
    }

    // ---------------- Courses ----------------

    fun insertCourse(courseName: String, description: String): Long {
        val values = ContentValues()
        values.put(COL_COURSE_NAME, courseName)
        values.put(COL_DESCRIPTION, description)
        return writableDatabase.insert(TABLE_COURSES, null, values)
    }

    fun getCourseIdByName(courseName: String): Long {
        var id = -1L
        readableDatabase.query(
            TABLE_COURSES,
            arrayOf(COL_ID),
            "$COL_COURSE_NAME = ?",
            arrayOf(courseName),
            null, null, null
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                id = cursor.getLong(0)
            }
        }
        return id
    }

    // ---------------- Applications ----------------

    fun insertApplication(
        userId: Long,
        courseId: Long,
        companyName: String,
        role: String,
        date: String,
        status: String,
        location: String
    ): Long {
        val values = ContentValues()
        values.put(COL_USER_ID, userId)
        values.put(COL_COURSE_ID, courseId)
        values.put(COL_COMPANY, companyName)
        values.put(COL_APP_ROLE, role)
        values.put(COL_DATE, date)
        values.put(COL_STATUS, status)
        values.put(COL_APP_LOCATION, location)
        return writableDatabase.insert(TABLE_APPLICATIONS, null, values)
    }

    fun applicationExists(userId: Long, companyName: String, role: String): Boolean {
        return readableDatabase.query(
            TABLE_APPLICATIONS,
            arrayOf(COL_ID),
            "$COL_USER_ID = ? AND $COL_COMPANY = ? AND $COL_APP_ROLE = ?",
            arrayOf(userId.toString(), companyName, role),
            null, null, null
        ).use { cursor ->
            cursor.count > 0
        }
    }

    fun deleteApplication(id: Long): Boolean {
        return writableDatabase.delete(
            TABLE_APPLICATIONS,
            "$COL_ID = ?",
            arrayOf(id.toString())
        ) > 0
    }

    fun updateApplicationStatus(id: Long, newStatus: String): Boolean {
        val values = ContentValues()
        values.put(COL_STATUS, newStatus)
        return writableDatabase.update(
            TABLE_APPLICATIONS,
            values,
            "$COL_ID = ?",
            arrayOf(id.toString())
        ) > 0
    }

    fun getApplications(userId: Long = -1L): List<ApplicationRecord> {
        val list = ArrayList<ApplicationRecord>()

        val selection = if (userId == -1L) null else "$COL_USER_ID = ?"
        val selectionArgs = if (userId == -1L) null else arrayOf(userId.toString())

        readableDatabase.query(
            TABLE_APPLICATIONS,
            null,
            selection,
            selectionArgs,
            null, null,
            "$COL_ID DESC"
        ).use { cursor ->
            while (cursor.moveToNext()) {
                list.add(
                    ApplicationRecord(
                        id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID)),
                        userId = cursor.getLong(cursor.getColumnIndexOrThrow(COL_USER_ID)),
                        courseId = cursor.getLong(cursor.getColumnIndexOrThrow(COL_COURSE_ID)),
                        companyName = cursor.getString(cursor.getColumnIndexOrThrow(COL_COMPANY)) ?: "",
                        role = cursor.getString(cursor.getColumnIndexOrThrow(COL_APP_ROLE)) ?: "",
                        date = cursor.getString(cursor.getColumnIndexOrThrow(COL_DATE)) ?: "",
                        status = cursor.getString(cursor.getColumnIndexOrThrow(COL_STATUS)) ?: "",
                        location = cursor.getString(cursor.getColumnIndexOrThrow(COL_APP_LOCATION)) ?: ""
                    )
                )
            }
        }
        return list
    }

    fun getApplicationCounts(userId: Long = -1L): ApplicationCounts {
        val apps = getApplications(userId)
        val total = apps.size
        val running = apps.count { it.status.equals("Running", ignoreCase = true) || it.status.equals("In Progress", ignoreCase = true) }
        val completed = apps.count { it.status.equals("Completed", ignoreCase = true) }
        return ApplicationCounts(totalApplied = total, runningCount = running, completedCount = completed)
    }

    fun saveInternshipApplication(
        userId: Long,
        companyName: String,
        role: String,
        location: String,
        category: String,
        status: String = "Applied"
    ): SaveResult {
        if (userId == -1L) {
            return SaveResult.FAILED
        }

        if (applicationExists(userId, companyName, role)) {
            return SaveResult.ALREADY_SAVED
        }

        var courseId = getCourseIdByName(category)
        if (courseId == -1L) {
            courseId = insertCourse(category, "Internship category: $category")
        }

        val today = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
        val rowId = insertApplication(userId, courseId, companyName, role, today, status, location)

        return if (rowId == -1L) SaveResult.FAILED else SaveResult.SAVED
    }
}