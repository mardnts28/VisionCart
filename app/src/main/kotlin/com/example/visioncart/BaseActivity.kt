package com.example.visioncart

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Vibrator
import android.speech.tts.TextToSpeech
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

open class BaseActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    protected var globalTts: TextToSpeech? = null
    protected var isTtsReady = false

    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("VisionCartPrefs", Context.MODE_PRIVATE)
        val fontScale = when(prefs.getString("font_size", "Large")) {
            "Normal" -> 1.0f
            "Large" -> 1.3f
            "ExtraLarge" -> 1.6f
            else -> 1.3f
        }
        val config = newBase.resources.configuration
        config.fontScale = fontScale
        super.attachBaseContext(newBase.createConfigurationContext(config))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        globalTts = TextToSpeech(this, this)
        applyHighContrastTheme()
    }

    private fun applyHighContrastTheme() {
        val prefs = getSharedPreferences("VisionCartPrefs", Context.MODE_PRIVATE)
        if (prefs.getBoolean("high_contrast", true)) {
            // In a real app, use setTheme(R.style.HighContrastTheme)
            // But for now, since VisionCart is already black/yellow, 
            // the dark background is set in XML.
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            globalTts?.language = Locale.US
            val prefs = getSharedPreferences("VisionCartPrefs", Context.MODE_PRIVATE)
            globalTts?.setSpeechRate(prefs.getFloat("speech_rate", 1.0f))
            isTtsReady = true
            onTtsReady()
        }
    }

    open fun onTtsReady() {}

    fun speak(text: String?, queueMode: Int = TextToSpeech.QUEUE_FLUSH) {
        val prefs = getSharedPreferences("VisionCartPrefs", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("voice_enabled", true)) return
        
        if (isTtsReady && text != null) {
            globalTts?.speak(text, queueMode, null, "global_audio")
        }
    }

    // Call this in onCreate to make a button talk on long press or hover
    fun setVocalButton(view: View?, description: String) {
        view?.setOnLongClickListener {
            speak(description)
            vibrate(30)
            true
        }
        view?.contentDescription = description
    }

    // Call this for any button click logic
    fun vibrate(duration: Long = 50) {
        val prefs = getSharedPreferences("VisionCartPrefs", Context.MODE_PRIVATE)
        if (prefs.getBoolean("vibration_enabled", true)) {
            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(duration)
        }
    }

    override fun onDestroy() {
        globalTts?.stop()
        globalTts?.shutdown()
        super.onDestroy()
    }
}
