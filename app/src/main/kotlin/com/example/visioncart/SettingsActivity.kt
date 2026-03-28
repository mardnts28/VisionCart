package com.example.visioncart

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Vibrator
import android.view.View
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.visioncart.R

class SettingsActivity : BaseActivity() {

    private lateinit var tvSpeechRate: TextView
    private lateinit var seekSpeechRate: SeekBar
    private lateinit var switchVoice: Switch
    private lateinit var switchVibration: Switch
    private lateinit var switchContrast: Switch
    private lateinit var btnFontNormal: TextView
    private lateinit var btnFontLarge: TextView
    private lateinit var btnFontExtraLarge: TextView

    override fun onTtsReady() {
        speak("Accessibility Settings Screen. Adjust speech rate, vibration, or font size here.")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Bind Views
        tvSpeechRate = findViewById(R.id.tvSpeechRate)
        seekSpeechRate = findViewById(R.id.seekSpeechRate)
        switchVoice = findViewById(R.id.switchVoiceAssistant)
        switchVibration = findViewById(R.id.switchVibration)
        switchContrast = findViewById(R.id.switchHighContrast)
        btnFontNormal = findViewById(R.id.btnFontNormal)
        btnFontLarge = findViewById(R.id.btnFontLarge)
        btnFontExtraLarge = findViewById(R.id.btnFontExtraLarge)

        loadSettings()
        setupListeners()
        setupNavigation()
    }

    private fun loadSettings() {
        val prefs = getSharedPreferences("VisionCartPrefs", Context.MODE_PRIVATE)
        
        // Load Voice/Vibe/Contrast
        switchVoice.isChecked = prefs.getBoolean("voice_enabled", true)
        switchVibration.isChecked = prefs.getBoolean("vibration_enabled", true)
        switchContrast.isChecked = prefs.getBoolean("high_contrast", true)

        // Load Speech Rate
        val rateProgress = prefs.getInt("speech_rate_progress", 1) // 0 to 4
        seekSpeechRate.progress = rateProgress
        updateSpeechRateText(rateProgress)

        // Load Font Size
        val fontSize = prefs.getString("font_size", "Large") ?: "Large"
        updateFontButtons(fontSize)
    }

    private fun setupListeners() {
        val prefs = getSharedPreferences("VisionCartPrefs", Context.MODE_PRIVATE)

        switchVoice.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("voice_enabled", isChecked).apply()
        }

        switchVibration.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("vibration_enabled", isChecked).apply()
            if (isChecked) vibrate()
        }

        switchContrast.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("high_contrast", isChecked).apply()
        }

        seekSpeechRate.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                updateSpeechRateText(progress)
                prefs.edit().putInt("speech_rate_progress", progress).apply()
                // Actual multiplier for TTS
                val actualRate = when(progress) {
                    0 -> 0.7f
                    1 -> 1.0f
                    2 -> 1.3f
                    3 -> 1.6f
                    4 -> 2.0f
                    else -> 1.0f
                }
                prefs.edit().putFloat("speech_rate", actualRate).apply()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        btnFontNormal.setOnClickListener { saveFontSize("Normal") }
        btnFontLarge.setOnClickListener { saveFontSize("Large") }
        btnFontExtraLarge.setOnClickListener { saveFontSize("ExtraLarge") }
    }

    private fun saveFontSize(size: String) {
        getSharedPreferences("VisionCartPrefs", Context.MODE_PRIVATE)
            .edit().putString("font_size", size).apply()
        updateFontButtons(size)
    }

    private fun updateSpeechRateText(progress: Int) {
        tvSpeechRate.text = when(progress) {
            0 -> "0.7x"
            1 -> "1x"
            2 -> "1.3x"
            3 -> "1.6x"
            4 -> "2x"
            else -> "1x"
        }
    }

    private fun updateFontButtons(selectedSize: String) {
        val activeBg = R.drawable.bg_font_btn_active
        val inactiveBg = R.drawable.bg_font_btn_inactive
        val activeColor = getColor(R.color.bg_dark)
        val inactiveColor = getColor(R.color.text_gray)

        btnFontNormal.setBackgroundResource(if (selectedSize == "Normal") activeBg else inactiveBg)
        btnFontNormal.setTextColor(if (selectedSize == "Normal") activeColor else inactiveColor)

        btnFontLarge.setBackgroundResource(if (selectedSize == "Large") activeBg else inactiveBg)
        btnFontLarge.setTextColor(if (selectedSize == "Large") activeColor else inactiveColor)

        btnFontExtraLarge.setBackgroundResource(if (selectedSize == "ExtraLarge") activeBg else inactiveBg)
        btnFontExtraLarge.setTextColor(if (selectedSize == "ExtraLarge") activeColor else inactiveColor)
    }

    private fun vibrate() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(50)
    }

    private fun setupNavigation() {
        findViewById<LinearLayout>(R.id.navHome).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        findViewById<LinearLayout>(R.id.navScan).setOnClickListener {
            startActivity(Intent(this, ScanActivity::class.java))
            finish()
        }
        findViewById<LinearLayout>(R.id.navHistory).setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
            finish()
        }
    }
}

