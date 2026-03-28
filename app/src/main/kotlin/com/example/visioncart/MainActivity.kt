package com.example.visioncart

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.visioncart.R

class MainActivity : BaseActivity() {
    override fun onTtsReady() {
        speak("Welcome to Vision Cart Home. Select Scan, History, or Accessibility.")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Main Action Buttons
        val btnScan = findViewById<LinearLayout>(R.id.btnScanProduct)
        val btnHistory = findViewById<LinearLayout>(R.id.btnHistory)
        val btnAccess = findViewById<LinearLayout>(R.id.btnAccessibility)

        setVocalButton(btnScan, "Scan Product - Hover to scan new items")
        setVocalButton(btnHistory, "View History - Check your recently scanned items")
        setVocalButton(btnAccess, "Accessibility Settings - Adjust font, vibration, and speech")

        btnScan.setOnClickListener {
            vibrate()
            startActivity(Intent(this, ScanActivity::class.java))
        }

        btnHistory.setOnClickListener {
            vibrate()
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        btnAccess.setOnClickListener {
            vibrate()
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        
        // Navigation Bar
        val navScan = findViewById<LinearLayout>(R.id.navScan)
        val navHistory = findViewById<LinearLayout>(R.id.navHistory)
        val navSettings = findViewById<LinearLayout>(R.id.navSettings)

        setVocalButton(navScan, "Scan Shortcut")
        setVocalButton(navHistory, "History Shortcut")
        setVocalButton(navSettings, "Settings Shortcut")

        navScan.setOnClickListener {
            vibrate(30)
            startActivity(Intent(this, ScanActivity::class.java))
        }
        navHistory.setOnClickListener {
            vibrate(30)
            startActivity(Intent(this, HistoryActivity::class.java))
        }
        navSettings.setOnClickListener {
            vibrate(30)
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }
}

