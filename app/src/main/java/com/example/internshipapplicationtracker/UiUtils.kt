package com.example.internshipapplicationtracker

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

/**
 * On Android 15+ apps draw behind the status bar and navigation bar.
 * This adds padding to the root view so content is never hidden behind them
 * (and stays above the keyboard).
 */
fun applySystemBarPadding(root: View) {
    ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
        val types = WindowInsetsCompat.Type.systemBars() or
                WindowInsetsCompat.Type.displayCutout() or
                WindowInsetsCompat.Type.ime()
        val bars = insets.getInsets(types)
        view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
        WindowInsetsCompat.CONSUMED
    }
}