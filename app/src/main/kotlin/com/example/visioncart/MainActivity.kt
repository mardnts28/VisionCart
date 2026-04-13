package com.example.visioncart

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import com.example.visioncart.R

class MainActivity : BaseActivity() {
    override fun onTtsReady() {
        speak("Welcome to Vision Cart Home. Select Scan, History, or Accessibility.")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Logo
        val logoContainer = findViewById<android.widget.FrameLayout>(R.id.logoContainer)
        setVocalButton(logoContainer, "Vision Cart Logo")
        logoContainer.setOnClickListener {
            vibrate(30)
            speak("Welcome to Vision Cart.")
        }

        // Search Bar Section (Voice Assistant Trigger)
        val searchContainer = findViewById<android.view.View>(R.id.searchContainer)
        val btnVoiceSearch = findViewById<android.view.View>(R.id.btnVoiceSearch)
        
        searchContainer.setOnClickListener {
            vibrate(30)
            startListeningForCommand()
        }
        btnVoiceSearch.setOnClickListener {
            vibrate(30)
            startListeningForCommand()
        }
        setVocalButton(searchContainer, "Voice Search - Tap to say a command like scan or change theme")

        // Main Action Buttons
        val btnScan = findViewById<LinearLayout>(R.id.btnScanProduct)
        val btnHistory = findViewById<LinearLayout>(R.id.btnHistory)
        val btnAccessibility = findViewById<LinearLayout>(R.id.btnAccessibility)
        val welcomeCard = findViewById<LinearLayout>(R.id.welcomeCard)

        setVocalButton(btnScan, "Scan Product - Hover to scan new items")
        setVocalButton(btnHistory, "View History - Check your recently scanned items")
        setVocalButton(btnAccessibility, "Accessibility Settings - Adjust font, vibration, and speech")
        setVocalButton(welcomeCard, "Welcome Card - Tap to repeat greeting")

        btnScan.setOnClickListener {
            vibrate()
            startActivity(Intent(this, ScanActivity::class.java))
        }

        btnHistory.setOnClickListener {
            vibrate()
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        btnAccessibility.setOnClickListener {
            vibrate()
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        welcomeCard.setOnClickListener {
            vibrate(30)
            speak("Welcome to Vision Cart Home. Select Scan, History, or Accessibility.")
        }

        // Voice Card Close
        val btnCloseVoice = findViewById<android.widget.TextView>(R.id.btnCloseVoice)
        val voiceAssistantCard = findViewById<android.widget.RelativeLayout>(R.id.voiceAssistantCard)
        val tvUserSpeech = findViewById<android.widget.TextView>(R.id.tvUserSpeech)

        // Quick Guides
        val guide1 = findViewById<android.view.View>(R.id.guidePointCamera)
        val guide2 = findViewById<android.view.View>(R.id.guideAutoDetect)
        val guide3 = findViewById<android.view.View>(R.id.guideHearInfo)

        guide1.setOnClickListener {
            vibrate(30)
            speak("Guide 1: Point your camera at a product barcode.")
            startActivity(Intent(this, ScanActivity::class.java))
        }
        setVocalButton(guide1, "Guide 1: Point Camera - Tao to open scanner")

        guide2.setOnClickListener {
            vibrate(30)
            speak("Guide 2: Wait for auto detection. The app will find the barcode for you.")
            startActivity(Intent(this, ScanActivity::class.java))
        }
        setVocalButton(guide2, "Guide 2: Auto Detect - Tap to open scanner")

        guide3.setOnClickListener {
            vibrate(30)
            speak("Guide 3: Hear product info. The app will read details like price and expiry.")
            findViewById<android.widget.ScrollView>(R.id.scrollView).smoothScrollTo(0, 0)
        }
        setVocalButton(guide3, "Guide 3: Hear Information - Learn more about speech")

        // Navigation Bar
        val navHome = findViewById<LinearLayout>(R.id.navHome)
        val navScan = findViewById<LinearLayout>(R.id.navScan)
        val navHistory = findViewById<LinearLayout>(R.id.navHistory)
        val navSettings = findViewById<LinearLayout>(R.id.navSettings)

        setVocalButton(navHome, "Home Shortcut")
        setVocalButton(navScan, "Scan Shortcut")
        setVocalButton(navHistory, "History Shortcut")
        setVocalButton(navSettings, "Settings Shortcut")

        navHome.setOnClickListener {
            vibrate(30)
            findViewById<android.widget.ScrollView>(R.id.scrollView).smoothScrollTo(0, 0)
        }
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

        // Voice Card Close + speech display
        btnCloseVoice.setOnClickListener {
            vibrate(30)
            voiceAssistantCard.visibility = android.view.View.GONE
        }
        
        // Show transcribed text in the voice assistant card
        onVoiceResultListener = { text ->
            runOnUiThread {
                tvUserSpeech.text = text
            }
        }
    }

}

