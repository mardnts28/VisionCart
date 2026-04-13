package com.example.visioncart

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.util.Locale
import java.util.ArrayList

open class BaseActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    protected var globalTts: TextToSpeech? = null
    protected var isTtsReady = false
    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false

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
        applyCustomTheme()
        super.onCreate(savedInstanceState)
        globalTts = TextToSpeech(this, this)
        setupGlobalSpeechRecognizer()
    }

    private fun applyCustomTheme() {
        val prefs = getSharedPreferences("VisionCartPrefs", Context.MODE_PRIVATE)
        val theme = prefs.getString("color_scheme", "YellowBlack")
        when(theme) {
            "WhiteBlue" -> setTheme(R.style.Theme_VisionCart_WhiteBlue)
            "BlackWhite" -> setTheme(R.style.Theme_VisionCart_BlackWhite)
            else -> setTheme(R.style.Theme_VisionCart_YellowBlack)
        }
    }

    private fun setupGlobalSpeechRecognizer() {
        val prefs = getSharedPreferences("VisionCartPrefs", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("voice_enabled", true)) return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) return

        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            speechRecognizer?.destroy()
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            }

            speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) { isListening = true }
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() { 
                    isListening = false
                    // Restart for wake word
                    setupGlobalSpeechRecognizer()
                }
                override fun onError(error: Int) { 
                    isListening = false
                    // Restart for wake word unless it's a critical error
                    if (error != SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS) {
                        setupGlobalSpeechRecognizer()
                    }
                }
                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        handleGlobalVoiceCommand(matches[0].lowercase())
                    } else {
                        setupGlobalSpeechRecognizer()
                    }
                }
                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
            speechRecognizer?.startListening(intent)
        }
    }

    override fun onPause() {
        super.onPause()
        globalTts?.stop()
    }

    open fun handleGlobalVoiceCommand(command: String) {
        val lowerCommand = command.lowercase()
        
        // 1. Check for wake word "VisionCart"
        if (lowerCommand.contains("vision cart") || lowerCommand.contains("visioncart")) {
            vibrate(100)
            
            // Extract the action (everything after the wake word)
            val action = lowerCommand
                .replace("vision cart", "")
                .replace("visioncart", "")
                .trim()
            
            if (action.isEmpty()) {
                // Just the wake word was said
                speak("Yes? I am listening. Say scan, history, or settings.")
            } else {
                // Wake word + command was said in one go
                processCommand(action)
            }
        }
        
        // ALWAYS restart listening for the next wake word
        setupGlobalSpeechRecognizer()
    }

    private fun processCommand(command: String) {
        when {
            command.contains("scan") -> startActivity(Intent(this, ScanActivity::class.java))
            command.contains("home") || command.contains("dashboard") -> {
                if (this !is MainActivity) {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            }
            command.contains("history") || command.contains("list") -> startActivity(Intent(this, HistoryActivity::class.java))
            command.contains("settings") -> startActivity(Intent(this, SettingsActivity::class.java))
            command.contains("stop") || command.contains("silence") || command.contains("quiet") -> globalTts?.stop()
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_DOWN) {
            if (globalTts?.isSpeaking == true) {
                globalTts?.stop()
                vibrate(30)
                return true
            }
        }
        return super.onTouchEvent(event)
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

    fun setVocalButton(view: View?, description: String) {
        view?.setOnLongClickListener {
            speak(description)
            vibrate(30)
            true
        }
        view?.contentDescription = description
    }

    fun vibrate(duration: Long = 50) {
        com.example.visioncart.util.VibrationUtil.vibrateClick(this)
    }

    fun vibrateSuccess() {
        com.example.visioncart.util.VibrationUtil.vibrateSuccess(this)
    }

    fun vibrateError() {
        com.example.visioncart.util.VibrationUtil.vibrateError(this)
    }

    override fun onDestroy() {
        globalTts?.stop()
        globalTts?.shutdown()
        speechRecognizer?.destroy()
        super.onDestroy()
    }
}
