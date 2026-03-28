package com.example.visioncart

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.visioncart.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<LinearLayout>(R.id.btnScanProduct).setOnClickListener {
            startActivity(Intent(this, ScanActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.btnHistory).setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.btnAccessibility).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        
        // Navigation Bar
        findViewById<LinearLayout>(R.id.navScan).setOnClickListener {
            startActivity(Intent(this, ScanActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.navHistory).setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.navSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }
}
