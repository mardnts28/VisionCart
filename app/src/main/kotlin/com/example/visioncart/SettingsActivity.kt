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

    private lateinit var themeYellowBlack: View
    private lateinit var themeWhiteBlue: View
    private lateinit var themeBlackWhite: View
    private lateinit var tvSpeechRate: TextView
    private lateinit var seekSpeechRate: SeekBar
    private lateinit var switchVoice: Switch
    private lateinit var switchVibration: Switch
    private lateinit var btnFontNormal: TextView
    private lateinit var btnFontLarge: TextView
    private lateinit var btnFontExtraLarge: TextView

    override fun onTtsReady() {
        val prefs = getSharedPreferences("VisionCartPrefs", Context.MODE_PRIVATE)
        if (prefs.getBoolean("skip_settings_intro", false)) {
            // Already announced or just recreation; clear the flag
            prefs.edit().putBoolean("skip_settings_intro", false).apply()
        } else {
            speak("Accessibility Settings Screen. Adjust speech rate, vibration, font size, or color scheme here.")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Bind Views
        tvSpeechRate = findViewById(R.id.tvSpeechRate)
        seekSpeechRate = findViewById(R.id.seekSpeechRate)
        switchVoice = findViewById(R.id.switchVoiceAssistant)
        switchVibration = findViewById(R.id.switchVibration)
        themeYellowBlack = findViewById(R.id.themeYellowBlack)
        themeWhiteBlue = findViewById(R.id.themeWhiteBlue)
        themeBlackWhite = findViewById(R.id.themeBlackWhite)
        btnFontNormal = findViewById(R.id.btnFontNormal)
        btnFontLarge = findViewById(R.id.btnFontLarge)
        btnFontExtraLarge = findViewById(R.id.btnFontExtraLarge)

        loadSettings()
        setupListeners()
        setupNavigation()
    }

    private fun loadSettings() {
        val prefs = getSharedPreferences("VisionCartPrefs", Context.MODE_PRIVATE)
        
        switchVoice.isChecked = prefs.getBoolean("voice_enabled", true)
        switchVibration.isChecked = prefs.getBoolean("vibration_enabled", true)

        val rateProgress = prefs.getInt("speech_rate_progress", 1) 
        seekSpeechRate.progress = rateProgress
        updateSpeechRateText(rateProgress)

        val fontSize = prefs.getString("font_size", "Large") ?: "Large"
        updateFontButtons(fontSize)

        val theme = prefs.getString("color_scheme", "YellowBlack") ?: "YellowBlack"
        updateThemeUI(theme)
    }

    private fun setupListeners() {
        val prefs = getSharedPreferences("VisionCartPrefs", Context.MODE_PRIVATE)

        switchVoice.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("voice_enabled", isChecked).apply()
        }

        switchVibration.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("vibration_enabled", isChecked).apply()
            if (isChecked) com.example.visioncart.util.VibrationUtil.vibrateClick(this)
        }

        themeYellowBlack.setOnClickListener { saveTheme("YellowBlack") }
        themeWhiteBlue.setOnClickListener { saveTheme("WhiteBlue") }
        themeBlackWhite.setOnClickListener { saveTheme("BlackWhite") }

        seekSpeechRate.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                           override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                updateSpeechRateText(progress)
                prefs.edit().putInt("speech_rate_progress", progress).apply()
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

    private fun saveTheme(theme: String) {
        val prefs = getSharedPreferences("VisionCartPrefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("color_scheme", theme)
            .putBoolean("skip_settings_intro", true)
            .apply()
        
        speak("$theme theme applied")
        com.example.visioncart.util.VibrationUtil.vibrateClick(this)
        
        // Recreate to apply the new theme immediately
        recreate()
    }

    private fun updateThemeUI(selected: String) {
        themeYellowBlack.setBackgroundResource(if (selected == "YellowBlack") R.drawable.bg_theme_option_active else R.drawable.bg_theme_option)
        themeWhiteBlue.setBackgroundResource(if (selected == "WhiteBlue") R.drawable.bg_theme_option_active else R.drawable.bg_theme_option)
        themeBlackWhite.setBackgroundResource(if (selected == "BlackWhite") R.drawable.bg_theme_option_active else R.drawable.bg_theme_option)
    }

    private fun saveFontSize(size: String) {
        getSharedPreferences("VisionCartPrefs", Context.MODE_PRIVATE)
            .edit()
            .putString("font_size", size)
            .putBoolean("skip_settings_intro", true)
            .apply()

        val text = when(size) {
            "Normal" -> "Normal font"
            "Large" -> "Large font"
            "ExtraLarge" -> "Extra Large font"
            else -> "$size font"
        }
        speak(text)
        com.example.visioncart.util.VibrationUtil.vibrateClick(this)
        recreate()
    }

    private fun updateSpeechRateText(progress: Int) {
        tvSpeechRate.text = when(progress) {
            0 -> "0.7x"; 1 -> "1x"; 2 -> "1.3x"; 3 -> "1.6x"; 4 -> "2x"; else -> "1x"
        }
    }

    private fun updateFontButtons(selectedSize: String) {
        val activeBg = R.drawable.bg_font_btn_active
        val inactiveBg = R.drawable.bg_font_btn_inactive
        
        // Use theme attributes to get colors
        val typedValue = android.util.TypedValue()
        theme.resolveAttribute(R.attr.appBackgroundColor, typedValue, true)
        val activeColor = typedValue.data
        theme.resolveAttribute(R.attr.appTextSecondary, typedValue, true)
        val inactiveColor = typedValue.data
 
        btnFontNormal.setBackgroundResource(if (selectedSize == "Normal") activeBg else inactiveBg)
        btnFontNormal.setTextColor(if (selectedSize == "Normal") activeColor else inactiveColor)
        btnFontNormal.setTypeface(null, if (selectedSize == "Normal") android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL)
 
        btnFontLarge.setBackgroundResource(if (selectedSize == "Large") activeBg else inactiveBg)
        btnFontLarge.setTextColor(if (selectedSize == "Large") activeColor else inactiveColor)
        btnFontLarge.setTypeface(null, if (selectedSize == "Large") android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL)
 
        btnFontExtraLarge.setBackgroundResource(if (selectedSize == "ExtraLarge") activeBg else inactiveBg)
        btnFontExtraLarge.setTextColor(if (selectedSize == "ExtraLarge") activeColor else inactiveColor)
        btnFontExtraLarge.setTypeface(null, if (selectedSize == "ExtraLarge") android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL)
    }

    private fun setupNavigation() {
        findViewById<LinearLayout>(R.id.navHome).setOnClickListener {
            com.example.visioncart.util.VibrationUtil.vibrateClick(this)
            startActivity(Intent(this, MainActivity::class.java)); finish()
        }
        findViewById<LinearLayout>(R.id.navScan).setOnClickListener {
            com.example.visioncart.util.VibrationUtil.vibrateClick(this)
            startActivity(Intent(this, ScanActivity::class.java)); finish()
        }
        findViewById<LinearLayout>(R.id.navHistory).setOnClickListener {
            com.example.visioncart.util.VibrationUtil.vibrateClick(this)
            startActivity(Intent(this, HistoryActivity::class.java)); finish()
        }
    }
}

